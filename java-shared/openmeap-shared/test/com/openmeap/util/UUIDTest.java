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

import junit.framework.Assert;
import junit.framework.TestCase;

public class UUIDTest extends TestCase {
	public void testRandomUUID() {
		for(int i=0; i<100; i++) {
			String uuid = UUID.randomUUID();
			Assert.assertEquals(40,uuid.length());
			Assert.assertEquals('-',uuid.charAt(8));
			Assert.assertEquals('-',uuid.charAt(13));
			Assert.assertEquals('-',uuid.charAt(18));
			Assert.assertEquals('-',uuid.charAt(23));
			Assert.assertEquals('4',uuid.charAt(14));
			Assert.assertTrue(
				uuid.charAt(19)=='8' 
				|| uuid.charAt(19)=='9'
				|| uuid.charAt(19)=='a'
				|| uuid.charAt(19)=='b'
			);
		}
	}
}
