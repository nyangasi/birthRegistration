package com.sdmmllc.superdupersmsmanager.mms.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import android.util.Log;

class StringComparator
		implements
			Comparator<String>,
			Serializable
		{
	
	private static final long serialVersionUID = 5603928894805771653L;
	
	static public final StringComparator Default = new StringComparator();

	/**
	 * Compares two strings.
	 **/
	public int compare(
			final String pString1,
			final String pString2) {
		
		if (pString1 == pString2)
			return 0;
		
		if (pString1 == null)
			return Integer.MIN_VALUE;
		
		if (pString2 == null)
			return Integer.MAX_VALUE;
		
		if (pString1.equals(pString2))
			return 0;
		
		final int aS1_Length = pString1.length();
		final int aS2_Length = pString2.length();
		final int aMinLength = Math.min(aS1_Length, aS2_Length);
		for(int i = 0; i < aMinLength; i++) {
			char aC1 = pString1.charAt(i);
			char aC2 = pString2.charAt(i);
			
			if (aC1 != aC2)
				return aC1 - aC2;
		}
		
		final int aLenghtDiffer = aS1_Length - aS2_Length;
		return aLenghtDiffer;
	}

}

class ClassComparator
		implements
			Comparator<Class<?>>,
			Serializable
		{
	
	private static final long serialVersionUID = -7120534646583185358L;
	
	static public final ClassComparator Default = new ClassComparator();
		
	/** Compares two classes. */
	public int compare(
			final Class<?> pClass1,
			final Class<?> pClass2) {
		
		if (pClass1 == pClass2)
			return 0;
		
		if (pClass1 == null)
			return Integer.MIN_VALUE;
		
		if (pClass2 == null)
			return Integer.MAX_VALUE;
		
		final String aC1_Name      = pClass1.getCanonicalName();
		final String aC2_Name      = pClass2.getCanonicalName();
		final int    aNameCompared = StringComparator.Default.compare(aC1_Name, aC2_Name);
		return aNameCompared;
	}

}

public class ClassDiscovery {

	static private String GetClassName_afterPackageAsPath(
			final String pFileName,
			final String pPkgAsPath) {
		final String  CName                    = pFileName.substring(0, pFileName.length() - 6).replace('/', '.').replace('\\', '.');
		final String  CName_AfterPackageAsPath = CName.substring(pPkgAsPath.length());
		return CName_AfterPackageAsPath;
	}
	static private String GetClassName_ofPackageAsPath(
			final String pFileName,
			final String pPkgAsPath) {
		
		final boolean aIsClass = pFileName.endsWith(".class");
		if (!aIsClass)
			return null;

		final boolean aIsBelongToPackage = pFileName.startsWith(pPkgAsPath);
		if (!aIsBelongToPackage)
			return null;

		final String  aClassName = ClassDiscovery.GetClassName_afterPackageAsPath(pFileName, pPkgAsPath);
		return aClassName;
	}

	static private File GetPackageFile(
			final String pPkgName,
			final File   pPkgPath) {
		
		final String aPkgFileName = pPkgPath.getAbsoluteFile().toString() + '/' + pPkgName.replace('.', '/');
		final File   aPkgFile     = new File(aPkgFileName);
		
		final boolean aIsExist             = aPkgFile.exists();
		final boolean aIsDirectory         = aPkgFile.isDirectory();
		final boolean aIsExist_asDirectory = aIsExist && aIsDirectory;
		if(!aIsExist_asDirectory)
			return null;
		
		return aPkgFile;
	}
	static private boolean Check_isJarFile(final File pFile) {
		final boolean aIsJarFile = pFile.toString().endsWith(".jar");
		return aIsJarFile;
	}
	
	static private ArrayList<String> DiscoverClassNames_fromJarFile(final PkgInfo pPkgInfo) {
		
		final ArrayList<String> aClassNames = new ArrayList<String>();
		try {
			final JarFile               JF  = new JarFile(pPkgInfo.PkgPath);
			final Enumeration<JarEntry> JEs = JF.entries();
			
			while (JEs.hasMoreElements()) {
				final JarEntry aJE     = JEs.nextElement();
				final String   aJEName = aJE.getName();
				
				final String aSimpleName = GetClassName_ofPackageAsPath(aJEName, pPkgInfo.PkgAsPath);
				if (aSimpleName == null)
					continue;
				
				final String aClassName  = pPkgInfo.PkgName + '.' + aSimpleName;
				aClassNames.add(aClassName);
			}
			
			JF.close();
		} catch (IOException e) {}
		
		return aClassNames;
	}
	static private void DiscoverClassNames_fromDirectory(
			final String            pAbsolutePackagePath,
			final String            pPackageName,
			final File              pPackageFolder,
			final ArrayList<String> pClassNames) {
		final File[] aFiles = pPackageFolder.listFiles();
		for(File aFile : aFiles) {
			if (aFile.isDirectory()) {
				DiscoverClassNames_fromDirectory(pAbsolutePackagePath, pPackageName, aFile, pClassNames);
				continue;
			}
		
			final String  aFileName    = aFile.getAbsolutePath().substring(pAbsolutePackagePath.length() + 1);
			final boolean aIsClassFile = aFileName.endsWith(".class");
			if (!aIsClassFile)
				continue;

			final String aSimpleName = aFileName.substring(0, aFileName.length() - 6).replace('/', '.').replace('\\', '.');
			final String aClassName  = pPackageName + '.' + aSimpleName;
			pClassNames.add(aClassName);
		}
	}
	static private ArrayList<String> DiscoverClassNames_fromDirectory(PkgInfo pPkgInfo) {

		final ArrayList<String> aClassNames = new ArrayList<String>();
		final File aPkgFile = ClassDiscovery.GetPackageFile(pPkgInfo.PkgName, pPkgInfo.PkgPath);
		if (aPkgFile == null)
			return aClassNames;
		
		DiscoverClassNames_fromDirectory(aPkgFile.getAbsolutePath(), pPkgInfo.PkgName, aPkgFile, aClassNames);
		return aClassNames;
	}
	
	static public class PkgInfo {
		PkgInfo(
				final File   pPkgPath,
				final String pPkgName,
				final String pPkgAsPath) {
			
			this.PkgPath   = pPkgPath;
			this.PkgName   = pPkgName;
			this.PkgAsPath = pPkgAsPath;
		}
		final File   PkgPath;
		final String PkgName;
		final String PkgAsPath;
	}
	static public PkgInfo GetPackageInfoOf(Class<?> pClass) {
		File   aPkgPath   = null;
		String aPkgName   = null;
		String aPkgAsPath = null;
		
		try {
			aPkgPath   = new File(pClass.getProtectionDomain().getCodeSource().getLocation().toURI());
			aPkgName   = pClass.getPackage().getName();
			aPkgAsPath = aPkgName.replace('.', '/') + '/';
		}
		catch (Throwable e) { }

		if (aPkgPath == null)
			return null;
		
		final PkgInfo aPkgInfo = new PkgInfo(aPkgPath, aPkgName, aPkgAsPath);
		return aPkgInfo;
	}
	static public ArrayList<String> DiscoverClassNames_inPackage(final PkgInfo pPkgInfo) {

		if (pPkgInfo == null)
			return null;

		ArrayList<String> aClassNames = new ArrayList<String>();
		if (pPkgInfo.PkgPath.isDirectory()) {
			
			aClassNames = ClassDiscovery.DiscoverClassNames_fromDirectory(pPkgInfo);
			
		} else if(pPkgInfo.PkgPath.isFile()) {
			boolean aIsJarFile = ClassDiscovery.Check_isJarFile(pPkgInfo.PkgPath);
			if (!aIsJarFile)
				return null;
			
			aClassNames = ClassDiscovery.DiscoverClassNames_fromJarFile(pPkgInfo);
		}
		
		return aClassNames;
	} 
	
	/**
	 * Returns an array of class in the same package as the the SeedClass
	 * 
	 * @param pFilterName     - Regular expression to match the desired classes' name (nullif no filtering needed)
	 * @param pFilterClass   - The super class of the desired classes (null if no filtering needed)
	 * 
	 * @return                - The array of matched classes, null if there is a problem.
	 * 
	 * @author The rest       - Nawaman  http://nawaman.net
	 * @author Package as Dir - Jon Peck http://jonpeck.com
	 *                              (adapted from http://www.javaworld.com/javaworld/javatips/jw-javatip113.html)
	 */
	@SuppressWarnings("unchecked")
	static <T> Class<? extends T>[] DiscoverClasses(
			final Class<?> pSeedClass,
			final String   pFilterName,
			final Class<T> pFilterClass) {

		final Pattern aClsNamePattern = (pFilterName == null) ? null : Pattern.compile(pFilterName);
		
		PkgInfo aPkgInfo = null;

		try { aPkgInfo = GetPackageInfoOf(pSeedClass); }
		catch (Throwable e) {}

		if (aPkgInfo == null)
			return null;
		
		final ArrayList<String> aClassNames = DiscoverClassNames_inPackage(aPkgInfo);
		
		if (aClassNames == null)
			return null;
		
		if (aClassNames.size() == 0)
			return null;
		
		final ArrayList<Class<?>> aClasses = new ArrayList<Class<?>>();
		for(final String aClassName : aClassNames) {
			
			if ((aClsNamePattern != null) && !aClsNamePattern.matcher(aClassName).matches())
				continue;

			// Get the class and filter it
			Class<?> aClass = null;
			try {
				aClass = Class.forName(aClassName);
			} catch (ClassNotFoundException e) {
				continue;
			} catch (NoClassDefFoundError e) {
				continue;
			}
			
			if((pFilterClass != null) && !pFilterClass.isAssignableFrom(aClass))
				continue;

			if (pFilterClass != null) {
				if (!pFilterClass.isAssignableFrom(aClass))
					continue;
					
				aClasses.add(aClass.asSubclass(pFilterClass));
			} else {
				aClasses.add(aClass);
			}
		}

		Collections.sort(aClasses, ClassComparator.Default);
		Class<? extends T>[] aClassesArray = aClasses.toArray((Class<? extends T>[]) (new Class[aClasses.size()]));

		return aClassesArray;
	}
	
	static public void discover(String TAG, String pArgs) {
		Class<?> aSeedClass = ClassDiscovery.class;
		try { aSeedClass = Class.forName(pArgs); }
		catch(Exception E) {}
		
		if (aSeedClass == null)
			aSeedClass = ClassDiscovery.class;
		
		final Class<?>[] aClasses =	DiscoverClasses(aSeedClass, null, null);
		
		Log.i("ClassDiscovery", "ClassDiscovery results:");
		Log.i(TAG, "[");
		if (aClasses != null) {
			for (Class aClass : aClasses)
				Log.i(TAG, "\t" + aClass);
		}
		Log.i(TAG, "]");
	}

}