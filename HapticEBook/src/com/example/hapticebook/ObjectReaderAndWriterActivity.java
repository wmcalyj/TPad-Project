package com.example.hapticebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.List;

import android.app.Activity;
import android.util.Log;

import com.example.hapticebook.data.book.impl.Book;
import com.example.hapticebook.data.book.impl.PageImpl;

public class ObjectReaderAndWriterActivity extends Activity {
	public void read() {
		ObjectInputStream input;
		String filename = "page.srl";

		try {
			input = new ObjectInputStream(new FileInputStream(new File(
					new File(getFilesDir(), "") + File.separator + filename)));
			Book mBook = (Book) input.readObject();
			List<PageImpl> mPages = mBook.getAllPages();
			if (mPages.size() == 0) {
				Log.v("serialization", "book is empty");
			} else {
				for (int i = 0; i < mPages.size(); i++) {
					Log.v("serialization", "book " + i);
				}
			}

			input.close();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void save(Object mObject) {
		Book myPersonObject = new Book();
		// myPersonObject.setA(432);
		String filename = "book.srl";
		ObjectOutput out = null;

		try {
			out = new ObjectOutputStream(new FileOutputStream(new File(
					getFilesDir(), "") + File.separator + filename));
			out.writeObject(myPersonObject);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
