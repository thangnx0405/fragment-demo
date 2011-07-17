package com.demo;

import java.io.File;
import java.util.Hashtable;

import android.content.Context;
import android.support.v4.app.Fragment;
import dalvik.system.DexClassLoader;

public class FragmentClassLoader {
	private Context context;
	private Hashtable<String, Class<Fragment>> cachedClasses;

	public FragmentClassLoader(Context context) {
		this.context = context;
		this.cachedClasses = new Hashtable<String, Class<Fragment>>();
	}

	// We're checking whether the loaded class is an instance of
	// Fragment, so we can just ignore the warning.
	@SuppressWarnings("unchecked")
	public synchronized Class<Fragment> loadFragmentClass(String jarPath,
			String className) throws ClassNotFoundException, ClassCastException {
		// We don't want to do load files twice, so we cache classes.
		if (this.cachedClasses.containsKey(className)) {
			return this.cachedClasses.get(className);
		}

		// We need a directory to cache optimized classes.
		File dexOutputDir = this.context.getDir("dex", 0);

		// The DexClassLoader will do the magic for us.
		DexClassLoader dexClassLoader = new DexClassLoader(jarPath,
				dexOutputDir.getAbsolutePath(), null, this.getClass()
						.getClassLoader());

		Class<?> loadedClass = (Class<?>) dexClassLoader.loadClass(className);

		// We have to check whether our class inherits from Fragment.
		if (!loadedClass.getSuperclass().equals(Fragment.class)) {
			throw new ClassCastException("Cannot cast " + className
					+ " to Fragment.");
		}

		Class<Fragment> fragmentClass = (Class<Fragment>) loadedClass;

		// Add the class to our cache.
		this.cachedClasses.put(className, fragmentClass);

		return fragmentClass;
	}
}
