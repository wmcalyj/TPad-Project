package com.example.hapticebook.config;

import android.os.Environment;

public class Configuration {
	public static final boolean DEBUG = true;
	public static final int COMPRESSIONRATE = 100;// 100 means no compression
	// Where to save all the images, recordings, logs and crash reports
	public static final String ROOT_PATH = Environment.getExternalStorageDirectory().toString();

}
