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

package com.openmeap.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;

import com.openmeap.thinclient.LocalStorage;
import com.openmeap.thinclient.LocalStorageException;
import com.openmeap.thinclient.update.UpdateException;
import com.openmeap.thinclient.update.UpdateResult;
import com.openmeap.thinclient.update.UpdateStatus;
import com.openmeap.util.GenericRuntimeException;

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

	public OutputStream openFileOutputStream(String fileName) throws LocalStorageException {
		String internalStorageName = FileContentProvider.getInternalStorageFileName(fileName);
        try {
			return activity.openFileOutput(internalStorageName,Activity.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			throw new LocalStorageException(e);
		}
	}
	
	public OutputStream openFileOutputStream(String prefix, String fileName) throws LocalStorageException {
		String internalStorageName = FileContentProvider.getInternalStorageFileName(prefix,fileName);
        try {
			return activity.openFileOutput(internalStorageName,Activity.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			throw new LocalStorageException(e);
		}
	}

	public void deleteImportArchive() {
		activity.deleteFile("import.zip");
	}

	public OutputStream getImportArchiveOutputStream() throws LocalStorageException {
		try {
			return activity.openFileOutput("import.zip", Activity.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			throw new LocalStorageException(e);
		}
	}

	public InputStream getImportArchiveInputStream() throws LocalStorageException {
		try {
			return activity.openFileInput("import.zip");
		} catch (FileNotFoundException e) {
			throw new LocalStorageException(e);
		}
	}
	
	public void unzipImportArchive(UpdateStatus update) throws LocalStorageException {
		// at this point, we've verified that:
		//   1) we have enough space on the device
		//   2) the archive downloaded is what was expected

		ZipInputStream zis = null;
		String newPrefix = "com.openmeap.storage."+update.getUpdateHeader().getHash().getValue();
		try {
			zis = new ZipInputStream( getImportArchiveInputStream() );
		    ZipEntry ze;
		    while ((ze = zis.getNextEntry()) != null) {
		    	if( ze.isDirectory() )
		    		continue;
		        OutputStream baos = openFileOutputStream(newPrefix,ze.getName());
		        try {
		        	byte[] buffer = new byte[1024];
		        	int count;
		        	while ((count = zis.read(buffer)) != -1) {
		        		baos.write(buffer, 0, count);
		        	}
		        }
		        catch( Exception e ) {
		        	;// TODO: something, for the love of god.
		        }
		        finally {
		        	baos.close();
		        }
		    }
		} catch( Exception e ) {
			
			// delete the recently unzipped assets
			
			throw new LocalStorageException(e);
		} finally {
			if( zis!=null ) {
				try {
					zis.close();
				} catch (IOException e) {
					throw new GenericRuntimeException(e);
				}
			}
		}
	}

	public void closeOutputStream(OutputStream outputStream) throws LocalStorageException {
		try {
			if(outputStream==null) {
				return;
			}
			outputStream.close();
		} catch(IOException ioe) {
			throw new LocalStorageException(ioe);
		}
	}

	public void closeInputStream(InputStream inputStream) throws LocalStorageException {
		try {
			if(inputStream==null) {
				return;
			}
			inputStream.close();
		} catch(IOException ioe) {
			throw new LocalStorageException(ioe);
		}
	}
}
