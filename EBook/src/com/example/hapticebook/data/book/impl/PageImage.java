package com.example.hapticebook.data.book.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PageImage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3016308491362802662L;
	private Bitmap image;
	private static final int NO_IMAGE = -1;

	public PageImage(Bitmap mImage) {
		this.image = mImage;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		if (image != null) {
			final ByteArrayOutputStream stream = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.PNG, 100, stream);
			final byte[] imageByteArray = stream.toByteArray();
			out.writeInt(imageByteArray.length);
			out.write(imageByteArray);
		} else {
			out.writeInt(NO_IMAGE);
		}
		// if (image != null) {
		// image.compress(Bitmap.CompressFormat.PNG, 100, out);
		// final byte[] imageByteArray = stream.toByteArray();
		// out.writeInt(imageByteArray.length);
		// out.write(imageByteArray);
		// }
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {

		final int length = in.readInt();

		if (length != NO_IMAGE) {
			final byte[] imageByteArray = new byte[length];
			in.readFully(imageByteArray);
			image = BitmapFactory.decodeByteArray(imageByteArray, 0, length);
		}
	}

}
