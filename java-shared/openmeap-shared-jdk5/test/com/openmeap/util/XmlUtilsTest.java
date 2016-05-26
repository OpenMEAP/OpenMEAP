/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XmlUtilsTest {
	@Test public void testGetDocument() throws Exception {
		// just validate that we can parse an xml document
		String testXml = "<?xml version=\"1.0\"?><rootNode><childNode attribute=\"one\"/></rootNode>";
		InputStream is = new BufferedInputStream(new ByteArrayInputStream(testXml.getBytes()));
		Document d = XmlUtils.getDocument(is);
		Node rootNode = d.getFirstChild();
		Node childNode = rootNode.getFirstChild();
		Assert.assertTrue(rootNode.getNodeName().compareTo("rootNode")==0);
		Assert.assertTrue(childNode.getNodeName().compareTo("childNode")==0);
		Assert.assertTrue(childNode.getAttributes().getNamedItem("attribute").getNodeValue().compareTo("one")==0);
	}
}
