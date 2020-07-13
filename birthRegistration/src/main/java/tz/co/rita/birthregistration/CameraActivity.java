/*
 * Copyright (C) 2015 UNICEF Tanzania.
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

package tz.co.rita.birthregistration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The camera activity class.
 * <p>
 *
 * @author Molalgne Girmaw, molbill@gmail.com
 *
 * @version 1.0.0
 * @since June, 2015
 *
 *
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements Camera.PictureCallback {

    public static final String FORM_IMAGE_PATH = "form_image_path";

    private final String TAG = getClass().getSimpleName();
    private static final int CAMERA_CAPTURE_QUALITY = 100;
    private static final int IMAGE_SAVE_QUALITY = 100;
    private static final int IMAGE_WIDTH = 960;
    private static final String IMAGE_EXTENSION = "webp";
    public static final Bitmap.CompressFormat IMAGE_MIME_TYPE = Bitmap.CompressFormat.WEBP;
    public static final int IMAGE_BASE64_ENCODING_QUALITY = 5;

    private Camera mCamera;
    private CameraPreview mPreview;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int DEFAULT_CAMERA = 0;
    private Camera.PictureCallback mPicture = null;
    private int mCameraId = DEFAULT_CAMERA;
    private byte[] mPictureData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mPicture = this;

        // Create an instance of Camera
        mCamera = getCameraInstance();
        setCameraParams();

        // Add a listener to the Capture button
        ImageButton captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
        mPreview = new CameraPreview(this, mCamera, mCameraId);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

    }


    public static Bitmap getScaledBitmap(byte[] imageAsBytes, int reqWidth, boolean grayScale) {
        Bitmap b = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
        int bWidth = b.getWidth();
        int bHeight = b.getHeight();
        float parentRatio = (float) reqWidth / bWidth;

        int nWidth = reqWidth;
        int nHeight = (int) (bHeight * parentRatio);

        Bitmap bmp = Bitmap.createScaledBitmap(b, nWidth, nHeight, true);

        return grayScale ? toGrayscale(bmp) : bmp;
    }



    private void setCameraParams() {
        // get Camera parameters
        Camera.Parameters params = mCamera.getParameters();
        params.setJpegQuality(CAMERA_CAPTURE_QUALITY);
        // params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
//        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//        params.setAutoWhiteBalanceLock(false);
        mCamera.setParameters(params);
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        mPictureData = data;
        findViewById(R.id.button_accept).setVisibility(View.VISIBLE);
        findViewById(R.id.button_discard).setVisibility(View.VISIBLE);
        findViewById(R.id.button_capture).setVisibility(View.INVISIBLE);

    }


    public void onCancelButtonClick(View v) {
        mCamera.stopPreview();
        mCamera.release();
        finish();
    }

    public void onDiscardButtonClick(View v) {
        mPictureData = null;
        mPreview.resetPreview();
        findViewById(R.id.button_accept).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_discard).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_capture).setVisibility(View.VISIBLE);
    }

    public void onAcceptButtonClick(View v) {

        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE, IMAGE_EXTENSION);
        String filePath = pictureFile.getPath();
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            Bitmap bm = getScaledBitmap(mPictureData, IMAGE_WIDTH, true);
            bm.compress(IMAGE_MIME_TYPE, IMAGE_SAVE_QUALITY, fos);
            //  fos.write(mPictureData);
            fos.close();
            Log.d(TAG, "Picture Saved");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

        Intent resultIntent = this.getIntent();
        resultIntent.putExtra(FORM_IMAGE_PATH, filePath);
        setResult(RESULT_OK, resultIntent);

        mCamera.stopPreview();
        mCamera.release();

        finish();

    }

    /**
     * Returns an instance of the Camera object or null if the device camera is
     * busy or unavailable.
     *
     * @return an instance of the Camera object or null.
     */

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(DEFAULT_CAMERA); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type, String ext) {
        return Uri.fromFile(getOutputMediaFile(type, ext));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type, String ext) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Rejesta");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Rejesta", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + "." + ext);
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + "." + ext);
        } else {
            return null;
        }

        return mediaFile;
    }
}