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

import net.sf.zipme.ZipEntry;
import net.sf.zipme.ZipInputStream;

import com.openmeap.thinclient.LocalStorage;
import com.openmeap.thinclient.LocalStorageException;
import com.openmeap.thinclient.SLICConfig;
import com.openmeap.thinclient.update.UpdateStatus;
import com.openmeap.util.GenericRuntimeException;

public class LocalStorageImpl implements LocalStorage {

	private static String STORAGE_ROOT = OpenMEAPApp.STORAGE_ROOT;
	private static String IMPORT_ARCHIVE = STORAGE_ROOT+"/import.zip";
	private static Hashtable connections = new Hashtable();
	private SLICConfig config;
	
	public LocalStorageImpl(SLICConfig config) {
		this.config=config;
		try {
			FileConnection fc = null;
			try {
				fc = (FileConnection)Connector.open(STORAGE_ROOT);
				if( !fc.exists() ) {
					fc.mkdir();
				}
			} finally {
				if(fc!=null) {
					fc.close();
				}
			}
		} catch(IOException e) {
			throw new GenericRuntimeException(e.getMessage(),e);
		} 
	}
	
	public void deleteImportArchive() throws LocalStorageException {
		
		FileConnection fc=null;
		try {
			fc = (FileConnection)Connector.open(IMPORT_ARCHIVE);
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
		String newPrefix = STORAGE_ROOT+'/'+status.getUpdateHeader().getHash().getValue();
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
		return openFileOutputStream("import.zip");
	}

	public InputStream getImportArchiveInputStream()
			throws LocalStorageException {
		return openFileInputStream("import.zip");
	}

	public OutputStream openFileOutputStream(String fileName) throws LocalStorageException {
		return openFileOutputStream(STORAGE_ROOT,fileName);
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
		return openFileInputStream(STORAGE_ROOT,fileName);
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
			c = (FileConnection)Connector.open(STORAGE_ROOT);
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
		return STORAGE_ROOT+'/';
	}

}
