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

import java.io.InputStream;
import java.io.OutputStream;

import com.openmeap.thinclient.LocalStorage;
import com.openmeap.thinclient.LocalStorageException;
import com.openmeap.thinclient.update.UpdateStatus;

public class LocalStorageImpl implements LocalStorage {

	public void deleteImportArchive() {
		// TODO Auto-generated method stub
		
	}

	public void unzipImportArchive(UpdateStatus status)
			throws LocalStorageException {
		// TODO Auto-generated method stub
		
	}

	public OutputStream getImportArchiveOutputStream()
			throws LocalStorageException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getImportArchiveInputStream()
			throws LocalStorageException {
		// TODO Auto-generated method stub
		return null;
	}

	public OutputStream openFileOutputStream(String fileName)
			throws LocalStorageException {
		return null;
	}

	public OutputStream openFileOutputStream(String prefix, String fileName)
			throws LocalStorageException {
		// TODO Auto-generated method stub
		return null;
	}

	public void resetStorage() {
		// TODO Auto-generated method stub
		
	}

	public void resetStorage(String prefix) {
		// TODO Auto-generated method stub
		
	}

	public Long getBytesFree() {
		// TODO Auto-generated method stub
		return null;
	}

}
