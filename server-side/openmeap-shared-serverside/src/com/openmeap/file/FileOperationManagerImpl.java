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

package com.openmeap.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.transaction.file.FileResourceManager;
import org.apache.commons.transaction.file.ResourceManagerException;
import org.apache.commons.transaction.file.ResourceManagerSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelService;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.util.SLF4JLoggerFacade;
import com.openmeap.util.Utils;

/**
 * FileOperationManager for transaction safe file operations.
 * Essentially just a thin-wrapper over org.apache.commons.transaction.file
 * 
 * @author schang
 */
public class FileOperationManagerImpl implements FileOperationManager {
	
	Logger logger = LoggerFactory.getLogger(FileOperationManagerImpl.class);
	ModelService modelService;
	FileResourceManager fileResourceManager;
	Map<Thread,Object> activeTransactions = Collections.synchronizedMap(new HashMap<Thread,Object>());
	
	public void setFileResourceManager(FileResourceManager fileResourceManager) {
		this.fileResourceManager = fileResourceManager;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	@Override
	public void deleteDir(String path) throws FileOperationException {
		String storeDir = this.fileResourceManager.getStoreDir();
		String workDir = this.fileResourceManager.getWorkDir();
		
		_deleteDir(storeDir,storeDir+File.separator+path);
		_deleteDir(workDir,workDir+File.separator+path);
		delete(path);
	}
	
	@Override
	public void delete(String path) throws FileOperationException {
		Object txId = activeTransactions.get(Thread.currentThread());
		logger.trace("Marking delete for {}",path);
		try {
			fileResourceManager.deleteResource(txId,path,true);
		} catch (ResourceManagerException e) {
			throw new FileOperationException(e);
		}
	}
	
	@Override
	public boolean exists(String path) throws FileOperationException {
		Object txId = activeTransactions.get(Thread.currentThread());
		try {
			return fileResourceManager.resourceExists(txId,path);
		} catch (ResourceManagerException e) {
			throw new FileOperationException(e);
		}
	}
	
	@Override
	public void create(String path) throws FileOperationException {
		Object txId = activeTransactions.get(Thread.currentThread());
		try {
			fileResourceManager.createResource(txId,path,false);
		} catch (ResourceManagerException e) {
			throw new FileOperationException(e);
		}
	}
	
	@Override
	public void copy(String src, String dest) throws FileOperationException {
		Object txId = activeTransactions.get(Thread.currentThread());
		try {
			fileResourceManager.copyResource(txId,src,dest,false);
		} catch (ResourceManagerException e) {
			throw new FileOperationException(e);
		}
	}
	
	@Override
	public void move(String src, String dest) throws FileOperationException {
		Object txId = activeTransactions.get(Thread.currentThread());
		try {
			fileResourceManager.moveResource(txId,src,dest,false);
		} catch (ResourceManagerException e) {
			throw new FileOperationException(e);
		}
	}
	
	@Override
	public InputStream read(String path) throws FileOperationException {
		Object txId = activeTransactions.get(Thread.currentThread());
		try {
			return fileResourceManager.readResource(txId,path);
		} catch (ResourceManagerException e) {
			throw new FileOperationException(e);
		}
	}
	
	@Override
	public OutputStream write(String path) throws FileOperationException {
		Object txId = activeTransactions.get(Thread.currentThread());
		try {
			return fileResourceManager.writeResource(txId,path);
		} catch (ResourceManagerException e) {
			throw new FileOperationException(e);
		}
	}
	
	@Override
	public void begin() throws FileOperationException {
		_setup();
		Object txId;
		try {
			fileResourceManager.start();
			txId = fileResourceManager.generatedUniqueTxId();
			fileResourceManager.startTransaction(txId);
		} catch (ResourceManagerSystemException e) {
			throw new FileOperationException(e);
		} catch (ResourceManagerException e) {
			throw new FileOperationException(e);
		}
		activeTransactions.put(Thread.currentThread(), txId);
	}
	
	@Override
	public void commit() throws FileOperationException {
		try {
			fileResourceManager.commitTransaction(activeTransactions.get(Thread.currentThread()));
		} catch (ResourceManagerException e) {
			throw new FileOperationException(e);
		}
	}
	
	@Override
	public void rollback() throws FileOperationException {
		try {
			fileResourceManager.rollbackTransaction(activeTransactions.get(Thread.currentThread()));
		} catch (ResourceManagerException e) {
			throw new FileOperationException(e);
		}
	}	
	
	@Override
	public void unzipFile(ZipFile zipFile, String destinationDir) throws FileOperationException {
		try {
			int BUFFER = 1024;
			BufferedOutputStream dest = null;
			BufferedInputStream is = null;
	
			ZipEntry entry;
			
			Enumeration e = zipFile.entries();
			while(e.hasMoreElements()) {
				try {
					entry = (ZipEntry) e.nextElement();
					is = new BufferedInputStream(zipFile.getInputStream(entry));
					String newFile = destinationDir+File.separator+entry.getName();
					if( entry.isDirectory() ) {
						continue; // skip directories, resource manager will create for us
					} else {
						OutputStream fos = write(newFile);
						dest = new BufferedOutputStream(fos, BUFFER);
						Utils.pipeInputStreamIntoOutputStream(is, fos);
					}
				} finally {
					if( dest!=null ) {
						dest.close();
					}
					if( is!=null ) {
						is.close();
					}
				}
			}
		} catch(IOException ioe) {
			throw new FileOperationException(ioe);
		}
	}
	
	private void _deleteDir(String prefix, String absolutePath) throws FileOperationException {
		
		File absFile = new File(absolutePath);
		String absPath = absFile.getAbsolutePath();
		String superPrefix = absFile.getAbsolutePath().substring(0,absPath.length()-absolutePath.length()-1);
		
		if(absFile.exists() && absFile.isDirectory()) {
			File[] files = absFile.listFiles();
			if(files==null) {
				return;
			}
			for(File thisFile : files) {
				String fullPath = thisFile.getAbsolutePath();
				String relativePath = fullPath.substring(superPrefix.length()+prefix.length()+2);
				if(thisFile.isDirectory()) {
					_deleteDir(prefix, prefix+File.separator+relativePath);
				} else {
					delete(relativePath);
				}
			}
		}
	}
	
	private void _setup() throws FileOperationException {
		
		if(fileResourceManager!=null) {
			return;
		}
		
		GlobalSettings settings = (GlobalSettings)modelService.findByPrimaryKey(GlobalSettings.class, 1L);
		if( settings.getTemporaryStoragePath()==null 
				|| !new File(settings.getTemporaryStoragePath()).exists()) {
			String msg = "The storage path has not been set in GlobalSettings.  Use the settings page to fix this.";
			logger.error(msg);
			throw new FileOperationException(msg);
		}
		
		FileResourceManager resMgr = new FileResourceManager(
				settings.getTemporaryStoragePath()
				,settings.getTemporaryStoragePath()+"/tmp",
				true,
				new SLF4JLoggerFacade(LoggerFactory.getLogger(FileResourceManager.class)));
		fileResourceManager = resMgr; 
	}
}
