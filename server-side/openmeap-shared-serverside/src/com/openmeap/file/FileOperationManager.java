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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipFile;

public interface FileOperationManager {

	public abstract boolean isTransactionActive() throws FileOperationException;
	
	public abstract void deleteDir(String path) throws FileOperationException;

	public abstract void delete(String path) throws FileOperationException;

	public abstract boolean exists(String path) throws FileOperationException;

	public abstract void create(String path) throws FileOperationException;

	public abstract void copy(String src, String dest)
			throws FileOperationException;

	public abstract void move(String src, String dest)
			throws FileOperationException;

	public abstract InputStream read(String path) throws FileOperationException;

	public abstract OutputStream write(String path)
			throws FileOperationException;

	public abstract void begin() throws FileOperationException;

	public abstract void commit() throws FileOperationException;

	public abstract void rollback() throws FileOperationException;

	public abstract void unzipFile(ZipFile zipFile, String destinationDir)
			throws FileOperationException;

}