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

package com.openmeap.web;

import org.junit.*;

import com.openmeap.web.FreeMarkerSection;
import com.openmeap.web.TemplatedSection;

import freemarker.template.*;
import freemarker.cache.*;
import java.util.*;

public class FreeMarkerSectionTest {
	@Test public void testBasicRender() throws Exception {		
		FreeMarkerSection fms = new FreeMarkerSection(FreeMarkerTestUtils.createConfiguration(this.getClass(),"./"),"templates/test.ftl");
		fms.setTemplateVariables(new HashMap());
		Map<Object,Object> moo = fms.getTemplateVariables();
		moo.put("testVariable","VALUE");
	
		String result = fms.render();
		Assert.assertTrue(result.compareTo((String)moo.get("testVariable"))==0);
	}
	
	@Test public void testChildSectionInclude() throws Exception {
		Configuration cfg = FreeMarkerTestUtils.createConfiguration(this.getClass(),"./");
		
		FreeMarkerSection fms = new FreeMarkerSection(cfg,"templates/parent.ftl");
		fms.setTemplateVariables(new HashMap());
		Map<Object,Object> moo = fms.getTemplateVariables();
		moo.put("testVariable","child.ftl");
		
		String result = fms.render();
		Assert.assertTrue("HERPDERP".compareTo(result)==0);
	}
	
	@Test public void testChildInclude() throws Exception {
		Configuration cfg = FreeMarkerTestUtils.createConfiguration(this.getClass(),"./");
		FreeMarkerSection fms = new FreeMarkerSection(cfg,"templates/1.ftl");
		fms.setChildren(new HashMap<String,TemplatedSection>());
		fms.getChildren().put("m1", new FreeMarkerSection(cfg,"templates/1.1.ftl"));
		fms.getChildren().put("m2", new FreeMarkerSection(cfg,"templates/1.2.ftl"));
		
		String result = fms.render();
		Assert.assertTrue("HERPDERP".compareTo(result)==0);
	}
}
