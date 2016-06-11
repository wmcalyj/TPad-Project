package com.example.hapticebook.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.hapticebook.config.Configuration;

public class LogService {
	private static volatile FileWriter fw = null;
	private static final String SEPARATOR = ";";
	private static final String root = Configuration.ROOT_PATH + "/hapticEBook/";
	private static final File dir = new File(root, "logs");

	public static void WriteToLog(Action action, String details) {
		if (fw == null) {
			try {
				fw = new FileWriter(createFile(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (fw != null) {
			String msg = processAction(action);
			try {
				fw.write(timestamp() + SEPARATOR + msg + SEPARATOR + details + "\r\n");
				fw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (action == Action.CLOSE) {
			StopWriting();
		}
	}

	private static String processAction(Action action) {
		switch (action) {
		case OPEN:
			return "Start playing with HapticEBook";
		case CLOSE:
			return "Stop playing with HapticEBook";
		case NEXT_PAGE:
			return "Move to next page";
		case PREVIOUS_PAGE:
			return "Move back to previous page";
		case ADD_PAGE:
			return "Add a new page";
		case CANCEL_AUDIO_SAVE:
			return "Cancel saving audio file";
		case CANCEL_EDIT:
			return "Cancel editing page";
		case CANCEL_FILTER_SAVE:
			return "Cancel choosing new filter";
		case DELETE:
			return "Delete page";
		case EDIT:
			return "Edit page";
		case FEEL:
			return "Feel page";
		case HAPTIC_BROWSING:
			return "Browsing haptic";
		case PLAY_AUDIO:
			return "Start playing audio file";
		case STOP_AUDIO:
			return "Stop playing audio file";
		case RECORD_AUDIO:
			return "Start recording audio file";
		case STOP_RECORD_AUDIO:
			return "Stop recording audio file";
		case SAVE_AUDIO:
			return "Save audio file";
		case SAVE_FILTER:
			return "Save filter";
		case SAVE_EDIT:
			return "Save edited page";
		default:
			return "Action cannot be recognized: " + action.toString() + "\r\n";
		}
	}

	private static void StopWriting() {
		if (fw != null) {
			try {
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fw = null;
		}
	}

	private static String createFileName() {
		StringBuilder sb = new StringBuilder(timestamp());
		sb.append("-log.txt");
		return sb.toString();
	}

	private static File createFile() {

		if (!dir.exists()) {
			dir.mkdirs();
		}

		File saveFile = new File(dir, createFileName());

		try {
			fw = new FileWriter(saveFile, true);
			fw.write("File Start\r\n");
			fw.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		return saveFile;
	}

	private static String timestamp() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy_h:mm:ss:SSS");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
}
