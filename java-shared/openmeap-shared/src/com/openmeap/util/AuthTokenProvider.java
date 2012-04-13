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

package com.openmeap.util;

import java.io.ByteArrayInputStream;
import java.util.Date;

import com.openmeap.digest.DigestException;
import com.openmeap.digest.DigestInputStream;
import com.openmeap.digest.DigestInputStreamFactory;

import com.openmeap.util.UUID;

public class AuthTokenProvider {
	
	private static String AUTH_PARTS_DELIM = ".";
	
	public static String newAuthToken(String authSalt) throws DigestException {
		String authToken = UUID.randomUUID() + AUTH_PARTS_DELIM + new Date().getTime();
		authToken = authToken + "." + getSha1( authSalt+authToken );
		return authToken;
	}
	
	public static boolean validateAuthToken(String authSalt, String authToken) throws DigestException {
		if( authToken==null )
			return false;
		String[] parts = StringUtils.split(authToken,AUTH_PARTS_DELIM);
		if( parts.length!=3 )
			return false;
		
		String[] slice = (String[]) Utils.arraySlice(parts, new String[parts.length-1], 0);
		StringBuffer sb = new StringBuffer();
		boolean firstRun = true;
		for(int i=0; i<slice.length; i++) {
			if( !firstRun ) {
				sb.append(".");
			} else {
				firstRun = false;
			}
			sb.append((String)slice[i]);
		}
		String prefix = sb.toString();
		
		return getSha1( authSalt+prefix ).compareTo(parts[parts.length-1])==0;
	}
	
	private static String getSha1(String value) throws DigestException {
		DigestInputStream sha1 = DigestInputStreamFactory.getDigestInputStream("SHA1");
		sha1.setInputStream(new ByteArrayInputStream(value.getBytes()));
		return Utils.byteArray2Hex(sha1.digest());
	}
}
