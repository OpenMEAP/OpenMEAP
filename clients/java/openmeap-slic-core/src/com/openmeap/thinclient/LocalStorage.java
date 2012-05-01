/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2012 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the LGPLv3                                                #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU Lesser General Public License as published #
 #    by the Free Software Foundation, either version 3 of the License, or     #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU Lesser General Public License for more details.                      #
 #                                                                             #
 #    You should have received a copy of the GNU Lesser General Public License #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.thinclient;

import java.io.InputStream;
import java.io.OutputStream;

import com.openmeap.thinclient.update.UpdateStatus;

public interface LocalStorage {
	
	public void setupSystemProperties();
	
	public void deleteImportArchive() throws LocalStorageException;
	
	public void unzipImportArchive(UpdateStatus status) throws LocalStorageException;
	
	public OutputStream getImportArchiveOutputStream() throws LocalStorageException;
	public InputStream getImportArchiveInputStream() throws LocalStorageException;
	
	/**
	 * Opens a FileOutputStream to the fileName under the current storage location
	 * @param fileName
	 * @return
	 */
	public OutputStream openFileOutputStream(String fileName) throws LocalStorageException;
	public OutputStream openFileOutputStream(String prefix, String fileName) throws LocalStorageException;
	
	/**
	 * Deletes all files from the current location
	 */
	public void resetStorage() throws LocalStorageException;
	public void resetStorage(String prefix) throws LocalStorageException;
	
	public void closeOutputStream(OutputStream outputStream) throws LocalStorageException;
	public void closeInputStream(InputStream inputStream) throws LocalStorageException;
	
	/**
	 * @return The number of bytes of storage that are free
	 */
	public Long getBytesFree() throws LocalStorageException;
}