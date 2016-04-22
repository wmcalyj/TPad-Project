package com.example.hapticebook.data.book.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.util.Log;

import com.example.hapticebook.MainActivity;
import com.example.hapticebook.data.book.Book;
import com.example.hapticebook.data.book.Page;

public class BookImpl extends MainActivity implements Serializable, Book {

	private static final long serialVersionUID = 7398311213542276810L;

	private Page header;
	private Page tail;
	private Page headerAvailable;
	private Page tailAvailable;
	private Page current;

	// Default mode is student (deleted pages not included)
	private boolean includeDeleted = false;

	private File mFilePath;

	public File getFilePath() {
		return mFilePath;
	}

	public void setFilePath(File filePath) {
		this.mFilePath = filePath;

	}

	public BookImpl() {
		header = null;
		headerAvailable = null;
		tail = null;
		tailAvailable = null;
		current = null;
		includeDeleted = false;

	}

	@Override
	public Page goToFirstPage() {
		if (includeDeleted) {
			this.current = this.header;
		} else {
			this.current = this.headerAvailable;
		}
		return current;
	}

	@Override
	public Page goToLastPage() {
		if (includeDeleted) {
			this.current = this.tail;
		} else {
			this.current = this.tailAvailable;
		}
		return this.current;
	}

	@Override
	public Page goToNextPage() {
		current = current.getNextPage(includeDeleted);
		return current;
	}

	@Override
	public Page goToPrevPage() {
		current = current.getPrevPage(includeDeleted);
		return current;
	}

	@Override
	public boolean saveBook() {
		return saveBook(mFilePath);
	}

	@Override
	public boolean saveBook(File filePath) {
		boolean originalMode = this.includeDeleted;

		// When the book is saved, always save as student mode
		// And put it back after saving
		this.includeDeleted = false;
		ObjectOutput out = null;

		try {
			out = new ObjectOutputStream(new FileOutputStream(new File(
					filePath, "") + File.separator + FILE_NAME));
			out.writeObject(this);
			out.close();
		} catch (FileNotFoundException e) {
			this.includeDeleted = originalMode;
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			this.includeDeleted = originalMode;
			e.printStackTrace();
			return false;
		} finally {
			this.includeDeleted = originalMode;
		}
		return true;
	}

	@Override
	public boolean loadBook() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addNewPage(Page newPage) {
		// The book is empty
		if (this.isEmpty()) {
			this.current = newPage;
			this.headerAvailable = newPage;
			this.tailAvailable = newPage;

			// This is a real empty book
			if (this.header == null) {
				this.header = newPage;
			}
			if (tail == null) {
				this.tail = newPage;
			} else {
				// If this is not a real empty book, meaning it's only empty in
				// student mode
				this.tail.setNextPage(newPage);
				newPage.setPrevPage(this.tail);
				this.tail = newPage;
			}
		} else {
			// Book is NOT empty
			this.tail.setNextPage(newPage);
			this.tailAvailable.setNextAvailablePage(newPage);
			newPage.setPrevAvailablePage(tailAvailable);
			newPage.setPrevPage(this.tail);

			this.tail = newPage;
			this.tailAvailable = newPage;
		}

		this.saveBook();

	}

	@Override
	public boolean isEmpty() {
		// In teacher mode
		if (includeDeleted) {
			return header == null;
		}
		// In student mode
		if (this.current == null ||
		// There is no previous available page and there is no next available
		// page, the current page is also NOT available. Then, we know the book
		// is empty
				(this.current.getPrevPage(includeDeleted) == null
						&& this.current.getNextPage(includeDeleted) == null && !this.current
							.isAvailable())) {
			return true;
		}
		return false;
	}

	@Override
	public Page createNewPage() {
		Page newPage = new PageImpl(mFilePath.getAbsolutePath());
		return newPage;
	}

	@Override
	public void deleteCurrentPage() {
		Page prevAvail = this.current.getPrevPage(false);
		Page nextAvail = this.current.getNextPage(false);

		// This is the only page left in the book
		if (prevAvail == null && nextAvail == null) {
			this.current.delete();
			this.current = null;
			this.headerAvailable = null;
			this.tailAvailable = null;
			this.saveBook();
			return;
		}
		this.current.delete();

		// If the current page is the last page
		if (this.tailAvailable == this.current) {
			// Only want the previous available page
			Log.d("", "Delete last page");
			this.tailAvailable = prevAvail;
			this.current = prevAvail;
		}
		// If the current page is the first page
		else if (this.headerAvailable == this.current) {
			Log.d("", "Delete first page");
			// Only want the next available page
			this.headerAvailable = nextAvail;
			this.current = nextAvail;
		}
		// In-between page
		else {
			this.current = nextAvail;
		}

		this.saveBook();
	}

	@Override
	public Page getCurrentPage() {
		return this.current;
	}

	@Override
	public boolean isCurrentPageFirstPage() {
		if (includeDeleted) {
			return this.current.equals(this.header);
		} else {
			return this.current.equals(this.headerAvailable);
		}
	}

	@Override
	public boolean isCurrentPageLastPage() {
		if (includeDeleted) {
			return this.current.equals(this.tail);
		} else {
			return this.current.equals(this.tailAvailable);
		}
	}

	@Override
	public void readAsTeacher() {
		this.includeDeleted = true;
	}

	@Override
	public void readAsStudent() {
		this.includeDeleted = false;
	}

	@Override
	public boolean isTeacherMode() {
		return this.includeDeleted == true;
	}

	@Override
	public boolean isStudentMode() {
		return this.includeDeleted == false;
	}
}
