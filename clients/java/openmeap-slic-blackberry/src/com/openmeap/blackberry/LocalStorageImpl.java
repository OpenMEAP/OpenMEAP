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

package com.openmeap.blackberry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.openmeap.thinclient.LocalStorage;
import com.openmeap.thinclient.LocalStorageException;
import com.openmeap.thinclient.SLICConfig;
import com.openmeap.thinclient.update.UpdateStatus;
import com.openmeap.thirdparty.net.sf.zipme.ZipEntry;
import com.openmeap.thirdparty.net.sf.zipme.ZipInputStream;
import com.openmeap.util.GenericRuntimeException;
import com.openmeap.util.StringUtils;

public class LocalStorageImpl implements LocalStorage {

	private static String IMPORT_FILE = "import.zip";
	private static Hashtable connections = new Hashtable();
	private SLICConfig config;
	
	public LocalStorageImpl(SLICConfig config) {
		this.config=config;
		try {
			_createDirs(getStorageRoot());
		} catch(IOException e) {
			throw new GenericRuntimeException(e.getMessage(),e);
		} 
	}
	
	public void deleteImportArchive() throws LocalStorageException {
		
		FileConnection fc=null;
		try {
			fc = (FileConnection)Connector.open(getImportArchivePath());
			if(fc.exists()) {
				fc.delete();
			}
		} catch (IOException e) {
			throw new LocalStorageException(e.getMessage(),e);
		} finally {
			try {
				if(fc!=null) {
					fc.close();
				}
			} catch (IOException e) {
				throw new LocalStorageException(e.getMessage(),e);
			}
		}
	}

	public void unzipImportArchive(UpdateStatus status) throws LocalStorageException {

		// at this point, we've verified that:
		//   1) we have enough space on the device
		//   2) the archive downloaded is what was expected

		ZipInputStream zis = null;
		InputStream importIs = null;
		String newPrefix = getStorageRoot()+'/'+status.getUpdateHeader().getHash().getValue();
		assertDir(newPrefix);
		
		try {
			importIs = getImportArchiveInputStream();
			zis = new ZipInputStream(importIs);
		    ZipEntry ze;
		    while ((ze = zis.getNextEntry()) != null) {
		    	if( ze.isDirectory() ) {
		    		assertDir(newPrefix+'/'+ze.getName());
		    		continue;
		    	}
		        OutputStream baos = null;
		        try {
		        	baos = openFileOutputStream(newPrefix,ze.getName());
		        	byte[] buffer = new byte[1024];
		        	int count;
		        	while ((count = zis.read(buffer)) != -1) {
		        		baos.write(buffer, 0, count);
		        	}
		        } finally {
		        	if(baos!=null) {
		        		baos.close();
		        	}
		        }
		    }
		} catch( Exception e ) {
			throw new LocalStorageException(e.getMessage(),e);
		} finally {
			try {
				if( zis!=null ) {
					zis.close();
				}
				if( importIs!=null ) {
					closeInputStream(importIs);
				}
			} catch (IOException e) {
				throw new LocalStorageException(e.getMessage(),e);
			}
		}
	}
	
	public void assertDir(String prefix) throws LocalStorageException {
		FileConnection fc = null;
		try {
			fc = (FileConnection)Connector.open(prefix+"/");
			
			if( fc.exists() && !fc.isDirectory() ) {
				fc.delete();
				fc.close();
				fc = (FileConnection)Connector.open(prefix+"/");
			}
			
			if( !fc.exists() ) {
				fc.mkdir();
			}
			
		} catch (IOException e1) {
			throw new LocalStorageException(e1.getMessage(),e1);
		} finally {
			try {
				if(fc!=null) {
					fc.close();
				}
			} catch(IOException ioe) {
				throw new LocalStorageException(ioe.getMessage(),ioe);
			}
		}
	}

	public OutputStream getImportArchiveOutputStream()
			throws LocalStorageException {
		return openFileOutputStream(IMPORT_FILE);
	}

	public InputStream getImportArchiveInputStream()
			throws LocalStorageException {
		return openFileInputStream(IMPORT_FILE);
	}

	public OutputStream openFileOutputStream(String fileName) throws LocalStorageException {
		return openFileOutputStream(getStorageRoot(),fileName);
	}

	public OutputStream openFileOutputStream(String prefix, String fileName) throws LocalStorageException {
		try {
			String location = prefix+'/'+fileName;
			FileConnection fc = (FileConnection)Connector.open(location);
			if(!fc.exists()) {
				fc.create();
			}
			OutputStream os = fc.openOutputStream();
			connections.put(os, fc);
			return os;
		} catch(IOException ioe) {
			throw new LocalStorageException(ioe.getMessage(),ioe);
		}
	}
	
	public InputStream openFileInputStream(String fileName) throws LocalStorageException {
		return openFileInputStream(getStorageRoot(),fileName);
	}
	
	public InputStream openFileInputStream(String prefix, String fileName) throws LocalStorageException {
		try {
			String location = prefix+'/'+fileName;
			FileConnection fc = (FileConnection)Connector.open(location);
			if(!fc.exists()) {
				fc.close();
				return null;
			}
			InputStream is = fc.openInputStream();
			connections.put(is, fc);
			return is;
		} catch(IOException ioe) {
			throw new LocalStorageException(ioe.getMessage(),ioe);
		}
	}
	
	public void closeOutputStream(OutputStream outputStream) throws LocalStorageException {
		try {
			if(outputStream==null) {
				return;
			}
			outputStream.close();
			if(connections.containsKey(outputStream)) {
				Connection conn = (Connection)connections.remove(outputStream);
				conn.close();
			}
		} catch(IOException ioe) {
			throw new LocalStorageException(ioe.getMessage(),ioe);
		}
	}
	
	public void closeInputStream(InputStream inputStream) throws LocalStorageException {
		try {
			if(inputStream==null) {
				return;
			}
			inputStream.close();
			if(connections.containsKey(inputStream)) {
				Connection conn = (Connection)connections.remove(inputStream);
				conn.close();
			}
		} catch(IOException ioe) {
			throw new LocalStorageException(ioe.getMessage(),ioe);
		}
	}

	public void resetStorage() throws LocalStorageException {
		String currentPrefix = config.getStorageLocation();
		if(currentPrefix!=null) {
			resetStorage(currentPrefix);
			config.clearStorageLocation();
		}
	}

	public void resetStorage(String prefix) throws LocalStorageException {
		try {
			_recursiveDelete(prefix);
		} catch (IOException e) {
			throw new LocalStorageException(e.getMessage(),e);
		}
	}

	public Long getBytesFree() throws LocalStorageException {
		FileConnection c = null;
		try {
			c = (FileConnection)Connector.open(getStorageRoot());
			Long ret = new Long(c.availableSize());
			return ret;
		} catch(IOException ioe) {
			throw new LocalStorageException(ioe.getMessage(),ioe);
		} finally {
			if(c!=null) {
				try {
					c.close();
				} catch (IOException e) {
					throw new LocalStorageException(e.getMessage(),e);
				}
			}
		}
	}

	public void setupSystemProperties() {
		
	}
	
	private void _createDirs(String path) throws IOException {
		String root = StringUtils.replaceAll(getStorageRoot(),"file://", "");
		String[] paths = StringUtils.split(root,"/");
		for(int i=0; i<paths.length; i++) {
			FileConnection fc = null;
			String currentPath = StringUtils.join(paths, "/", 0, i);
			try {
				fc = (FileConnection)Connector.open("file:///"+currentPath);
				if( !fc.exists() ) {
					fc.mkdir();
				}
			} finally {
				if(fc!=null) {
					fc.close();
				}
			}
		}
	}
	
	private void _recursiveDelete(String prefix) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(prefix);
		Enumeration e = fc.list();
		while(e.hasMoreElements()) {
			String path = (String)e.nextElement();
			if(path.endsWith("/")) {
				_recursiveDelete(prefix+path.substring(0,path.length()-1));
			} else {
				FileConnection fc2 = (FileConnection)Connector.open(prefix+'/'+path);
				fc2.delete();
			}
		}
		fc.delete();
	}

	public String getStorageRoot() {
		return getStorageRootPath();
	}
	
	public static String getStorageRootPath() {
		return OpenMEAPApp.STORAGE_ROOT;
	}
	
	public static String getImportArchivePath() {
		return getStorageRootPath()+'/'+"/import.zip";
	}

}
