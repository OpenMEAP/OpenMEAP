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

import java.io.ByteArrayInputStream;
import java.util.Hashtable;

import com.openmeap.blackberry.HttpRequestExecuterImpl;
import com.openmeap.constants.FormConstants;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.http.HttpRequestExecuter;
import com.openmeap.http.HttpResponse;
import com.openmeap.util.AuthTokenProvider;
import com.openmeap.util.GenericRuntimeException;
import com.openmeap.util.Utils;

public class DigestInputStreamTest implements Runnable {
	
	private static String TEST_STRING = "The quick brown fox jumps over the lazy dog.";
			
	OpenMEAPAppTestScreen screen;
	
	public DigestInputStreamTest(OpenMEAPAppTestScreen screen) {
		this.screen=screen;
	}
	
	public void run() {
		try {
			screen.append("<h3>DigestInputStreamTest</h3><br/>");
			screen.append("Test string: \""+TEST_STRING+"\"<br/>");
			// test MD5
			screen.append("Testing MD5: ");
			String expectedHash = "e4d909c290d0fb1ca068ffaddf22cbd0";
			String hash = Utils.hashInputStream("md5", new ByteArrayInputStream(TEST_STRING.getBytes(FormConstants.CHAR_ENC_DEFAULT)));
			screen.assertTrue(hash, hash.equals(expectedHash));
			
			// test SHA-1
			screen.append("Testing SHA-1: ");
			expectedHash = "408d94384216f890ff7a0c3528e8bed1e0b01621";
			hash = Utils.hashInputStream("sha1", new ByteArrayInputStream(TEST_STRING.getBytes(FormConstants.CHAR_ENC_DEFAULT)));
			screen.assertTrue(hash, hash.equals(expectedHash));
			
		} catch(Exception e) {
			throw new GenericRuntimeException(e);
		}
	}
}
