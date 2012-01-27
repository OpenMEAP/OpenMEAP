package com.openmeap.thinclient;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.List;

public interface LocalStorage {
	
	public void deleteImportArchive();
	
	public OutputStream getImportArchiveOutputStream() throws FileNotFoundException;
	
	public InputStream getImportArchiveInputStream() throws FileNotFoundException;
	
	/**
	 * Opens a FileOutputStream to the fileName under the current storage location
	 * @param fileName
	 * @return
	 */
	public FileOutputStream openFileOutputStream(String fileName) throws FileNotFoundException;
	public FileOutputStream openFileOutputStream(String prefix, String fileName) throws FileNotFoundException;
	
	/**
	 * Deletes all files from the current location
	 */
	public void resetStorage();
	public void resetStorage(String prefix);
	
	/**
	 * @return The number of bytes of storage that are free
	 */
	public Long getBytesFree();
}