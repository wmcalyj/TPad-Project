package com.example.hapticebook.config;

import android.os.Environment;

public class Configuration {
	public static final boolean DEBUG = false;
	public static final int COMPRESSIONRATE = 100;// 100 means no compression
	// Where to save all the images, recordings, logs and crash reports
	public static final String ROOT_PATH = Environment.getExternalStorageDirectory().toString();

	public static final boolean USE_GIVEN_INSAMPLESIZE = true;
	public static final int INSAMPLESIZE = 4;

	public class FilterValue {
		public static final int WOODCUT = 41;
		public static final int CANNY = 3;
		public static final int NOISE = 5;
	}

}
