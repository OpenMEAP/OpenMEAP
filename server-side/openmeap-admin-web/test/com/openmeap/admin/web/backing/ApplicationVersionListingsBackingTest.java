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

package com.openmeap.admin.web.backing;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.openmeap.constants.FormConstants;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.event.ProcessingTargets;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelTestUtils;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.web.ProcessingUtils;
import com.openmeap.web.html.Anchor;
import com.openmeap.digest.DigestInputStreamFactory;

public class ApplicationVersionListingsBackingTest {
	
	static ModelManager modelManager = null;
	
	@BeforeClass static public void beforeClass() {
		if( modelManager==null ) {
			ModelTestUtils.resetTestDb();
			ModelTestUtils.createModel(null);
			modelManager = ModelTestUtils.createModelManager();
		}
		DigestInputStreamFactory.setDigestInputStreamForName("md5", com.openmeap.digest.Md5DigestInputStream.class);
		DigestInputStreamFactory.setDigestInputStreamForName("sha1", com.openmeap.digest.Sha1DigestInputStream.class);
	}
	
	@AfterClass static public void afterClass() {
		ModelTestUtils.resetTestDb();
	}
	
	@Test public void testFormSetup() {
		
		Map<Object,Object> vars = new HashMap<Object,Object>();
		Map<Object,Object> parms = new HashMap<Object,Object>();
		
		////////////////
		// If no application id is specified, then we should get only a MessageBacking targeting event back
		ApplicationVersionListingsBacking avlb = new ApplicationVersionListingsBacking();
		avlb.setModelManager(modelManager);
		Collection<ProcessingEvent> events = avlb.process(null,vars,parms);
		Assert.assertTrue(events!=null && events.size()==1 && ProcessingUtils.containsTarget(events,ProcessingTargets.MESSAGES));
		Assert.assertTrue(vars.size()==0);
		
		////////////////
		// Verify that a valid application id returns 
		//   - a "Create Application Version" anchor
		//   - a list of versions
		parms.put(FormConstants.APP_ID, new String[]{"1"});
		events = avlb.process(null,vars,parms);
		Assert.assertTrue(events!=null && events.size()==3 && ProcessingUtils.containsTarget(events,ProcessingTargets.NAV_SUB));
		for( ProcessingEvent event : events ) {
			Assert.assertTrue(event.getPayload() instanceof Anchor);
		}
		Assert.assertTrue(vars.get("versions")!=null && ((Map<String,ApplicationVersion>)vars.get("versions")).size()==2 );
	}
}
