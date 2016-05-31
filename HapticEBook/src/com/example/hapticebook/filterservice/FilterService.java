package com.example.hapticebook.filterservice;

import com.example.hapticebook.data.book.Page;

import android.content.res.Resources;
import android.graphics.Bitmap;

public interface FilterService {
	public Bitmap generateTPadFilter(Resources resources, Page currentPage, int sampeSize);

	public Bitmap generateTPadFilter(Resources resources, Page currentPage);
}
