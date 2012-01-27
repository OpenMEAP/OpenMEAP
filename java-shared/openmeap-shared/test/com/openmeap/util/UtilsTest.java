/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.util;

import org.junit.*;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;


public class UtilsTest {
	@Test public void testGetDocument() throws Exception {
		// just validate that we can parse an xml document
		String testXml = "<?xml version=\"1.0\"?><rootNode><childNode attribute=\"one\"/></rootNode>";
		InputStream is = new BufferedInputStream(new ByteArrayInputStream(testXml.getBytes()));
		Document d = Utils.getDocument(is);
		Node rootNode = d.getFirstChild();
		Node childNode = rootNode.getFirstChild();
		Assert.assertTrue(rootNode.getNodeName().compareTo("rootNode")==0);
		Assert.assertTrue(childNode.getNodeName().compareTo("childNode")==0);
		Assert.assertTrue(childNode.getAttributes().getNamedItem("attribute").getNodeValue().compareTo("one")==0);
	}
	@Test public void testReadInputStream() throws Exception {
		String testXml = "<?xml version=\"1.0\"?><rootNode><childNode attribute=\"one\"/></rootNode>";
		InputStream is = new BufferedInputStream(new ByteArrayInputStream(testXml.getBytes()));
		String result = Utils.readInputStream(is,"UTF-8");
		Assert.assertTrue(result.compareTo(testXml+System.getProperty("line.separator"))==0);
	}
	@Test public void testReplaceFields() {
		String template = "${TEST} and ${ANOTHER_TEST} making sure that ${TEST} gets replace.";
		String result = "test and test making sure that test gets replace.";
		Map<String,String> parms = new HashMap<String,String>();
		parms.put("TEST", "test");
		parms.put("ANOTHER_TEST", "test");
		Assert.assertTrue(Utils.replaceFields(parms,template).compareTo(result)==0);
	}
}
