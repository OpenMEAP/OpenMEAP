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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class AuthTokenProvider {
	
	public static String newAuthToken(String authSalt) {
		String authToken = UUID.randomUUID().toString() + "." + new Date().getTime();
		authToken = authToken + "." + getSha1( authSalt+authToken );
		return authToken;
	}
	
	public static Boolean validateAuthToken(String authSalt, String authToken) {
		if( authToken==null )
			return false;
		String[] parts = authToken.split("\\.");
		if( parts.length!=3 )
			return false;
		String prefix = StringUtils.join(ArrayUtils.subarray(parts, 0, parts.length-1),".");
		return getSha1( authSalt+prefix ).compareTo(parts[parts.length-1])==0;
	}
	
	private static String getSha1(String value) {
		MessageDigest sha1;
		try {
			sha1 = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		return Utils.byteArray2Hex(sha1.digest(value.getBytes()));
	}
}
