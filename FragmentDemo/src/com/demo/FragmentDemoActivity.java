package com.demo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class FragmentDemoActivity extends FragmentActivity {
	private final static String TAG = "FragmentDemoActivity";
	private final static int PROGRESS_DIALOG = 1;
	private final static int CONTENT_VIEW_ID = 10101010;
	private final static String APP_DIRECTORY = "fragmentdemo";
	private ProgressDialog progressDialog;
	private FragmentClassLoader fragmentClassLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FrameLayout frame = new FrameLayout(this);
		frame.setId(CONTENT_VIEW_ID);

		this.setContentView(frame, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		this.fragmentClassLoader = new FragmentClassLoader(this);

		// We only want to load a new Fragment if the Activity
		// is built from scratch.
		if (savedInstanceState == null) {
			new FragmentInstaller().install(
					"http://upload.visusnet.de/ECi/fragment.zip",
					"com.demo.DemoFragment");
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Downloading Fragment...");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(100);
			return progressDialog;
		default:
			return null;
		}
	}

	private class FragmentInstaller extends FileDownloader {
		private String className;

		public void install(String jarUrl, String className) {
			this.className = className;
			String fragmentDirectory = Environment
					.getExternalStorageDirectory() + "/" + APP_DIRECTORY;
			this.execute(jarUrl, fragmentDirectory, className);
		}

		@Override
		protected void onPreExecute() {
			showDialog(PROGRESS_DIALOG);
		}

		@Override
		protected void onProgressUpdate(final Integer... values) {
			progressDialog.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(String jarPath) {
			dismissDialog(PROGRESS_DIALOG);

			if (jarPath == null) {
				return;
			}

			try {
				Class<Fragment> fragmentClass = fragmentClassLoader
						.loadFragmentClass(jarPath, this.className);

				Fragment fragment = fragmentClass.newInstance();

				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.add(CONTENT_VIEW_ID, fragment).commit();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}
}