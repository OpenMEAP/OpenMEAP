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

	private static String STORAGE_ROOT = "file:///store";
	private static String IMPORT_ARCHIVE = STORAGE_ROOT+"/import.zip";
	private static Hashtable connections = new Hashtable();
	private SLICConfig config;
	
	public LocalStorageImpl(SLICConfig config) {
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
			throw new GenericRuntimeException(e);
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
			throw new LocalStorageException(e);
		} finally {
			try {
				if(fc!=null) {
					fc.close();
				}
			} catch (IOException e) {
				throw new GenericRuntimeException(e);
			}
		}
	}

	public void unzipImportArchive(UpdateStatus status) throws LocalStorageException {

		// at this point, we've verified that:
		//   1) we have enough space on the device
		//   2) the archive downloaded is what was expected

		ZipInputStream zis = null;
		String newPrefix = STORAGE_ROOT+'/'+status.getUpdateHeader().getHash().getValue();
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

	public OutputStream getImportArchiveOutputStream()
			throws LocalStorageException {
		return openFileOutputStream("import.zip");
	}

	public InputStream getImportArchiveInputStream()
			throws LocalStorageException {
		// TODO Auto-generated method stub
		return null;
	}

	public OutputStream openFileOutputStream(String fileName) throws LocalStorageException {
		return openFileOutputStream(STORAGE_ROOT,fileName);
	}

	public OutputStream openFileOutputStream(String prefix, String fileName) throws LocalStorageException {
		try {
			FileConnection fc = (FileConnection)Connector.open(prefix+'/'+fileName);
			OutputStream os = fc.openOutputStream();
			connections.put(os, fc);
			return os;
		} catch(IOException ioe) {
			throw new LocalStorageException(ioe);
		}
	}
	
	public void closeOutputStream(OutputStream outputStream) throws LocalStorageException {
		try {
			if(outputStream==null) {
				return;
			}
			Connection conn = (Connection)connections.remove(outputStream);
			if(!connections.contains(conn)) {
				conn.close();
			}
		} catch(IOException ioe) {
			throw new LocalStorageException(ioe);
		}
	}
	
	public void closeInputStream(InputStream inputStream) throws LocalStorageException {
		try {
			if(inputStream==null) {
				return;
			}
			Connection conn = (Connection)connections.remove(inputStream);
			if(!connections.contains(conn)) {
				conn.close();
			}
		} catch(IOException ioe) {
			throw new LocalStorageException(ioe);
		}
	}

	public void resetStorage() {
		String currentPrefix = config.getStorageLocation();
		if(currentPrefix!=null) {
			resetStorage(currentPrefix);
			config.clearStorageLocation();
		}
	}

	public void resetStorage(String prefix) {
		
	}

	public Long getBytesFree() throws LocalStorageException {
		try {
			FileConnection c = (FileConnection)Connector.open(STORAGE_ROOT);
			Long ret = new Long(c.availableSize());
			c.close();
			return ret;
		} catch(IOException ioe) {
			throw new LocalStorageException(ioe);
		}
	}

	public void setupSystemProperties() {
		
	}

}
