/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;

import com.openmeap.thinclient.LocalStorage;

public class LocalStorageImpl implements LocalStorage {

	private MainActivity activity = null;
	
	public LocalStorageImpl(MainActivity activity) {
		this.activity = activity;
	}
	
	/**
     * Sets up a few system properties that are needed by the FileContentProvider
     * I could probably put them in a better location, but this is adequate for the time being.
     */
    public void setupSystemProperties() {
    	String path = activity.getFilesDir().getAbsolutePath()+System.getProperty("file.separator");
    	System.setProperty("root.openmeap.path",path);
    	String storageLocation = activity.getConfig().getStorageLocation();
    	System.setProperty("root.openmeap.internalStoragePrefix", storageLocation!=null ? storageLocation : "");
    }

	public Long getBytesFree() {
		String absPath = activity.getFilesDir().getAbsolutePath();
		android.os.StatFs fs = new android.os.StatFs(absPath);
		Long blockSize = (long)fs.getBlockSize();
		Long availableBlocks = (long)fs.getAvailableBlocks();
		return blockSize * availableBlocks;
	}

	public void resetStorage() {
		String currentPrefix = activity.getConfig().getStorageLocation();
		if(currentPrefix!=null) {
			resetStorage(currentPrefix);
			activity.getConfig().clearStorageLocation();
		}
	}
	
	public void resetStorage(String prefix) {
		File storageDir = activity.getFilesDir();
		for( String file : storageDir.list() ) {
 			if( file.startsWith(prefix) )
 				activity.deleteFile(file);
 		}
	}

	public FileOutputStream openFileOutputStream(String fileName) throws FileNotFoundException {
		String internalStorageName = FileContentProvider.getInternalStorageFileName(fileName);
        return activity.openFileOutput(internalStorageName,Activity.MODE_PRIVATE);
	}
	
	public FileOutputStream openFileOutputStream(String prefix, String fileName) throws FileNotFoundException {
		String internalStorageName = FileContentProvider.getInternalStorageFileName(prefix,fileName);
        return activity.openFileOutput(internalStorageName,Activity.MODE_PRIVATE);
	}

	public void deleteImportArchive() {
		activity.deleteFile("import.zip");
	}

	public OutputStream getImportArchiveOutputStream() throws FileNotFoundException {
		return activity.openFileOutput("import.zip", Activity.MODE_PRIVATE);
	}

	public InputStream getImportArchiveInputStream() throws FileNotFoundException {
		return activity.openFileInput("import.zip");
	}
}
