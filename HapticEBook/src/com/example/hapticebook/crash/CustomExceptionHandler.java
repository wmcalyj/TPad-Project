package com.example.hapticebook.crash;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.hapticebook.config.Configuration;

public class CustomExceptionHandler implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler defaultUEH;

	private String localPath;

	private static final String root = Configuration.ROOT_PATH + "/hapticEBook/";
	private static final File dir = new File(root + "reports");

	/*
	 * if any of the parameters is null, the respective functionality will not
	 * be used
	 */
	public CustomExceptionHandler() {
		if (!dir.exists()) {
			dir.mkdirs();
		}
		localPath = dir.getAbsolutePath();
		this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		String stacktrace = result.toString();
		printWriter.close();
		String filename = timeStamp + "-report.txt";

		if (localPath != null) {
			writeToFile(stacktrace, filename);
		}

		defaultUEH.uncaughtException(t, e);
	}

	private void writeToFile(String stacktrace, String filename) {
		try {
			BufferedWriter bos = new BufferedWriter(new FileWriter(localPath + "/" + filename));
			bos.write(stacktrace);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
