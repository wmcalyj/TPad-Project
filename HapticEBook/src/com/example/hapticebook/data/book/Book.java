package com.example.hapticebook.data.book;

import java.io.File;

public interface Book {

	public Page goToFirstPage();

	public Page goToLastPage();

	public Page goToNextPage();

	public Page goToPrevPage();

	public boolean saveBook(File filePath);

	public boolean saveBook();

	public boolean loadBook();

	public void addNewPage(Page newPage);

	public void deleteCurrentPage();

	public Page getCurrentPage();

	public Page createNewPage();

	public boolean isCurrentPageFirstPage();

	public boolean isCurrentPageLastPage();

	public boolean isEmpty();

	public void setFilePath(File filePath);

	public File getFilePath();

	public void readAsTeacher();

	void readAsStudent();

	boolean isTeacherMode();

	boolean isStudentMode();
}
