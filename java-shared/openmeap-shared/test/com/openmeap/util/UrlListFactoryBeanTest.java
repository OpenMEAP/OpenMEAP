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
import java.net.*;

public class UrlListFactoryBeanTest {
	@Test public void testSetListValues() throws Exception {
		String validUrls = "http://www.validurl.com,https://another.valid.url";
		String invalidUrls = "http_invalidurl,https:/another.invalid.url";
		UrlListFactoryBean b = new UrlListFactoryBean();
		b.setListValues(validUrls);
		Assert.assertTrue(b.getListValues().equals(validUrls));
		
		Boolean thrown = false;
		try {
			b.setListValues(invalidUrls);
		} catch( MalformedURLException mue ) {
			thrown = true;
		}
		Assert.assertTrue(thrown);
	}
}
