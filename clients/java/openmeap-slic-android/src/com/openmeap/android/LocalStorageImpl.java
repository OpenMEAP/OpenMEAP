/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Rob Thacher                                 #
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.content.Context;

import com.openmeap.thinclient.LocalStorage;
import com.openmeap.thinclient.LocalStorageException;
import com.openmeap.thinclient.update.UpdateException;
import com.openmeap.thinclient.update.UpdateResult;
import com.openmeap.thinclient.update.UpdateStatus;
import com.openmeap.util.GenericRuntimeException;

public class LocalStorageImpl implements LocalStorage {

	private static String STORAGE_ROOT = "com.openmeap.storage";
	
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
	/**
	 * Call this method remove the file.
	 * @param file: File object referrence.
	 */
	public void removeFile(File file) throws LocalStorageException {
		try {
			//Requesting the current file to delete.
			file.delete();
		} catch (Exception e) {
			throw new LocalStorageException("LocalStorageImpl::removeFile()::Exception thrown while deleting the file.",e);
		}
	}
	
	/**
	 * Call this method to traverse(Recursively) through directory and delete individual files. 
	 * @param fileOrDirectory: file or directory referrence.
	 */
	public void traverseDirectoryChainAndRemove(File fileOrDirectory) throws LocalStorageException {
		if (fileOrDirectory.exists()) {
			if(fileOrDirectory.isDirectory()){
					//means the fileOrDirectory is a directory.
					if(fileOrDirectory.list().length!= 0){
						//this directory has child files.
						for( File file1 : fileOrDirectory.listFiles() ) {
							traverseDirectoryChainAndRemove(file1);
				 		}
					}
					//Requesting the current file to delete.
					removeFile(fileOrDirectory);
			}else {
				//Requesting the current file to delete.
				removeFile(fileOrDirectory);
			}
		}else {
			//requested direcotory does not exist.
			throw new LocalStorageException("LocalStorageImpl::traverseDirectoryChainAndRemove()::Directory does not exists.");
		}
	}
	
	public void resetStorage(String prefix) {
		try {
			File fileOrDirectoryReferrence = new File(activity.getFilesDir(),prefix);
			//actual call for removing the previous hash based webapp from phone memory.
			traverseDirectoryChainAndRemove(fileOrDirectoryReferrence);
		} catch (Exception e) {
			;// handle reset storage failure.
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
		File hashHolder = null;
		String hashRootAbsolutePath = "";
		try {
			hashHolder = new File(activity.getFilesDir(),newPrefix);
			hashHolder.mkdir();
			hashRootAbsolutePath = hashHolder.getAbsolutePath();
		} catch (Exception e) {
			System.out.println("Exception thrown while creating hash folder.");
			System.out.println(e);
		}
		try {
			zis = new ZipInputStream( getImportArchiveInputStream() );
		    ZipEntry ze;
		    while ((ze = zis.getNextEntry()) != null) {
		    	if( ze.isDirectory() ){
//		    		continue;
		    		try {
		    			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		    			System.out.println("Writing directory structure in phone memory.");
						File directoryStructure = new File(hashRootAbsolutePath,ze.getName());
						directoryStructure.mkdirs();
					} catch (Exception e) {
						System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
						System.out.println("Exception thrown while writing directory structure.");
						System.out.println(e);
					}
		    	}else {
		    		try {
		    			String osSeperator = System.getProperty("file.separator");
		    			int seperatorLastIndex = ze.getName().lastIndexOf(osSeperator);
		    			String fileName = ze.getName().substring(seperatorLastIndex+1, ze.getName().length());
		    			String fileNameParentDirectoryPrefix = "";
		    			String absolutePathFromPrefix = "";
		    			if (seperatorLastIndex != -1 && seperatorLastIndex != 0) {
		    				fileNameParentDirectoryPrefix = ze.getName().substring(0, seperatorLastIndex);
			    			absolutePathFromPrefix = hashRootAbsolutePath+osSeperator+fileNameParentDirectoryPrefix;	
						} else {
							absolutePathFromPrefix = hashRootAbsolutePath+osSeperator;
						}
		    			URI osResourePathForThisFile = URI.create(absolutePathFromPrefix);
		    			File writableFileReference = new File(osResourePathForThisFile.getPath(),fileName);
		    			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(writableFileReference.getAbsolutePath(),true),1024);
				        try {
				        	byte[] buffer = new byte[1024];
				        	int count;
				        	while ((count = zis.read(buffer)) != -1) {
				        		outputStream.write(buffer, 0, count);
				        	}
				        }
				        catch( Exception e ) {
							System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
							System.out.println("Exception while writing file contents.");
							System.out.println(e);
				        }
				        finally {
				        	outputStream.close();
				        }
					} catch (Exception e) {
						System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
						System.out.println("Unknown exception.");
						System.out.println(e);
					}
		    		
		    	}
//		    	Commenting following code to make use of file:/// alternate to content://
		    	
//		        OutputStream baos = openFileOutputStream(newPrefix,ze.getName());		        
//		        try {
//		        	byte[] buffer = new byte[1024];
//		        	int count;
//		        	while ((count = zis.read(buffer)) != -1) {
//		        		baos.write(buffer, 0, count);
//		        	}
//		        }
//		        catch( Exception e ) {
//		        	;// TODO: something, for the love of god.
//		        }
//		        finally {
//		        	baos.close();
//		        }
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

	public String getStorageRoot() {
		return STORAGE_ROOT+".";
	}
}
