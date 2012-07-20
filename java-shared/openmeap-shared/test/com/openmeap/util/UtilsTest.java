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
import java.io.InputStream;
import java.util.Hashtable;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.openmeap.constants.FormConstants;


public class UtilsTest extends TestCase {
	public void testReadInputStream() throws Exception {
		String testXml = "<?xml version=\"1.0\"?><rootNode><childNode attribute=\"one\"/></rootNode>";
		InputStream is = new ByteArrayInputStream(testXml.getBytes());
		String result = Utils.readInputStream(is,FormConstants.CHAR_ENC_DEFAULT);
		Assert.assertTrue(result.compareTo(testXml)==0);
	}
	public void testReplaceFields() {
		String template = "${TEST} and ${ANOTHER_TEST} making sure that ${TEST} gets replace.";
		String expected = "test and test making sure that test gets replace.";
		Hashtable parms = new Hashtable();
		parms.put("TEST", "test");
		parms.put("ANOTHER_TEST", "test");
		String result = Utils.replaceFields(parms,template);
		Assert.assertEquals(expected,result);
	}
	public void testReadLine() throws Exception {
		String lines = "One\r\n2\r\n\r\n";
		InputStream is = new ByteArrayInputStream(lines.getBytes());
		Assert.assertEquals("One",Utils.readLine(is, "utf-8"));
		Assert.assertEquals("2",Utils.readLine(is, "utf-8"));
		Assert.assertEquals("",Utils.readLine(is, "utf-8"));
		is.close();
	}
}
