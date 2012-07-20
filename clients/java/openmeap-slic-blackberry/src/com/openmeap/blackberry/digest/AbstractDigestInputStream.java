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

package com.openmeap.blackberry.digest;

import java.io.InputStream;

import net.rim.device.api.crypto.AbstractDigest;

import com.openmeap.digest.DigestException;
import com.openmeap.digest.DigestInputStream;
import com.openmeap.util.GenericRuntimeException;

public class AbstractDigestInputStream  implements DigestInputStream {

	private InputStream inputStream;
	protected AbstractDigest digest = null;
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public byte[] digest() throws DigestException {
		try {
			byte[] bytes = new byte[1];
			int read = 0;
			digest.reset();
			while( (read=inputStream.read(bytes))!=(-1) ) {
				digest.update(bytes[0]);
			}
		} catch(Exception e) {
			throw new DigestException(e);
		} finally {
			try{
				inputStream.close();
			} catch(Exception e) {
				throw new GenericRuntimeException(e);
			}
		}
		return digest.getDigest();
	}
}
