/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
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

import junit.framework.TestCase;
import junit.framework.Assert;
import java.net.*;

public class UrlListFactoryBeanTest extends TestCase {
	public void testSetListValues() throws Exception {
		String validUrls = "http://www.validurl.com,https://another.valid.url";
		String invalidUrls = "http_invalidurl,https:/another.invalid.url";
		UrlListFactoryBean b = new UrlListFactoryBean();
		b.setListValues(validUrls);
		Assert.assertTrue(b.getListValues().equals(validUrls));
		
		boolean thrown = false;
		try {
			b.setListValues(invalidUrls);
		} catch( MalformedURLException mue ) {
			thrown = true;
		}
		Assert.assertTrue(thrown);
	}
}
