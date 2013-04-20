/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2013 OpenMEAP, Inc.                                   #
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

import java.util.Enumeration;
import java.util.Hashtable;

import com.openmeap.util.GenericRuntimeException;

public class DigestInputStreamFactory {
	private static Hashtable digests = new Hashtable();
	
	public void setStaticDigests(Hashtable digests) {
		Enumeration enumer = digests.keys();
		while(enumer.hasMoreElements()) {
			String key = (String) enumer.nextElement();
			String clazz = (String) digests.get(key);
			try {
				this.digests.put(key, DigestInputStreamFactory.class.forName(clazz));
			} catch (ClassNotFoundException e) {
				throw new GenericRuntimeException(e);
			}
		}
	}
	public void setStaticDigestInputStreamForName(String hashAlgorithm, Class digest) {
		setDigestInputStreamForName(hashAlgorithm.toUpperCase(),digest);
	}
	static public void setDigestInputStreamForName(String hashAlgorithm, Class digest) {
		digests.put(hashAlgorithm.toUpperCase(), digest);
	}
	
	static public DigestInputStream getDigestInputStream(String hashAlgorithm) {
		try {
			return (DigestInputStream)((Class)digests.get(hashAlgorithm.toUpperCase())).newInstance();
		} catch (Exception e) {
			throw new GenericRuntimeException(e);
		} 
	}
}
