/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sdmmllc.superdupersmsmanager.mms.pdu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.drm.DrmManagerClient;
import android.net.Uri;
import android.os.Binder;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.provider.Telephony.CanonicalAddressesColumns;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Addr;
import android.provider.Telephony.Mms.Part;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.database.sqlite.SqliteWrapper;
import com.sdmmllc.superdupersmsmanager.mms.ContentType;
import com.sdmmllc.superdupersmsmanager.mms.InvalidHeaderValueException;
import com.sdmmllc.superdupersmsmanager.mms.MmsException;
import com.sdmmllc.superdupersmsmanager.mms.util.DownloadDrmHelper;
import com.sdmmllc.superdupersmsmanager.mms.util.DrmConvertSession;
import com.sdmmllc.superdupersmsmanager.mms.util.PduCache;
import com.sdmmllc.superdupersmsmanager.mms.util.PduCacheEntry;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;
import com.sdmmllc.superdupersmsmanager.sdk.utils.SDContentResolver;
import com.sdmmllc.superdupersmsmanager.sdk.utils.SdmmsUtil;

/**
 * This class is the high-level manager of PDU storage.
 */
public class PduPersister {
    private static final String TAG = "PduPersister";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private static final long DUMMY_THREAD_ID = Long.MAX_VALUE;

    /**
     * The uri of temporary drm objects.
     */
//    public static final String TEMPORARY_DRM_OBJECT_URI =
//        //"content://mms/" + Long.MAX_VALUE + "/part";
//    	SDSmsManager.contentMms() + Long.MAX_VALUE + "/part";
    /**
     * Indicate that we transiently failed to process a MM.
     */
    public static final int PROC_STATUS_TRANSIENT_FAILURE   = 1;
    /**
     * Indicate that we permanently failed to process a MM.
     */
    public static final int PROC_STATUS_PERMANENTLY_FAILURE = 2;
    /**
     * Indicate that we have successfully processed a MM.
     */
    public static final int PROC_STATUS_COMPLETED           = 3;

    private static PduPersister sPersister;
    private static final PduCache PDU_CACHE_INSTANCE;

    private static final int[] ADDRESS_FIELDS = new int[] {
            PduHeaders.BCC,
            PduHeaders.CC,
            PduHeaders.FROM,
            PduHeaders.TO
    };

    private static final String[] PDU_PROJECTION = new String[] {
        Mms._ID,
        Mms.MESSAGE_BOX,
        Mms.THREAD_ID,
        Mms.RETRIEVE_TEXT,
        Mms.SUBJECT,
        Mms.CONTENT_LOCATION,
        Mms.CONTENT_TYPE,
        Mms.MESSAGE_CLASS,
        Mms.MESSAGE_ID,
        Mms.RESPONSE_TEXT,
        Mms.TRANSACTION_ID,
        Mms.CONTENT_CLASS,
        Mms.DELIVERY_REPORT,
        Mms.MESSAGE_TYPE,
        Mms.MMS_VERSION,
        Mms.PRIORITY,
        Mms.READ_REPORT,
        Mms.READ_STATUS,
        Mms.REPORT_ALLOWED,
        Mms.RETRIEVE_STATUS,
        Mms.STATUS,
        Mms.DATE,
        Mms.DELIVERY_TIME,
        Mms.EXPIRY,
        Mms.MESSAGE_SIZE,
        Mms.SUBJECT_CHARSET,
        Mms.RETRIEVE_TEXT_CHARSET,
    };

    private static final int PDU_COLUMN_ID                    = 0;
    private static final int PDU_COLUMN_MESSAGE_BOX           = 1;
    private static final int PDU_COLUMN_THREAD_ID             = 2;
    private static final int PDU_COLUMN_RETRIEVE_TEXT         = 3;
    private static final int PDU_COLUMN_SUBJECT               = 4;
    private static final int PDU_COLUMN_CONTENT_LOCATION      = 5;
    private static final int PDU_COLUMN_CONTENT_TYPE          = 6;
    private static final int PDU_COLUMN_MESSAGE_CLASS         = 7;
    private static final int PDU_COLUMN_MESSAGE_ID            = 8;
    private static final int PDU_COLUMN_RESPONSE_TEXT         = 9;
    private static final int PDU_COLUMN_TRANSACTION_ID        = 10;
    private static final int PDU_COLUMN_CONTENT_CLASS         = 11;
    private static final int PDU_COLUMN_DELIVERY_REPORT       = 12;
    private static final int PDU_COLUMN_MESSAGE_TYPE          = 13;
    private static final int PDU_COLUMN_MMS_VERSION           = 14;
    private static final int PDU_COLUMN_PRIORITY              = 15;
    private static final int PDU_COLUMN_READ_REPORT           = 16;
    private static final int PDU_COLUMN_READ_STATUS           = 17;
    private static final int PDU_COLUMN_REPORT_ALLOWED        = 18;
    private static final int PDU_COLUMN_RETRIEVE_STATUS       = 19;
    private static final int PDU_COLUMN_STATUS                = 20;
    private static final int PDU_COLUMN_DATE                  = 21;
    private static final int PDU_COLUMN_DELIVERY_TIME         = 22;
    private static final int PDU_COLUMN_EXPIRY                = 23;
    private static final int PDU_COLUMN_MESSAGE_SIZE          = 24;
    private static final int PDU_COLUMN_SUBJECT_CHARSET       = 25;
    private static final int PDU_COLUMN_RETRIEVE_TEXT_CHARSET = 26;

    private static final String[] PART_PROJECTION = new String[] {
        Part._ID,
        Part.CHARSET,
        Part.CONTENT_DISPOSITION,
        Part.CONTENT_ID,
        Part.CONTENT_LOCATION,
        Part.CONTENT_TYPE,
        Part.FILENAME,
        Part.NAME,
        Part.TEXT
    };

    private static final int PART_COLUMN_ID                  = 0;
    private static final int PART_COLUMN_CHARSET             = 1;
    private static final int PART_COLUMN_CONTENT_DISPOSITION = 2;
    private static final int PART_COLUMN_CONTENT_ID          = 3;
    private static final int PART_COLUMN_CONTENT_LOCATION    = 4;
    private static final int PART_COLUMN_CONTENT_TYPE        = 5;
    private static final int PART_COLUMN_FILENAME            = 6;
    private static final int PART_COLUMN_NAME                = 7;
    private static final int PART_COLUMN_TEXT                = 8;

    //TODO
    //private static final HashMap<Uri, Integer> MESSAGE_BOX_MAP;
    // These map are used for convenience in persist() and load().
    private static final HashMap<Integer, Integer> CHARSET_COLUMN_INDEX_MAP;
    private static final HashMap<Integer, Integer> ENCODED_STRING_COLUMN_INDEX_MAP;
    private static final HashMap<Integer, Integer> TEXT_STRING_COLUMN_INDEX_MAP;
    private static final HashMap<Integer, Integer> OCTET_COLUMN_INDEX_MAP;
    private static final HashMap<Integer, Integer> LONG_COLUMN_INDEX_MAP;
    private static final HashMap<Integer, String> CHARSET_COLUMN_NAME_MAP;
    private static final HashMap<Integer, String> ENCODED_STRING_COLUMN_NAME_MAP;
    private static final HashMap<Integer, String> TEXT_STRING_COLUMN_NAME_MAP;
    private static final HashMap<Integer, String> OCTET_COLUMN_NAME_MAP;
    private static final HashMap<Integer, String> LONG_COLUMN_NAME_MAP;

    static {
        //MESSAGE_BOX_MAP = new HashMap<Uri, Integer>();
        //MESSAGE_BOX_MAP.put(SDSmsManager.Mms.Inbox.CONTENT_URI(),  Mms.MESSAGE_BOX_INBOX);
        //MESSAGE_BOX_MAP.put(SDSmsManager.Mms.Sent.CONTENT_URI(),   Mms.MESSAGE_BOX_SENT);
        //MESSAGE_BOX_MAP.put(SDSmsManager.Mms.Draft.CONTENT_URI(),  Mms.MESSAGE_BOX_DRAFTS);
        //MESSAGE_BOX_MAP.put(SDSmsManager.Mms.Outbox.CONTENT_URI(), Mms.MESSAGE_BOX_OUTBOX);

        CHARSET_COLUMN_INDEX_MAP = new HashMap<Integer, Integer>();
        CHARSET_COLUMN_INDEX_MAP.put(PduHeaders.SUBJECT, PDU_COLUMN_SUBJECT_CHARSET);
        CHARSET_COLUMN_INDEX_MAP.put(PduHeaders.RETRIEVE_TEXT, PDU_COLUMN_RETRIEVE_TEXT_CHARSET);

        CHARSET_COLUMN_NAME_MAP = new HashMap<Integer, String>();
        CHARSET_COLUMN_NAME_MAP.put(PduHeaders.SUBJECT, Mms.SUBJECT_CHARSET);
        CHARSET_COLUMN_NAME_MAP.put(PduHeaders.RETRIEVE_TEXT, Mms.RETRIEVE_TEXT_CHARSET);

        // Encoded string field code -> column index/name map.
        ENCODED_STRING_COLUMN_INDEX_MAP = new HashMap<Integer, Integer>();
        ENCODED_STRING_COLUMN_INDEX_MAP.put(PduHeaders.RETRIEVE_TEXT, PDU_COLUMN_RETRIEVE_TEXT);
        ENCODED_STRING_COLUMN_INDEX_MAP.put(PduHeaders.SUBJECT, PDU_COLUMN_SUBJECT);

        ENCODED_STRING_COLUMN_NAME_MAP = new HashMap<Integer, String>();
        ENCODED_STRING_COLUMN_NAME_MAP.put(PduHeaders.RETRIEVE_TEXT, Mms.RETRIEVE_TEXT);
        ENCODED_STRING_COLUMN_NAME_MAP.put(PduHeaders.SUBJECT, Mms.SUBJECT);

        // Text string field code -> column index/name map.
        TEXT_STRING_COLUMN_INDEX_MAP = new HashMap<Integer, Integer>();
        TEXT_STRING_COLUMN_INDEX_MAP.put(PduHeaders.CONTENT_LOCATION, PDU_COLUMN_CONTENT_LOCATION);
        TEXT_STRING_COLUMN_INDEX_MAP.put(PduHeaders.CONTENT_TYPE, PDU_COLUMN_CONTENT_TYPE);
        TEXT_STRING_COLUMN_INDEX_MAP.put(PduHeaders.MESSAGE_CLASS, PDU_COLUMN_MESSAGE_CLASS);
        TEXT_STRING_COLUMN_INDEX_MAP.put(PduHeaders.MESSAGE_ID, PDU_COLUMN_MESSAGE_ID);
        TEXT_STRING_COLUMN_INDEX_MAP.put(PduHeaders.RESPONSE_TEXT, PDU_COLUMN_RESPONSE_TEXT);
        TEXT_STRING_COLUMN_INDEX_MAP.put(PduHeaders.TRANSACTION_ID, PDU_COLUMN_TRANSACTION_ID);

        TEXT_STRING_COLUMN_NAME_MAP = new HashMap<Integer, String>();
        TEXT_STRING_COLUMN_NAME_MAP.put(PduHeaders.CONTENT_LOCATION, Mms.CONTENT_LOCATION);
        TEXT_STRING_COLUMN_NAME_MAP.put(PduHeaders.CONTENT_TYPE, Mms.CONTENT_TYPE);
        TEXT_STRING_COLUMN_NAME_MAP.put(PduHeaders.MESSAGE_CLASS, Mms.MESSAGE_CLASS);
        TEXT_STRING_COLUMN_NAME_MAP.put(PduHeaders.MESSAGE_ID, Mms.MESSAGE_ID);
        TEXT_STRING_COLUMN_NAME_MAP.put(PduHeaders.RESPONSE_TEXT, Mms.RESPONSE_TEXT);
        TEXT_STRING_COLUMN_NAME_MAP.put(PduHeaders.TRANSACTION_ID, Mms.TRANSACTION_ID);

        // Octet field code -> column index/name map.
        OCTET_COLUMN_INDEX_MAP = new HashMap<Integer, Integer>();
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.CONTENT_CLASS, PDU_COLUMN_CONTENT_CLASS);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.DELIVERY_REPORT, PDU_COLUMN_DELIVERY_REPORT);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.MESSAGE_TYPE, PDU_COLUMN_MESSAGE_TYPE);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.MMS_VERSION, PDU_COLUMN_MMS_VERSION);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.PRIORITY, PDU_COLUMN_PRIORITY);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.READ_REPORT, PDU_COLUMN_READ_REPORT);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.READ_STATUS, PDU_COLUMN_READ_STATUS);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.REPORT_ALLOWED, PDU_COLUMN_REPORT_ALLOWED);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.RETRIEVE_STATUS, PDU_COLUMN_RETRIEVE_STATUS);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.STATUS, PDU_COLUMN_STATUS);

        OCTET_COLUMN_NAME_MAP = new HashMap<Integer, String>();
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.CONTENT_CLASS, Mms.CONTENT_CLASS);
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.DELIVERY_REPORT, Mms.DELIVERY_REPORT);
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.MESSAGE_TYPE, Mms.MESSAGE_TYPE);
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.MMS_VERSION, Mms.MMS_VERSION);
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.PRIORITY, Mms.PRIORITY);
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.READ_REPORT, Mms.READ_REPORT);
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.READ_STATUS, Mms.READ_STATUS);
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.REPORT_ALLOWED, Mms.REPORT_ALLOWED);
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.RETRIEVE_STATUS, Mms.RETRIEVE_STATUS);
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.STATUS, Mms.STATUS);

        // Long field code -> column index/name map.
        LONG_COLUMN_INDEX_MAP = new HashMap<Integer, Integer>();
        LONG_COLUMN_INDEX_MAP.put(PduHeaders.DATE, PDU_COLUMN_DATE);
        LONG_COLUMN_INDEX_MAP.put(PduHeaders.DELIVERY_TIME, PDU_COLUMN_DELIVERY_TIME);
        LONG_COLUMN_INDEX_MAP.put(PduHeaders.EXPIRY, PDU_COLUMN_EXPIRY);
        LONG_COLUMN_INDEX_MAP.put(PduHeaders.MESSAGE_SIZE, PDU_COLUMN_MESSAGE_SIZE);

        LONG_COLUMN_NAME_MAP = new HashMap<Integer, String>();
        LONG_COLUMN_NAME_MAP.put(PduHeaders.DATE, Mms.DATE);
        LONG_COLUMN_NAME_MAP.put(PduHeaders.DELIVERY_TIME, Mms.DELIVERY_TIME);
        LONG_COLUMN_NAME_MAP.put(PduHeaders.EXPIRY, Mms.EXPIRY);
        LONG_COLUMN_NAME_MAP.put(PduHeaders.MESSAGE_SIZE, Mms.MESSAGE_SIZE);

        PDU_CACHE_INSTANCE = PduCache.getInstance();
     }

    private final Context mContext;
    private final SDContentResolver mContentResolver;
    private final DrmManagerClient mDrmManagerClient;
    private final TelephonyManager mTelephonyManager;

    private PduPersister(Context context) {
        mContext = context;
        mContentResolver = new SDContentResolver(context);
        mDrmManagerClient = new DrmManagerClient(context);
        mTelephonyManager = (TelephonyManager)context
                .getSystemService(Context.TELEPHONY_SERVICE);
     }

    /** Get(or create if not exist) an instance of PduPersister */
    public static PduPersister getPduPersister(Context context) {
        if ((sPersister == null) || !context.equals(sPersister.mContext)) {
            sPersister = new PduPersister(context);
        }

        return sPersister;
    }

    private void setEncodedStringValueToHeaders(
            Cursor c, int columnIndex,
            PduHeaders headers, int mapColumn) {
        String s = c.getString(columnIndex);
        if ((s != null) && (s.length() > 0)) {
            int charsetColumnIndex = CHARSET_COLUMN_INDEX_MAP.get(mapColumn);
            int charset = c.getInt(charsetColumnIndex);
            EncodedStringValue value = new EncodedStringValue(
                    charset, getBytes(s));
            headers.setEncodedStringValue(value, mapColumn);
        }
    }

    private void setTextStringToHeaders(
            Cursor c, int columnIndex,
            PduHeaders headers, int mapColumn) {
        String s = c.getString(columnIndex);
        if (s != null) {
            headers.setTextString(getBytes(s), mapColumn);
        }
    }

    private void setOctetToHeaders(
            Cursor c, int columnIndex,
            PduHeaders headers, int mapColumn) throws InvalidHeaderValueException {
        if (!c.isNull(columnIndex)) {
            int b = c.getInt(columnIndex);
            headers.setOctet(b, mapColumn);
        }
    }

    private void setLongToHeaders(
            Cursor c, int columnIndex,
            PduHeaders headers, int mapColumn) {
        if (!c.isNull(columnIndex)) {
            long l = c.getLong(columnIndex);
            headers.setLongInteger(l, mapColumn);
        }
    }

    private Integer getIntegerFromPartColumn(Cursor c, int columnIndex) {
        if (!c.isNull(columnIndex)) {
            return c.getInt(columnIndex);
        }
        return null;
    }

    private byte[] getByteArrayFromPartColumn(Cursor c, int columnIndex) {
        if (!c.isNull(columnIndex)) {
            return getBytes(c.getString(columnIndex));
        }
        return null;
    }

    private PduPart[] loadParts(long msgId) throws MmsException {
        Cursor c = SqliteWrapper.query(mContext, mContentResolver,
                //Uri.parse("content://mms/" + msgId + "/part"),
                Uri.parse(SDSmsManager.contentMms() + msgId + "/part"),
                PART_PROJECTION, null, null, null);

        PduPart[] parts = null;

        try {
            if ((c == null) || (c.getCount() == 0)) {
                if (LOCAL_LOGV) {
                    Log.v(TAG, "loadParts(" + msgId + "): no part to load.");
                }
                return null;
            }

            int partCount = c.getCount();
            int partIdx = 0;
            parts = new PduPart[partCount];
            while (c.moveToNext()) {
                PduPart part = new PduPart();
                Integer charset = getIntegerFromPartColumn(
                        c, PART_COLUMN_CHARSET);
                if (charset != null) {
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                    Log.i(TAG, "MMS PDU loadParts _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ PART_COLUMN_CHARSET " +
	                    		" charset: " + charset);
                    part.setCharset(charset);
                }

                byte[] contentDisposition = getByteArrayFromPartColumn(
                        c, PART_COLUMN_CONTENT_DISPOSITION);
                if (contentDisposition != null) {
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                    Log.i(TAG, "MMS PDU loadParts _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ PART_COLUMN_CONTENT_DISPOSITION " +
	                    		" contentDisposition: " + contentDisposition);
                    part.setContentDisposition(contentDisposition);
                }

                byte[] contentId = getByteArrayFromPartColumn(
                        c, PART_COLUMN_CONTENT_ID);
                if (contentId != null) {
                    part.setContentId(contentId);
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                    Log.i(TAG, "MMS PDU loadParts _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ PART_COLUMN_CONTENT_ID " +
	                    		" contentId: " + contentId);
                }

                byte[] contentLocation = getByteArrayFromPartColumn(
                        c, PART_COLUMN_CONTENT_LOCATION);
                if (contentLocation != null) {
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                    Log.i(TAG, "MMS PDU loadParts _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ PART_COLUMN_CONTENT_LOCATION " +
	                    		" contentLocation: " + contentLocation);
                    part.setContentLocation(contentLocation);
                }

                byte[] contentType = getByteArrayFromPartColumn(
                        c, PART_COLUMN_CONTENT_TYPE);
                if (contentType != null) {
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                    Log.i(TAG, "MMS PDU loadParts _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ PART_COLUMN_CONTENT_TYPE " +
	                    		" contentType: " + contentType);
                    part.setContentType(contentType);
                } else {
                    throw new MmsException("Content-Type must be set.");
                }

                byte[] fileName = getByteArrayFromPartColumn(
                        c, PART_COLUMN_FILENAME);
                if (fileName != null) {
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                    Log.i(TAG, "MMS PDU loadParts _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ PART_COLUMN_FILENAME " +
	                    		" fileName: " + fileName);
                    part.setFilename(fileName);
                }

                byte[] name = getByteArrayFromPartColumn(
                        c, PART_COLUMN_NAME);
                if (name != null) {
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                    Log.i(TAG, "MMS PDU loadParts _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ PART_COLUMN_NAME " +
	                    		" name: " + name);
                    part.setName(name);
                }

                // Construct a Uri for this part.
                long partId = c.getLong(PART_COLUMN_ID);
                //Uri partURI = Uri.parse("content://mms/part/" + partId);
                Uri partURI = Uri.parse(SDSmsManager.contentMms() + "part/" + partId);
                part.setDataUri(partURI);

                // For images/audio/video, we won't keep their data in Part
                // because their renderer accept Uri as source.
                String type = toIsoString(contentType);
                if (!ContentType.isImageType(type)
                        && !ContentType.isAudioType(type)
                        && !ContentType.isVideoType(type)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    InputStream is = null;

                    // Store simple string values directly in the database instead of an
                    // external file.  This makes the text searchable and retrieval slightly
                    // faster.
                    if (ContentType.TEXT_PLAIN.equals(type) || ContentType.APP_SMIL.equals(type)
                            || ContentType.TEXT_HTML.equals(type)) {
                        String text = c.getString(PART_COLUMN_TEXT);
                        byte [] blob = new EncodedStringValue(text != null ? text : "")
                            .getTextString();
                        baos.write(blob, 0, blob.length);
                    } else {

                        try {
                            is = mContentResolver.openInputStream(partURI);

                            byte[] buffer = new byte[256];
                            int len = is.read(buffer);
                            while (len >= 0) {
                                baos.write(buffer, 0, len);
                                len = is.read(buffer);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to load part data", e);
                            c.close();
                            throw new MmsException(e);
                        } finally {
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Failed to close stream", e);
                                } // Ignore
                            }
                        }
                    }
                    part.setData(baos.toByteArray());
                }
                parts[partIdx++] = part;
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return parts;
    }

    private void loadAddress(long msgId, PduHeaders headers) {
        Cursor c = SqliteWrapper.query(mContext, mContentResolver,
                //Uri.parse("content://mms/" + msgId + "/addr"),
                Uri.parse(SDSmsManager.contentMms() + msgId + "/addr"),
                new String[] { Addr.ADDRESS, Addr.CHARSET, Addr.TYPE },
                null, null, null);

        if (c != null) {
            try {
                while (c.moveToNext()) {
                    String addr = c.getString(0);
                    if (!TextUtils.isEmpty(addr)) {
                        int addrType = c.getInt(2);
                        switch (addrType) {
                            case PduHeaders.FROM:
                                if (SDSmsConsts.DEBUG_RECEIVE_MMS)
            	                    Log.i(TAG, "MMS PDU loadAddress _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ FROM: " +
            	                    		" addr: " + (new EncodedStringValue(c.getInt(1), getBytes(addr))).getString());
                                headers.setEncodedStringValue(
                                        new EncodedStringValue(c.getInt(1), getBytes(addr)),
                                        addrType);
                                break;
                            case PduHeaders.TO:
                            case PduHeaders.CC:
                            case PduHeaders.BCC:
                                if (SDSmsConsts.DEBUG_RECEIVE_MMS)
            	                    Log.i(TAG, "MMS PDU loadAddress _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ TO: " +
            	                    		" addr: " + (new EncodedStringValue(c.getInt(1), getBytes(addr))).getString());
                                headers.appendEncodedStringValue(
                                        new EncodedStringValue(c.getInt(1), getBytes(addr)),
                                        addrType);
                                break;
                            default:
                                Log.e(TAG, "Unknown address type: " + addrType);
                                break;
                        }
                    }
                }
            } finally {
                c.close();
            }
        }
    }

    // This must be consistent with the column constants below.
    private static final String[] MMS_STATUS_PROJECTION = new String[] {
        Mms.THREAD_ID, Mms.DATE, Mms._ID, Mms.SUBJECT, Mms.SUBJECT_CHARSET };
    private static final String NEW_INCOMING_MM_CONSTRAINT =
            "(" + Mms.MESSAGE_BOX + "=" + Mms.MESSAGE_BOX_INBOX
            + " AND " + Mms.SEEN + "=0"
            + " AND (" + Mms.MESSAGE_TYPE + "=" + PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND
            + " OR " + Mms.MESSAGE_TYPE + "=" + PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF + "))";
    private static final int COLUMN_THREAD_ID   = 0;
    private static final int COLUMN_MMS_ID      = 2;

    private static long findThreadId(Context context, GenericPdu pdu, int type) {
        SDContentResolver resolver = (new SDContentResolver(context));

        Cursor cursor = SqliteWrapper.query(context, resolver, SDSmsManager.Mms.CONTENT_URI(),
                MMS_STATUS_PROJECTION, NEW_INCOMING_MM_CONSTRAINT,
                null, Mms.DATE + " desc");

		if (cursor == null) {
			return -1;
		}
		
        try {
            while (cursor.moveToNext()) {

                long msgId = cursor.getLong(COLUMN_MMS_ID);
                Uri msgUri = SDSmsManager.Mms.CONTENT_URI().buildUpon().appendPath(
                        Long.toString(msgId)).build();
                long threadId = cursor.getLong(COLUMN_THREAD_ID);
                if (SDSmsConsts.DEBUG_RECEIVE_MMS) {
                    Log.i(TAG, "addMmsNotificationInfos _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ " +
                    		" threadId: " + threadId);
            	}
                if (msgId >= 0) {
                	cursor.close();
                	return msgId;
                }
            }
        } finally {
            cursor.close();
        }
        return -1;
    }

    private long getThreadId(long msgId, PduHeaders headers) {
        Cursor c = SqliteWrapper.query(mContext, mContentResolver,
                //Uri.parse("content://mms/" + msgId + "/addr"),
                Uri.parse(SDSmsManager.contentMms() + msgId + "/addr"),
                new String[] { Addr.ADDRESS, Addr.CHARSET, Addr.TYPE },
                null, null, null);

        if (c != null) {
            try {
                while (c.moveToNext()) {
                    String addr = c.getString(0);
                    if (!TextUtils.isEmpty(addr)) {
                        int addrType = c.getInt(2);
                        switch (addrType) {
                            case PduHeaders.FROM:
                                if (SDSmsConsts.DEBUG_RECEIVE_MMS)
            	                    Log.i(TAG, "MMS PDU getAddress _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ FROM: " +
            	                    		" addr: " + (new EncodedStringValue(c.getInt(1), getBytes(addr))).getString());
                                return SdmmsUtil.getOrCreateThreadId(SDSmsManager.getContext(),
                                		(new EncodedStringValue(c.getInt(1), getBytes(addr))).toString());
                            case PduHeaders.TO:
                            case PduHeaders.CC:
                            case PduHeaders.BCC:
                            default:
                                Log.e(TAG, "Unknown address type: " + addrType);
                                break;
                        }
                    }
                }
            } finally {
                c.close();
            }
        }
        return -1;
    }

    /**
     * Load a PDU from storage by given Uri.
     *
     * @param uri The Uri of the PDU to be loaded.
     * @return A generic PDU object, it may be cast to dedicated PDU.
     * @throws MmsException Failed to load some fields of a PDU.
     */
    public GenericPdu load(Uri uri) throws MmsException {
        GenericPdu pdu = null;
        PduCacheEntry cacheEntry = null;
        int msgBox = 0;
        long threadId = -1;
        try {
            synchronized(PDU_CACHE_INSTANCE) {
                if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                    if (LOCAL_LOGV) {
                        Log.v(TAG, "load: " + uri + " blocked by isUpdating()");
                    }
                    try {
                        PDU_CACHE_INSTANCE.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "load: ", e);
                    }
                    cacheEntry = PDU_CACHE_INSTANCE.get(uri);
                    if (cacheEntry != null) {
                        return cacheEntry.getPdu();
                    }
                }
                // Tell the cache to indicate to other callers that this item
                // is currently being updated.
                PDU_CACHE_INSTANCE.setUpdating(uri, true);
            }

            Cursor c = SqliteWrapper.query(mContext, mContentResolver, uri,
                    PDU_PROJECTION, null, null, null);
            PduHeaders headers = new PduHeaders();
            Set<Entry<Integer, Integer>> set;
            long msgId = ContentUris.parseId(uri);

            if (SDSmsConsts.DEBUG_RECEIVE_MMS)
                Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ uri: " +
                		uri + " msgId: " + msgId);
            try {
                if ((c == null) || (c.getCount() != 1) || !c.moveToFirst()) {
                    throw new MmsException("Bad uri: " + uri);
                }

                msgBox = c.getInt(PDU_COLUMN_MESSAGE_BOX);
                threadId = c.getLong(PDU_COLUMN_THREAD_ID);
                if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ msgBox: " 
	                		+ msgBox + " threadId: " + threadId);

                set = ENCODED_STRING_COLUMN_INDEX_MAP.entrySet();
                for (Entry<Integer, Integer> e : set) {
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                    Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ ENCODED_STRING_COLUMN_INDEX_MAP" +
	                    		" - e.getValue(): " + e.getValue() 
	                    		+ " e.getKey(): " + e.getKey());
                    setEncodedStringValueToHeaders(
                            c, e.getValue(), headers, e.getKey());
                }

                set = TEXT_STRING_COLUMN_INDEX_MAP.entrySet();
                for (Entry<Integer, Integer> e : set) {
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                    Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ TEXT_STRING_COLUMN_INDEX_MAP" +
	                    		" - e.getValue(): " + e.getValue() 
	                    		+ " e.getKey(): " + e.getKey());
                    setTextStringToHeaders(
                            c, e.getValue(), headers, e.getKey());
                }

                set = OCTET_COLUMN_INDEX_MAP.entrySet();
                for (Entry<Integer, Integer> e : set) {
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                    Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ OCTET_COLUMN_INDEX_MAP" +
	                    		" - e.getValue(): " + e.getValue() 
	                    		+ " e.getKey(): " + e.getKey());
                    setOctetToHeaders(
                            c, e.getValue(), headers, e.getKey());
                }

                set = LONG_COLUMN_INDEX_MAP.entrySet();
                for (Entry<Integer, Integer> e : set) {
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                    Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ LONG_COLUMN_INDEX_MAP" +
	                    		" - e.getValue(): " + e.getValue() 
	                    		+ " e.getKey(): " + e.getKey());
                    setLongToHeaders(
                            c, e.getValue(), headers, e.getKey());
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            // Check whether 'msgId' has been assigned a valid value.
            if (msgId == -1L) {
                throw new MmsException("Error! ID of the message: -1.");
            }

            // Load address information of the MM.
            loadAddress(msgId, headers);

            int msgType = headers.getOctet(PduHeaders.MESSAGE_TYPE);
            Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ MESSAGE_TYPE - msgType: " + msgType);
            PduBody body = new PduBody();

            // For PDU which type is M_retrieve.conf or Send.req, we should
            // load multiparts and put them into the body of the PDU.
            if ((msgType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF)
                    || (msgType == PduHeaders.MESSAGE_TYPE_SEND_REQ)) {
                PduPart[] parts = loadParts(msgId);
                if (parts != null) {
                    int partsNum = parts.length;
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
                        Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ MESSAGE_TYPE_RETRIEVE_CONF or " +
                        		"MESSAGE_TYPE_SEND_REQ - partsNum: " + partsNum);
                    for (int i = 0; i < partsNum; i++) {
                        if (SDSmsConsts.DEBUG_RECEIVE_MMS)
                            Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ MESSAGE_TYPE_RETRIEVE_CONF or " +
                            		"MESSAGE_TYPE_SEND_REQ - parts[" + i + "]: " + parts[i]);
                        body.addPart(parts[i]);
                    }
                }
            }

            switch (msgType) {
            case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                pdu = new NotificationInd(headers);
                break;
            case PduHeaders.MESSAGE_TYPE_DELIVERY_IND:
                pdu = new DeliveryInd(headers);
                break;
            case PduHeaders.MESSAGE_TYPE_READ_ORIG_IND:
                pdu = new ReadOrigInd(headers);
                break;
            case PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF:
                pdu = new RetrieveConf(headers, body);
                break;
            case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                pdu = new SendReq(headers, body);
                break;
            case PduHeaders.MESSAGE_TYPE_ACKNOWLEDGE_IND:
                pdu = new AcknowledgeInd(headers);
                break;
            case PduHeaders.MESSAGE_TYPE_NOTIFYRESP_IND:
                pdu = new NotifyRespInd(headers);
                break;
            case PduHeaders.MESSAGE_TYPE_READ_REC_IND:
                pdu = new ReadRecInd(headers);
                break;
            case PduHeaders.MESSAGE_TYPE_SEND_CONF:
            case PduHeaders.MESSAGE_TYPE_FORWARD_REQ:
            case PduHeaders.MESSAGE_TYPE_FORWARD_CONF:
            case PduHeaders.MESSAGE_TYPE_MBOX_STORE_REQ:
            case PduHeaders.MESSAGE_TYPE_MBOX_STORE_CONF:
            case PduHeaders.MESSAGE_TYPE_MBOX_VIEW_REQ:
            case PduHeaders.MESSAGE_TYPE_MBOX_VIEW_CONF:
            case PduHeaders.MESSAGE_TYPE_MBOX_UPLOAD_REQ:
            case PduHeaders.MESSAGE_TYPE_MBOX_UPLOAD_CONF:
            case PduHeaders.MESSAGE_TYPE_MBOX_DELETE_REQ:
            case PduHeaders.MESSAGE_TYPE_MBOX_DELETE_CONF:
            case PduHeaders.MESSAGE_TYPE_MBOX_DESCR:
            case PduHeaders.MESSAGE_TYPE_DELETE_REQ:
            case PduHeaders.MESSAGE_TYPE_DELETE_CONF:
            case PduHeaders.MESSAGE_TYPE_CANCEL_REQ:
            case PduHeaders.MESSAGE_TYPE_CANCEL_CONF:
                throw new MmsException(
                        "Unsupported PDU type: " + Integer.toHexString(msgType));

            default:
                throw new MmsException(
                        "Unrecognized PDU type: " + Integer.toHexString(msgType));
            }
        } finally {
            synchronized(PDU_CACHE_INSTANCE) {
                if (pdu != null) {
                    assert(PDU_CACHE_INSTANCE.get(uri) == null);
                    // Update the cache entry with the real info
                    cacheEntry = new PduCacheEntry(pdu, msgBox, threadId);
                    PDU_CACHE_INSTANCE.put(uri, cacheEntry);
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS)
                        Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ PDU_CACHE_INSTANCE update " +
                        		"MESSAGE_TYPE_SEND_REQ - msgBox: " + msgBox +
                        		" threadId: " + threadId);
                }
                PDU_CACHE_INSTANCE.setUpdating(uri, false);
                PDU_CACHE_INSTANCE.notifyAll(); // tell anybody waiting on this entry to go ahead
            }
        }
        return pdu;
    }

    private void persistAddress(
            long msgId, int type, EncodedStringValue[] array) {
        ContentValues values = new ContentValues(3);

        for (EncodedStringValue addr : array) {
            values.clear(); // Clear all values first.
            values.put(Addr.ADDRESS, toIsoString(addr.getTextString()));
            values.put(Addr.CHARSET, addr.getCharacterSet());
            values.put(Addr.TYPE, type);

            //Uri uri = Uri.parse("content://mms/" + msgId + "/addr");
            Uri uri = Uri.parse(SDSmsManager.contentMms() + msgId + "/addr");
            SqliteWrapper.insert(mContext, mContentResolver, uri, values);
        }
    }

    private static String getPartContentType(PduPart part) {
        return part.getContentType() == null ? null : toIsoString(part.getContentType());
    }

    public Uri persistPart(PduPart part, long msgId, HashMap<Uri, InputStream> preOpenedFiles)
            throws MmsException {
        //Uri uri = Uri.parse("content://mms/" + msgId + "/part");
        Uri uri = Uri.parse(SDSmsManager.contentMms() + msgId + "/part");
        ContentValues values = new ContentValues(8);
      	if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "persistPart uri: " + uri);
    	if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "persistPart part.getDataUri(): " + part.getDataUri());
  
        int charset = part.getCharset();
        if (charset != 0 ) {
            values.put(Part.CHARSET, charset);
        }

        String contentType = getPartContentType(part);
        if (contentType != null) {
            // There is no "image/jpg" in Android (and it's an invalid mimetype).
            // Change it to "image/jpeg"
            if (ContentType.IMAGE_JPG.equals(contentType)) {
                contentType = ContentType.IMAGE_JPEG;
            }

            values.put(Part.CONTENT_TYPE, contentType);
            // To ensure the SMIL part is always the first part.
            if (ContentType.APP_SMIL.equals(contentType)) {
                values.put(Part.SEQ, -1);
            }
        } else {
            throw new MmsException("MIME type of the part must be set.");
        }

        if (part.getFilename() != null) {
            String fileName = new String(part.getFilename());
            values.put(Part.FILENAME, fileName);
        }

        if (part.getName() != null) {
            String name = new String(part.getName());
            values.put(Part.NAME, name);
        }

        Object value = null;
        if (part.getContentDisposition() != null) {
            value = toIsoString(part.getContentDisposition());
            values.put(Part.CONTENT_DISPOSITION, (String) value);
        }

        if (part.getContentId() != null) {
            value = toIsoString(part.getContentId());
            values.put(Part.CONTENT_ID, (String) value);
        }

        if (part.getContentLocation() != null) {
            value = toIsoString(part.getContentLocation());
            values.put(Part.CONTENT_LOCATION, (String) value);
        }

    	if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, "problem is here!!!!");
        Uri res = SqliteWrapper.insert(mContext, mContentResolver, uri, values);
        if (res == null) {
            throw new MmsException("Failed to persist part, return null.");
        }
    	if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "persistPart before persistData res null: " + (res == null));
    	if (SDSmsConsts.DEBUG_SEND_MMS && uri != null) Log.i(TAG, "persistPart before persistData res: " + res.toString());
    	if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "persistPart before persistData uri null: " + (uri == null));
    	if (SDSmsConsts.DEBUG_SEND_MMS && uri != null) Log.i(TAG, "persistPart before persistData uri: " + uri.toString());
    	if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "persistPart before persistData part.getDataUri() null: " + (part.getDataUri() == null));
    	if (SDSmsConsts.DEBUG_SEND_MMS && part.getDataUri() != null) Log.i(TAG, "persistPart before persistData part.getDataUri(): " + part.getDataUri().toString());

        persistData(part, res, contentType, preOpenedFiles);
        // After successfully store the data, we should update
        // the dataUri of the part.
    	if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "persistPart after persistData res null: " + (res == null));
    	if (SDSmsConsts.DEBUG_SEND_MMS && uri != null) Log.i(TAG, "persistPart after persistData res: " + res.toString());
        part.setDataUri(res);
    	if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "persistPart after set persistData part.getDataUri() null: " + (part.getDataUri() == null));
    	if (SDSmsConsts.DEBUG_SEND_MMS && part.getDataUri() != null) Log.i(TAG, "persistPart after set persistData part.getDataUri(): " + part.getDataUri().toString());

        return res;
    }

    /**
     * Save data of the part into storage. The source data may be given
     * by a byte[] or a Uri. If it's a byte[], directly save it
     * into storage, otherwise load source data from the dataUri and then
     * save it. If the data is an image, we may scale down it according
     * to user preference.
     *
     * @param part The PDU part which contains data to be saved.
     * @param uri The URI of the part.
     * @param contentType The MIME type of the part.
     * @param preOpenedFiles if not null, a map of preopened InputStreams for the parts.
     * @throws MmsException Cannot find source data or error occurred
     *         while saving the data.
     */
    private void persistData(PduPart part, Uri uri,
            String contentType, HashMap<Uri, InputStream> preOpenedFiles)
            throws MmsException {
        OutputStream os = null;
        InputStream is = null;
        DrmConvertSession drmConvertSession = null;
        Uri dataUri = null;
        String path = null;

    	if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "persistData part: " + part);
    	if (SDSmsConsts.DEBUG_SEND_MMS) {
    		if (part != null) Log.i(TAG, "persistData part.getDataUri(): " + part.getDataUri());
        	else Log.i(TAG, "persistData part is null ");
    		if (part != null) Log.i(TAG, "persistData uri: " + uri);
    	}

        try {
            byte[] data = part.getData();
            if (ContentType.TEXT_PLAIN.equals(contentType)
                    || ContentType.APP_SMIL.equals(contentType)
                    || ContentType.TEXT_HTML.equals(contentType)) {
                ContentValues cv = new ContentValues();
                cv.put(Telephony.Mms.Part.TEXT, new EncodedStringValue(data).getString());
                if (mContentResolver.update(uri, cv, null, null) != 1) {
                    throw new MmsException("unable to update " + uri.toString());
                }
            } else {
                boolean isDrm = DownloadDrmHelper.isDrmConvertNeeded(contentType);
                if (isDrm) {
                    if (uri != null) {
                        try {
                            path = convertUriToPath(mContext, uri);
                            if (LOCAL_LOGV) {
                                Log.v(TAG, "drm uri: " + uri + " path: " + path);
                            }
                            File f = new File(path);
                            long len = f.length();
                            if (LOCAL_LOGV) {
                                Log.v(TAG, "drm path: " + path + " len: " + len);
                            }
                            if (len > 0) {
                                // we're not going to re-persist and re-encrypt an already
                                // converted drm file
                                return;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Can't get file info for: " + part.getDataUri(), e);
                        }
                    }
                    // We haven't converted the file yet, start the conversion
                    drmConvertSession = DrmConvertSession.open(mContext, contentType);
                    if (drmConvertSession == null) {
                        throw new MmsException("Mimetype " + contentType +
                                " can not be converted.");
                    }
                }
                // uri can look like:
                // content://mms/part/98
                if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "persistData uri is null: " + (uri == null));
            	if (SDSmsConsts.DEBUG_SEND_MMS && uri != null) Log.i(TAG, "persistData uri: " + uri.toString());
                if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "persistData part.getDataUri(): " + part.getDataUri());
                os = mContentResolver.openOutputStream(uri);
                if (data == null) {
                    dataUri = part.getDataUri();
                    if ((dataUri == null) || (dataUri == uri)) {
                        Log.w(TAG, "Can't find data for this part.");
                        return;
                    }
                    // dataUri can look like:
                    // content://com.google.android.gallery3d.provider/picasa/item/5720646660183715586
                    if (preOpenedFiles != null && preOpenedFiles.containsKey(dataUri)) {
                        is = preOpenedFiles.get(dataUri);
                    }
                    if (SDSmsConsts.DEBUG_SEND_MMS && (is == null)) {
                    	Log.i(TAG, "persistData inputstream preOpenedFiles null dataUri: " + dataUri.toString());
                    	Log.i(TAG, "persistData inputstream preOpenedFiles null uri: " + uri.toString());
                    } else {
                    	Log.i(TAG, "persistData inputstream preOpenedFiles not null dataUri: " + dataUri.toString());
                    	Log.i(TAG, "persistData inputstream preOpenedFiles not null uri: " + uri.toString());
                    }
                    if (is == null) {
                        is = mContentResolver.openInputStream(dataUri);
                    }
                    if (SDSmsConsts.DEBUG_SEND_MMS && (is == null)) {
                    	Log.i(TAG, "persistData inputstream null dataUri: " + dataUri.toString());
                    	Log.i(TAG, "persistData inputstream null uri: " + uri.toString());
                    } else {
                    	Log.i(TAG, "persistData inputstream not null dataUri: " + dataUri.toString());
                    	Log.i(TAG, "persistData inputstream not null uri: " + uri.toString());
                    }

                    if (LOCAL_LOGV) {
                        Log.v(TAG, "Saving data to: " + uri);
                    }

                    byte[] buffer = new byte[8192];
                    for (int len = 0; (len = is.read(buffer)) != -1; ) {
                        if (!isDrm) {
                            os.write(buffer, 0, len);
                        } else {
                            byte[] convertedData = drmConvertSession.convert(buffer, len);
                            if (convertedData != null) {
                                os.write(convertedData, 0, convertedData.length);
                            } else {
                                throw new MmsException("Error converting drm data.");
                            }
                        }
                    }
                } else {
                    if (LOCAL_LOGV) {
                        Log.v(TAG, "Saving data to: " + uri);
                    }
                    if (!isDrm) {
                        os.write(data);
                    } else {
                        dataUri = uri;
                        byte[] convertedData = drmConvertSession.convert(data, data.length);
                        if (convertedData != null) {
                            os.write(convertedData, 0, convertedData.length);
                        } else {
                            throw new MmsException("Error converting drm data.");
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to open Input/Output stream.", e);
            throw new MmsException(e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read/write data.", e);
            throw new MmsException(e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException while closing: " + os, e);
                } // Ignore
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException while closing: " + is, e);
                } // Ignore
            }
            if (drmConvertSession != null) {
                drmConvertSession.close(path);

                // Reset the permissions on the encrypted part file so everyone has only read
                // permission.
                File f = new File(path);
                ContentValues values = new ContentValues(0);
                SqliteWrapper.update(mContext, mContentResolver,
                					//Uri.parse("content://mms/resetFilePerm/" + f.getName()),
                					Uri.parse(SDSmsManager.contentMms() + "resetFilePerm/" + f.getName()),
                                    values, null, null);
            }
        }
    }

    /**
     * This method expects uri in the following format
     *     content://media/<table_name>/<row_index> (or)
     *     file://sdcard/test.mp4
     *     http://test.com/test.mp4
     *
     * Here <table_name> shall be "video" or "audio" or "images"
     * <row_index> the index of the content in given table
     */
    static public String convertUriToPath(Context context, Uri uri) {
        String path = null;
        if (null != uri) {
            String scheme = uri.getScheme();
            if (null == scheme || scheme.equals("") ||
                    scheme.equals(ContentResolver.SCHEME_FILE)) {
                path = uri.getPath();

            } else if (scheme.equals("http")) {
                path = uri.toString();

            } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                String[] projection = new String[] {MediaStore.MediaColumns.DATA};
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(uri, projection, null,
                            null, null);
                    if (null == cursor || 0 == cursor.getCount() || !cursor.moveToFirst()) {
                        throw new IllegalArgumentException("Given Uri could not be found" +
                                " in media store");
                    }
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    path = cursor.getString(pathIndex);
                } catch (SQLiteException e) {
                    throw new IllegalArgumentException("Given Uri is not formatted in a way " +
                            "so that it can be found in media store.");
                } finally {
                    if (null != cursor) {
                        cursor.close();
                    }
                }
            } else {
                throw new IllegalArgumentException("Given Uri scheme is not supported");
            }
        }
        return path;
    }

    private void updateAddress(
            long msgId, int type, EncodedStringValue[] array) {
        // Delete old address information and then insert new ones.
        SqliteWrapper.delete(mContext, mContentResolver,
                //Uri.parse("content://mms/" + msgId + "/addr"),
                Uri.parse(SDSmsManager.contentMms() + msgId + "/addr"),
                Addr.TYPE + "=" + type, null);

        persistAddress(msgId, type, array);
    }

    /**
     * Update headers of a SendReq.
     *
     * @param uri The PDU which need to be updated.
     * @param pdu New headers.
     * @throws MmsException Bad URI or updating failed.
     */
    public void updateHeaders(Uri uri, SendReq sendReq) {
        synchronized(PDU_CACHE_INSTANCE) {
            // If the cache item is getting updated, wait until it's done updating before
            // purging it.
            if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                if (LOCAL_LOGV) {
                    Log.v(TAG, "updateHeaders: " + uri + " blocked by isUpdating()");
                }
                try {
                    PDU_CACHE_INSTANCE.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "updateHeaders: ", e);
                }
            }
        }
        PDU_CACHE_INSTANCE.purge(uri);

        ContentValues values = new ContentValues(10);
        byte[] contentType = sendReq.getContentType();
        if (contentType != null) {
            values.put(Mms.CONTENT_TYPE, toIsoString(contentType));
        }

        long date = sendReq.getDate();
        if (date != -1) {
            values.put(Mms.DATE, date);
        }

        int deliveryReport = sendReq.getDeliveryReport();
        if (deliveryReport != 0) {
            values.put(Mms.DELIVERY_REPORT, deliveryReport);
        }

        long expiry = sendReq.getExpiry();
        if (expiry != -1) {
            values.put(Mms.EXPIRY, expiry);
        }

        byte[] msgClass = sendReq.getMessageClass();
        if (msgClass != null) {
            values.put(Mms.MESSAGE_CLASS, toIsoString(msgClass));
        }

        int priority = sendReq.getPriority();
        if (priority != 0) {
            values.put(Mms.PRIORITY, priority);
        }

        int readReport = sendReq.getReadReport();
        if (readReport != 0) {
            values.put(Mms.READ_REPORT, readReport);
        }

        byte[] transId = sendReq.getTransactionId();
        if (transId != null) {
            values.put(Mms.TRANSACTION_ID, toIsoString(transId));
        }

        EncodedStringValue subject = sendReq.getSubject();
        if (subject != null) {
            values.put(Mms.SUBJECT, toIsoString(subject.getTextString()));
            values.put(Mms.SUBJECT_CHARSET, subject.getCharacterSet());
        } else {
            values.put(Mms.SUBJECT, "");
        }

        long messageSize = sendReq.getMessageSize();
        if (messageSize > 0) {
            values.put(Mms.MESSAGE_SIZE, messageSize);
        }

        PduHeaders headers = sendReq.getPduHeaders();
        HashSet<String> recipients = new HashSet<String>();
        for (int addrType : ADDRESS_FIELDS) {
            EncodedStringValue[] array = null;
            if (addrType == PduHeaders.FROM) {
                EncodedStringValue v = headers.getEncodedStringValue(addrType);
                if (v != null) {
                    array = new EncodedStringValue[1];
                    array[0] = v;
                }
            } else {
                array = headers.getEncodedStringValues(addrType);
            }

            if (array != null) {
                long msgId = ContentUris.parseId(uri);
                updateAddress(msgId, addrType, array);
                if (addrType == PduHeaders.TO) {
                    for (EncodedStringValue v : array) {
                        if (v != null) {
                            recipients.add(v.getString());
                        }
                    }
                }
            }
        }
        if (!recipients.isEmpty()) {
        	//TODO
            //long threadId = Threads.getOrCreateThreadId(mContext, recipients);
            long threadId = SdmmsUtil.getOrCreateThreadId(mContext, recipients);

            values.put(Mms.THREAD_ID, threadId);
        }

        SqliteWrapper.update(mContext, mContentResolver, uri, values, null, null);
    }

    private void updatePart(Uri uri, PduPart part, HashMap<Uri, InputStream> preOpenedFiles)
            throws MmsException {
        ContentValues values = new ContentValues(7);

        int charset = part.getCharset();
        if (charset != 0 ) {
            values.put(Part.CHARSET, charset);
        }

        String contentType = null;
        if (part.getContentType() != null) {
            contentType = toIsoString(part.getContentType());
            values.put(Part.CONTENT_TYPE, contentType);
        } else {
            throw new MmsException("MIME type of the part must be set.");
        }

        if (part.getFilename() != null) {
            String fileName = new String(part.getFilename());
            values.put(Part.FILENAME, fileName);
        }

        if (part.getName() != null) {
            String name = new String(part.getName());
            values.put(Part.NAME, name);
        }

        Object value = null;
        if (part.getContentDisposition() != null) {
            value = toIsoString(part.getContentDisposition());
            values.put(Part.CONTENT_DISPOSITION, (String) value);
        }

        if (part.getContentId() != null) {
            value = toIsoString(part.getContentId());
            values.put(Part.CONTENT_ID, (String) value);
        }

        if (part.getContentLocation() != null) {
            value = toIsoString(part.getContentLocation());
            values.put(Part.CONTENT_LOCATION, (String) value);
        }

        if (SDSmsConsts.DEBUG_DRAFT) Log.i(TAG, "updatePart uri: " + uri.toString());
        SqliteWrapper.update(mContext, mContentResolver, uri, values, null, null);

        // Only update the data when:
        // 1. New binary data supplied or
        // 2. The Uri of the part is different from the current one.
        if ((part.getData() != null)
                || (uri != part.getDataUri())) {
            persistData(part, uri, contentType, preOpenedFiles);
        }
    }

    /**
     * Update all parts of a PDU.
     *
     * @param uri The PDU which need to be updated.
     * @param body New message body of the PDU.
     * @param preOpenedFiles if not null, a map of preopened InputStreams for the parts.
     * @throws MmsException Bad URI or updating failed.
     */
    public void updateParts(Uri uri, PduBody body, HashMap<Uri, InputStream> preOpenedFiles)
            throws MmsException {
        try {
            PduCacheEntry cacheEntry;
            synchronized(PDU_CACHE_INSTANCE) {
                if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                    if (LOCAL_LOGV) {
                        Log.v(TAG, "updateParts: " + uri + " blocked by isUpdating()");
                    }
                    try {
                        PDU_CACHE_INSTANCE.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "updateParts: ", e);
                    }
                    cacheEntry = PDU_CACHE_INSTANCE.get(uri);
                    if (cacheEntry != null) {
                        ((MultimediaMessagePdu) cacheEntry.getPdu()).setBody(body);
                    }
                }
                // Tell the cache to indicate to other callers that this item
                // is currently being updated.
                PDU_CACHE_INSTANCE.setUpdating(uri, true);
            }

            ArrayList<PduPart> toBeCreated = new ArrayList<PduPart>();
            HashMap<Uri, PduPart> toBeUpdated = new HashMap<Uri, PduPart>();

            int partsNum = body.getPartsNum();
            StringBuilder filter = new StringBuilder().append('(');
            for (int i = 0; i < partsNum; i++) {
                PduPart part = body.getPart(i);
                Uri partUri = part.getDataUri();
            	if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "updateParts partsNum: " + partsNum);
            	if (SDSmsConsts.DEBUG_SEND_MMS) {
            		if (part != null) Log.i(TAG, "updateParts part.getDataUri(): " + part.getDataUri());
                	else Log.i(TAG, "updateParts part is null ");
            	}
                //if ((partUri == null) || !partUri.getAuthority().startsWith("mms")) {
                if ((partUri == null) || !partUri.getAuthority().startsWith(SDSmsManager.startsWithMms())) {
                    toBeCreated.add(part);
                } else {
                    toBeUpdated.put(partUri, part);

                    // Don't use 'i > 0' to determine whether we should append
                    // 'AND' since 'i = 0' may be skipped in another branch.
                    if (filter.length() > 1) {
                        filter.append(" AND ");
                    }

                    filter.append(Part._ID);
                    filter.append("!=");
                    DatabaseUtils.appendEscapedSQLString(filter, partUri.getLastPathSegment());
                }
            }
            filter.append(')');

            long msgId = ContentUris.parseId(uri);

            // Remove the parts which doesn't exist anymore.
            SqliteWrapper.delete(mContext, mContentResolver,
                    Uri.parse(SDSmsManager.Mms.CONTENT_URI() + "/" + msgId + "/part"),
                    filter.length() > 2 ? filter.toString() : null, null);

            // Create new parts which didn't exist before.
            for (PduPart part : toBeCreated) {
                if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "updateParts uri is null: " + (uri == null));
            	if (SDSmsConsts.DEBUG_SEND_MMS && uri != null) Log.i(TAG, "updateParts uri: " + uri.toString());
                if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "updateParts part.getDataUri(): " + part.getDataUri());
                persistPart(part, msgId, preOpenedFiles);
                if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "updateParts after persistPart uri is null: " + (uri == null));
            	if (SDSmsConsts.DEBUG_SEND_MMS && uri != null) Log.i(TAG, "updateParts after persistPart uri: " + uri.toString());
                if (SDSmsConsts.DEBUG_SEND_MMS) Log.i(TAG, "updateParts after persistPart part.getDataUri(): " + part.getDataUri());
            }

            // Update the modified parts.
            for (Map.Entry<Uri, PduPart> e : toBeUpdated.entrySet()) {
                updatePart(e.getKey(), e.getValue(), preOpenedFiles);
            }
        } finally {
            synchronized(PDU_CACHE_INSTANCE) {
                PDU_CACHE_INSTANCE.setUpdating(uri, false);
                PDU_CACHE_INSTANCE.notifyAll();
            }
        }
    }

    /**
     * Persist a PDU object to specific location in the storage.
     *
     * @param pdu The PDU object to be stored.
     * @param uri Where to store the given PDU object.
     * @param createThreadId if true, this function may create a thread id for the recipients
     * @param groupMmsEnabled if true, all of the recipients addressed in the PDU will be used
     *  to create the associated thread. When false, only the sender will be used in finding or
     *  creating the appropriate thread or conversation.
     * @param preOpenedFiles if not null, a map of preopened InputStreams for the parts.
     * @return A Uri which can be used to access the stored PDU.
     */

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled,
            HashMap<Uri, InputStream> preOpenedFiles)
            throws MmsException {
        if (uri == null) {
            throw new MmsException("Uri may not be null.");
        }
    	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.i(TAG, "persist uri null: " + (uri == null));
    	if (SDSmsConsts.DEBUG_RECEIVE_MMS && uri != null) Log.i(TAG, "persist uri: " + uri.toString());
        long msgId = -1;
        try {
            msgId = ContentUris.parseId(uri);
        } catch (NumberFormatException e) {
            // the uri ends with "inbox" or something else like that
        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.e(TAG, "persist uri null: " + (uri == null));
        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) e.printStackTrace();
        }
        boolean existingUri = msgId != -1;
        HashMap<Uri, Integer> MESSAGE_BOX_MAP = new HashMap<Uri, Integer>();
        MESSAGE_BOX_MAP.put(SDSmsManager.Mms.Inbox.CONTENT_URI(),  Mms.MESSAGE_BOX_INBOX);
        MESSAGE_BOX_MAP.put(SDSmsManager.Mms.Sent.CONTENT_URI(),   Mms.MESSAGE_BOX_SENT);
        MESSAGE_BOX_MAP.put(SDSmsManager.Mms.Draft.CONTENT_URI(),  Mms.MESSAGE_BOX_DRAFTS);
        MESSAGE_BOX_MAP.put(SDSmsManager.Mms.Outbox.CONTENT_URI(), Mms.MESSAGE_BOX_OUTBOX);

        if (!existingUri && MESSAGE_BOX_MAP.get(uri) == null) {
            throw new MmsException(
                    "Bad destination, must be one of "
	                    //+ "content://mms/inbox, content://mms/sent, "
	                    //+ "content://mms/drafts, content://mms/outbox, "
	                    //+ "content://mms/temp.");
			            + SDSmsManager.contentMms() + "inbox, " + SDSmsManager.contentMms() + "sent, "
			            + SDSmsManager.contentMms() + "drafts, " + SDSmsManager.contentMms() + "outbox, "
			            + SDSmsManager.contentMms() + "temp.");
        }
        synchronized(PDU_CACHE_INSTANCE) {
            // If the cache item is getting updated, wait until it's done updating before
            // purging it.
            if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                if (LOCAL_LOGV) {
                    Log.v(TAG, "persist: " + uri + " blocked by isUpdating()");
                }
                try {
                    PDU_CACHE_INSTANCE.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "persist1: ", e);
                }
            }
        }
        PDU_CACHE_INSTANCE.purge(uri);
    	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.i(TAG, "persist after purge uri null: " + (uri == null));
    	if (SDSmsConsts.DEBUG_RECEIVE_MMS && uri != null) Log.i(TAG, "persist after purge  uri: " + uri.toString());

        PduHeaders header = pdu.getPduHeaders();
        PduBody body = null;
        ContentValues values = new ContentValues();
        Set<Entry<Integer, String>> set;

        set = ENCODED_STRING_COLUMN_NAME_MAP.entrySet();
        for (Entry<Integer, String> e : set) {
            int field = e.getKey();
            EncodedStringValue encodedString = header.getEncodedStringValue(field);
            if (encodedString != null) {
                String charsetColumn = CHARSET_COLUMN_NAME_MAP.get(field);
	            if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ ENCODED_STRING_COLUMN_NAME_MAP " +
	                		" - e.getValue(): " + e.getValue().toString() 
	                		+ " toIsoString(encodedString.getTextString()): " + toIsoString(encodedString.getTextString())
	                		+ " charsetColumn: " + charsetColumn
	                		+ " encodedString.getCharacterSet(): " + encodedString.getCharacterSet()
	                		);
               values.put(e.getValue(), toIsoString(encodedString.getTextString()));
                values.put(charsetColumn, encodedString.getCharacterSet());
            }
        }

        set = TEXT_STRING_COLUMN_NAME_MAP.entrySet();
        for (Entry<Integer, String> e : set){
            byte[] text = header.getTextString(e.getKey());
            if (text != null) {
	            if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ TEXT_STRING_COLUMN_NAME_MAP " +
	                		" - e.getValue(): " + e.getValue().toString() 
	                		+ " toIsoString(text): " + toIsoString(text)
	                		);
                values.put(e.getValue(), toIsoString(text));
            }
        }

        set = OCTET_COLUMN_NAME_MAP.entrySet();
        for (Entry<Integer, String> e : set){
            int b = header.getOctet(e.getKey());
            if (b != 0) {
	            if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ OCTET_COLUMN_NAME_MAP " +
	                		" - e.getValue(): " + e.getValue().toString() 
	                		+ " b: " + b
	                		);
                values.put(e.getValue(), b);
            }
        }

        set = LONG_COLUMN_NAME_MAP.entrySet();
        for (Entry<Integer, String> e : set){
            long l = header.getLongInteger(e.getKey());
            if (l != -1L) {
	            if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ LONG_COLUMN_NAME_MAP " +
	                		" - e.getValue(): " + e.getValue().toString() 
	                		+ " l: " + l
	                		);
                values.put(e.getValue(), l);
            }
        }

        HashMap<Integer, EncodedStringValue[]> addressMap =
                new HashMap<Integer, EncodedStringValue[]>(ADDRESS_FIELDS.length);
        // Save address information.
        for (int addrType : ADDRESS_FIELDS) {
            EncodedStringValue[] array = null;
            if (addrType == PduHeaders.FROM) {
                EncodedStringValue v = header.getEncodedStringValue(addrType);
                if (v != null) {
                    array = new EncodedStringValue[1];
                    array[0] = v;
                }
            } else {
                array = header.getEncodedStringValues(addrType);
            }
            addressMap.put(addrType, array);
            if (array != null) { 
            	for (EncodedStringValue val : array) {
		            if (SDSmsConsts.DEBUG_RECEIVE_MMS)
		                Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ ADDRESS_FIELDS " +
		                		" - addrType: " + addrType 
		                		+ " val.getString(): " + val.getString()
		                		+ " val.toString(): " + val.toString()
		                		);
	            }
            }
        }
        
        HashSet<String> recipients = new HashSet<String>();
        int msgType = pdu.getMessageType();
        // Here we only allocate thread ID for M-Notification.ind,
        // M-Retrieve.conf and M-Send.req.
        // Some of other PDU types may be allocated a thread ID outside
        // this scope.
        if ((msgType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND)
                || (msgType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF)
                || (msgType == PduHeaders.MESSAGE_TYPE_SEND_REQ)) {
            switch (msgType) {
                case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                case PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF:
                    loadRecipients(PduHeaders.FROM, recipients, addressMap, false);

                    // For received messages when group MMS is enabled, we want to associate this
                    // message with the thread composed of all the recipients -- all but our own
                    // number, that is. This includes the person who sent the
                    // message or the FROM field (above) in addition to the other people the message
                    // was addressed to or the TO field. Our own number is in that TO field and
                    // we have to ignore it in loadRecipients.
                    if (groupMmsEnabled) {
                        loadRecipients(PduHeaders.TO, recipients, addressMap, true);
                    }
		            if (SDSmsConsts.DEBUG_RECEIVE_MMS)
		            	for (String name : recipients) {
			                Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ recipients " + name);
		            	}
                    break;
                case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                    loadRecipients(PduHeaders.TO, recipients, addressMap, false);
                    break;
            }
            long threadId = 0;
            if (SDSmsConsts.DEBUG_RECEIVE_MMS)
                Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ createThreadId " +
                		" - threadId: " + threadId 
                		+ " createThreadId: " + createThreadId
                		+ " recipients.isEmpty(): " + recipients.isEmpty()
                		);
            if (createThreadId && !recipients.isEmpty()) {
                // Given all the recipients associated with this message, find (or create) the
                // correct thread.
            	//TODO
                //threadId = Threads.getOrCreateThreadId(mContext, recipients);
                threadId = SdmmsUtil.getOrCreateThreadId(mContext, recipients);
	            if (SDSmsConsts.DEBUG_RECEIVE_MMS)
	                Log.i(TAG, "MMS PDU INFO _+_+_+_+_+_+_+_+_+_+_+_+_+_+_ createThreadId " +
	                		" - threadId: " + threadId 
	                		);
	        	// fix for MMS bug #65843 on Samsung
	        	// 7/1/2014 - do not know what all of this does or means...
	        	// but "address" cannot be "null" - possibly related to null octets
	        	final long token = Binder.clearCallingIdentity();
	        	String address = values.getAsString(CanonicalAddressesColumns.ADDRESS);
	        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.w(TAG, "___________________ MMS address: " + address + "___________________");
	        	if (address == null || address.length() < 1) {
	        		address = SdmmsUtil.getOrCreateThreadIdFix(mContext, recipients)+"";
		        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.w(TAG, "___________________ getOrCreateThreadIdFix MMS address: " + address + "___________________");
	                try {
	                    values.put(Mms.THREAD_ID, address);
	                } finally {
	                    Binder.restoreCallingIdentity(token);
	                }
	        	}
	        	address = values.getAsString(CanonicalAddressesColumns.ADDRESS);

	        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.w(TAG, "___________________ MMS CanonicalAddressesColumns address: " + address + "___________________");
	        	if (address == null && msgId >= 0) {
	        		long tempId = getThreadId(msgId, header);
		        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.w(TAG, "___________________ CanonicalAddressesColumns MMS address: " + address + "___________________");
	        		if (tempId >= 0) {
	    	    		address = tempId+"";
	    	    		values.put(Mms.THREAD_ID, address);
	        		}
	        	}
	        	
	        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.w(TAG, "___________________ MMS getThreadId address: " + address + "___________________");
	        	if (address == null || address.length() < 1) {
	        		long tempId = findThreadId(mContext, pdu, msgType);
		        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.w(TAG, "___________________ findThreadId MMS address: " + address + "___________________");
	        		if (tempId >= 0) {
	        			address = tempId+"";
	        			values.put(Mms.THREAD_ID, address);
	        		}
	        	}
	            
	        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.w(TAG, "___________________ MMS findThreadId address: " + address + "___________________");
	        	if (address == null || address.length() < 1) {
	        		address = msgId+"";
	                try {
	    	        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.w(TAG, "___________________ MMS SdmmsUtil.getOrCreateThreadId(mContext, address): " + SdmmsUtil.getOrCreateThreadId(mContext, address) + "___________________");
	                    values.put(Mms.THREAD_ID, SdmmsUtil.getOrCreateThreadId(mContext, address));
	                } finally {
	                    Binder.restoreCallingIdentity(token);
	                }
	        	}
	        	address = values.getAsString(CanonicalAddressesColumns.ADDRESS);
	        	if (address == null || address.length() < 1) {
	        		address = msgId+"";
	                try {
	    	        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.w(TAG, "___________________ MMS findThreadId address: " + address + "___________________");
	                    values.put(Mms.THREAD_ID, SdmmsUtil.getOrCreateThreadIdNew(mContext, address));
	                } finally {
	                    Binder.restoreCallingIdentity(token);
	                }
	        	}
	        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.w(TAG, "___________________ MMS address: " + address + "___________________");
            }
        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.w(TAG, "___________________ MMS threadId: " + threadId + "___________________");
            if (threadId > 0) values.put(Mms.THREAD_ID, threadId);
        }

    	if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, "persist before get dummy uri null: " + (uri == null));
    	if (SDSmsConsts.DEBUG_UTILS && uri != null) Log.i(TAG, "persist before get dummy uri: " + uri.toString());
        // Save parts first to avoid inconsistent message is loaded
        // while saving the parts.
        long dummyId = System.currentTimeMillis(); // Dummy ID of the msg.

        // Figure out if this PDU is a text-only message
        boolean textOnly = true;

        // Get body if the PDU is a RetrieveConf or SendReq.
        if (pdu instanceof MultimediaMessagePdu) {
            body = ((MultimediaMessagePdu) pdu).getBody();
            // Start saving parts if necessary.
            if (body != null) {
                int partsNum = body.getPartsNum();
                if (partsNum > 2) {
                    // For a text-only message there will be two parts: 1-the SMIL, 2-the text.
                    // Down a few lines below we're checking to make sure we've only got SMIL or
                    // text. We also have to check then we don't have more than two parts.
                    // Otherwise, a slideshow with two text slides would be marked as textOnly.
                    textOnly = false;
                }
                for (int i = 0; i < partsNum; i++) {
                    PduPart part = body.getPart(i);
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.i(TAG, "persist uri is null: " + (uri == null));
                	if (SDSmsConsts.DEBUG_RECEIVE_MMS && uri != null) Log.i(TAG, "persist uri: " + uri.toString());
                    if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.i(TAG, "persistData part.getDataUri(): " + part.getDataUri());
                    persistPart(part, dummyId, preOpenedFiles);

                    // If we've got anything besides text/plain or SMIL part, then we've got
                    // an mms message with some other type of attachment.
                    String contentType = getPartContentType(part);
                    if (contentType != null && !ContentType.APP_SMIL.equals(contentType)
                            && !ContentType.TEXT_PLAIN.equals(contentType)) {
                        textOnly = false;
                    }
                }
            }
        }
        // Record whether this mms message is a simple plain text or not. This is a hint for the
        // UI.
        if (SDSmsManager.hasKitKat()) values.put(Mms.TEXT_ONLY, textOnly ? 1 : 0);
    	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.i(TAG, "persist after text only uri null: " + (uri == null));
    	if (SDSmsConsts.DEBUG_RECEIVE_MMS && uri != null) Log.i(TAG, "persist after text only uri: " + uri.toString());

        Uri res = null;
        if (existingUri) {
            res = uri;
            SqliteWrapper.update(mContext, mContentResolver, res, values, null, null);
        	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.i(TAG, "SqliteWrapper.update 1432 res null: " + (res == null));
        	if (SDSmsConsts.DEBUG_RECEIVE_MMS && res != null) Log.i(TAG, "SqliteWrapper.update 1432 res: " + res.toString());
        } else {
            res = SqliteWrapper.insert(mContext, mContentResolver, uri, values);
            if (res == null) {
                throw new MmsException("persist() failed: return null.");
            }
            // Get the real ID of the PDU and update all parts which were
            // saved with the dummy ID.
            msgId = ContentUris.parseId(res);
        }
    	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.i(TAG, "persist post-update res null: " + (res == null));
    	if (SDSmsConsts.DEBUG_RECEIVE_MMS && uri != null) Log.i(TAG, "persist post-update res: " + res.toString());
    	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.i(TAG, "persist post-update uri null: " + (uri == null));
    	if (SDSmsConsts.DEBUG_RECEIVE_MMS && uri != null) Log.i(TAG, "persist post-update uri: " + uri.toString());

        values = new ContentValues(1);
        values.put(Part.MSG_ID, msgId);
        SqliteWrapper.update(mContext, mContentResolver,
        					//Uri.parse("content://mms/" + dummyId + "/part"),
        					Uri.parse(SDSmsManager.contentMms() + dummyId + "/part"),
                             values, null, null);
        // We should return the longest URI of the persisted PDU, for
        // example, if input URI is "content://mms/inbox" and the _ID of
        // persisted PDU is '8', we should return "content://mms/inbox/8"
        // instead of "content://mms/8".
        // FIXME: Should the MmsProvider be responsible for this???
        if (!existingUri) {
            res = Uri.parse(uri + "/" + msgId);
        }

    	if (SDSmsConsts.DEBUG_RECEIVE_MMS) Log.i(TAG, "SqliteWrapper.update 1446 res null: " + (res == null));
    	if (SDSmsConsts.DEBUG_RECEIVE_MMS && res != null) Log.i(TAG, "SqliteWrapper.update 1446 res: " + res.toString());
        // Save address information.
        for (int addrType : ADDRESS_FIELDS) {
            EncodedStringValue[] array = addressMap.get(addrType);
            if (array != null) {
                persistAddress(msgId, addrType, array);
            }
        }

        return res;
    }

    /**
     * For a given address type, extract the recipients from the headers.
     *
     * @param addressType can be PduHeaders.FROM or PduHeaders.TO
     * @param recipients a HashSet that is loaded with the recipients from the FROM or TO headers
     * @param addressMap a HashMap of the addresses from the ADDRESS_FIELDS header
     * @param excludeMyNumber if true, the number of this phone will be excluded from recipients
     */
    private void loadRecipients(int addressType, HashSet<String> recipients,
            HashMap<Integer, EncodedStringValue[]> addressMap, boolean excludeMyNumber) {
        EncodedStringValue[] array = addressMap.get(addressType);
        if (array == null) {
            return;
        }

        // If the TO recipients is only a single address, then we can skip loadRecipients when
        // we're excluding our own number because we know that address is our own.
        if (excludeMyNumber && array.length == 1) {
            return;
        }
        String myNumber = excludeMyNumber ? mTelephonyManager.getLine1Number() : null;
        for (EncodedStringValue v : array) {
            if (v != null) {
                String number = v.getString();
                if ((myNumber == null || !PhoneNumberUtils.compare(number, myNumber)) &&
                        !recipients.contains(number)) {
                    // Only add numbers which aren't my own number.
                    recipients.add(number);
                }
            }
        }
    }

    /**
     * Move a PDU object from one location to another.
     *
     * @param from Specify the PDU object to be moved.
     * @param to The destination location, should be one of the following:
     *        "content://mms/inbox", "content://mms/sent",
     *        "content://mms/drafts", "content://mms/outbox",
     *        "content://mms/trash".
     * @return New Uri of the moved PDU.
     * @throws MmsException Error occurred while moving the message.
     */
    public Uri move(Uri from, Uri to) throws MmsException {
        // Check whether the 'msgId' has been assigned a valid value.
        long msgId = ContentUris.parseId(from);
        if (msgId == -1L) {
            throw new MmsException("Error! ID of the message: -1.");
        }
        HashMap<Uri, Integer> MESSAGE_BOX_MAP = new HashMap<Uri, Integer>();
        MESSAGE_BOX_MAP.put(SDSmsManager.Mms.Inbox.CONTENT_URI(),  Mms.MESSAGE_BOX_INBOX);
        MESSAGE_BOX_MAP.put(SDSmsManager.Mms.Sent.CONTENT_URI(),   Mms.MESSAGE_BOX_SENT);
        MESSAGE_BOX_MAP.put(SDSmsManager.Mms.Draft.CONTENT_URI(),  Mms.MESSAGE_BOX_DRAFTS);
        MESSAGE_BOX_MAP.put(SDSmsManager.Mms.Outbox.CONTENT_URI(), Mms.MESSAGE_BOX_OUTBOX);

        // Get corresponding int value of destination box.
        Integer msgBox = MESSAGE_BOX_MAP.get(to);
        if (msgBox == null) {
            throw new MmsException(
                    "Bad destination, must be one of "
                    //+ "content://mms/inbox, content://mms/sent, "
                    //+ "content://mms/drafts, content://mms/outbox, "
                    //+ "content://mms/temp.");
		            + SDSmsManager.contentMms() + "inbox, " + SDSmsManager.contentMms() + "sent, "
		            + SDSmsManager.contentMms() + "drafts, " + SDSmsManager.contentMms() + "outbox, "
		            + SDSmsManager.contentMms() + "temp.");
        }

        ContentValues values = new ContentValues(1);
        values.put(Mms.MESSAGE_BOX, msgBox);
        SqliteWrapper.update(mContext, mContentResolver, from, values, null, null);
        return ContentUris.withAppendedId(to, msgId);
    }

    /**
     * Wrap a byte[] into a String.
     */
    public static String toIsoString(byte[] bytes) {
        try {
            return new String(bytes, CharacterSets.MIMENAME_ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            // Impossible to reach here!
            Log.e(TAG, "ISO_8859_1 must be supported!", e);
            return "";
        }
    }

    /**
     * Unpack a given String into a byte[].
     */
    public static byte[] getBytes(String data) {
        try {
            return data.getBytes(CharacterSets.MIMENAME_ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            // Impossible to reach here!
            Log.e(TAG, "ISO_8859_1 must be supported!", e);
            return new byte[0];
        }
    }

    /**
     * Remove all objects in the temporary path.
     */
    public void release() {
        //Uri uri = Uri.parse(TEMPORARY_DRM_OBJECT_URI);
        Uri uri = Uri.parse(SDSmsManager.contentMms() + Long.MAX_VALUE + "/part");
        SqliteWrapper.delete(mContext, mContentResolver, uri, null, null);
    }

    /**
     * Find all messages to be sent or downloaded before certain time.
     */
    public Cursor getPendingMessages(long dueTime) {
        Uri.Builder uriBuilder = SDSmsManager.MmsSms.PendingMessages.CONTENT_URI().buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");

        String selection = PendingMessages.ERROR_TYPE + " < ?"
                + " AND " + PendingMessages.DUE_TIME + " <= ?";

        String[] selectionArgs = new String[] {
                String.valueOf(MmsSms.ERR_TYPE_GENERIC_PERMANENT),
                String.valueOf(dueTime)
        };

        return SqliteWrapper.query(mContext, mContentResolver,
                uriBuilder.build(), null, selection, selectionArgs,
                PendingMessages.DUE_TIME);
    }
}
