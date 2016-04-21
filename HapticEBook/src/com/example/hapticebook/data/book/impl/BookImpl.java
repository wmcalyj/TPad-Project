package com.example.hapticebook.data.book.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.example.hapticebook.MainActivity;
import com.example.hapticebook.data.book.Book;
import com.example.hapticebook.data.book.Page;

public class BookImpl extends MainActivity implements Serializable, Book {

	private static final long serialVersionUID = 7398311213542276810L;

	private Page header;
	private Page tail;
	private Page current;
	private boolean includeDeleted;

	private File mFilePath;

	public File getFilePath() {
		return mFilePath;
	}

	public void setFilePath(File filePath) {
		this.mFilePath = filePath;

	}

	@Override
	public Page goToFirstPage() {
		return this.header;
	}

	@Override
	public Page goToLastPage() {
		return this.tail;
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

	@Override
	public boolean loadBook() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addNewPage(Page newPage) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Page createNewPage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteCurrentPage() {
		// TODO Auto-generated method stub

	}

	@Override
	public Page getCurrentPage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCurrentPageFirstPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCurrentPageLastPage() {
		// TODO Auto-generated method stub
		return false;
	}
}
