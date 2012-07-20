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

package com.openmeap.digest;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import com.openmeap.util.GenericRuntimeException;

abstract public class AbstractDigestInputStream implements DigestInputStream {
	
	private InputStream inputStream;
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	abstract protected String getHashAlgorithm();

	public byte[] digest() throws DigestException {
		InputStream is=null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(getHashAlgorithm());
			is = new java.security.DigestInputStream(inputStream, md);
			byte[] bytes = new byte[1024];
			while( is.read(bytes)==1024 );
		} catch( Exception ioe ) {
			throw new DigestException(ioe);
		} finally {
			if(is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new GenericRuntimeException(e);
				}
			}
		}
		return md.digest();
	}
}
