package com.example.hapticebook.data.book;

import java.io.File;

import android.net.Uri;

public interface Book {

	public Page goToFirstPage();

	public Page goToLastPage();

	public Page goToNextPage();

	public Page goToPrevPage();

	public boolean saveBook(File filePath);

	public boolean saveBook();

	public void addNewPage(Page newPage);

	public void deleteCurrentPage();

	public Page getCurrentPage();

	public boolean isCurrentPageFirstPage();

	public boolean isCurrentPageLastPage();

	public boolean isEmpty();

	public void setFilePath(File filePath);

	public File getFilePath();

	public void readAsTeacher();

	void readAsStudent();

	boolean isTeacherMode();

	boolean isStudentMode();

	boolean isCompressed();

	int getCompressionRate();

	public Page createNewPage(Uri fileUri);

	Page createNewPage(String filePath);

	public boolean isNewlyTakenImage(Page currentPage);

	public void cancelSavingNewImage();
}
