/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Rob Thacher                                 #
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

import java.io.InputStream;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.openmeap.digest.DigestException;
import com.openmeap.digest.DigestInputStream;
import com.openmeap.digest.DigestInputStreamFactory;
import com.openmeap.util.AuthTokenProvider;

public class AuthTokenProviderTest extends TestCase {
	public static class MockDigestInputStream implements DigestInputStream {
		public void setInputStream(InputStream inputStream) {
		}
		public byte[] digest() throws DigestException {
			return new byte[]{(byte)0xde,(byte)0xad,(byte)0xbe,(byte)0xef};
		}
	}
	public void test() throws Exception {
		DigestInputStreamFactory.setDigestInputStreamForName("sha1", MockDigestInputStream.class);
		String token = AuthTokenProvider.newAuthToken("testSalt");
		Assert.assertTrue( AuthTokenProvider.validateAuthToken("testSalt",token));
	}
}
