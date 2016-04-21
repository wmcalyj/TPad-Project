package com.example.hapticebook.data.book.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.example.hapticebook.MainActivity;
import com.example.hapticebook.data.book.Page;

public class Book extends MainActivity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8096429413088139091L;
	private List<PageImpl> pages;
	private int current;
	private File mFilePath;

	public Book() {
		pages = new ArrayList<PageImpl>();
		current = 0;
	}

	public int getCurrent() {
		return this.current;
	}

	// Get all the pages, if there is no page, null will be returned
	public List<PageImpl> getAllPages() {
		// TODO
		if (pages == null) {
			return new ArrayList<PageImpl>();
		}
		return pages;
	}

	public PageImpl goToFirstPage() {
		if (pages != null && pages.size() > 0) {
			current = 0;
			return pages.get(0);
		}
		return null;
	}

	public PageImpl goToLastPage() {
		if (pages != null && pages.size() > 0) {
			current = pages.size() - 1;
			return pages.get(current);
		}
		return null;
	}

	public PageImpl goToNextPage() {
		PageImpl p;
		if (current < pages.size() - 1) {
			p = pages.get(++current);
		} else {
			current = pages.size() - 1;
			p = pages.get(pages.size() - 1);
		}
		return p;
	}

	public PageImpl goToPrevPage() {
		PageImpl p;
		if (current > 0) {
			p = pages.get(--current);
		} else {
			current = 0;
			p = pages.get(0);
		}
		return p;
	}

	public Boolean isEmpty() {
		if (pages == null || pages.size() == 0 || pages.isEmpty()) {
			return true;
		}
		return false;
	}

	public int getTotalPages() {
		return pages.size();
	}

	public void addAndSavePage(PageImpl page) {
		pages.add(page);
		saveBook();
	}

	public void addPage(PageImpl page) {
		pages.add(page);
	}

	public Page createNewPage() {
		return new PageImpl(mFilePath.getAbsolutePath());
	}

	public void addAndSavePages(List<PageImpl> pages) {
		pages.addAll(pages);
		saveBook();
	}

	public void addPages(List<PageImpl> pages) {
		pages.addAll(pages);
	}

	public boolean saveBook() {

		ObjectOutput out = null;

		try {
			out = new ObjectOutputStream(new FileOutputStream(new File(
					mFilePath, "") + File.separator + FILE_NAME));
			out.writeObject(this);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean deleteCurrentPage() {

		// Delete the only page
		if (pages.size() == 1) {
			pages.remove(current);
			current = -1;
			return true;
		}
		try {
			if (current == pages.size() - 1) {
				// Last page
				pages.remove(current--);
			} else {
				// Not last page
				pages.remove(current);
			}
		} catch (UnsupportedOperationException e) {
			Log.w("", "Failed to delete current page - " + e.getMessage());
			return false;
		} catch (IndexOutOfBoundsException e) {
			Log.w("", "Failed to delete current page - " + e.getMessage());
			return false;
		}
		return saveBook();
	}

	public PageImpl getCurrentPage() {
		Log.d("", "current: " + current + ", size of pages: " + pages.size());
		if (current == -1 || pages == null || pages.size() <= current) {
			return null;
		}
		return this.pages.get(current);
	}

	public boolean saveBook(File filePath) {
		ObjectOutput out = null;

		try {
			out = new ObjectOutputStream(new FileOutputStream(new File(
					filePath, "") + File.separator + FILE_NAME));
			out.writeObject(this);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public File getFilePath() {
		return mFilePath;
	}

	public void setFilePath(File filePath) {
		this.mFilePath = filePath;

	}
}
