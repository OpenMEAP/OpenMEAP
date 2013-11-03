/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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
import com.openmeap.model.dto.Application;
import com.openmeap.web.ProcessingUtils;

public class AddModifyApplicationBackingTest {
	
	@BeforeClass static public void beforeClass() {
		ModelTestUtils.resetTestDb();
		ModelTestUtils.createModel(null);
	}
	
	@AfterClass static public void afterClass() {
		ModelTestUtils.resetTestDb();
	}
	
	@Test public void testAddModifyFormSetup() {

		ModelManager mm = ModelTestUtils.createModelManager();
		
		//////////////////////////
		// Verify the correct templateVariables are produced when no applicationId is passed in
		Map<Object,Object> vars = new HashMap<Object,Object>();
		Map<Object,Object> parms = new HashMap<Object,Object>();
		AddModifyApplicationBacking amab = new AddModifyApplicationBacking();
		amab.setModelManager( mm );
		amab.process(null, vars, parms);
		Assert.assertTrue(vars.get(FormConstants.PROCESS_TARGET)!=null && ((String)vars.get(FormConstants.PROCESS_TARGET)).compareTo(ProcessingTargets.ADDMODIFY_APP)==0);
		
		//////////////////////////
		// Verify that passing in an applicationId will return the app in the model 
		vars = new HashMap<Object,Object>();
		parms = new HashMap<Object,Object>();
		parms.put(FormConstants.APP_ID, new String[] {"1"} );
		amab = new AddModifyApplicationBacking();
		amab.setModelManager( mm );
		amab.process(null, vars, parms);
		Assert.assertTrue(vars.get(FormConstants.PROCESS_TARGET)!=null && ((String)vars.get(FormConstants.PROCESS_TARGET)).compareTo(ProcessingTargets.ADDMODIFY_APP)==0);
		Assert.assertTrue(vars.get("application")!=null && ((Application)vars.get("application")).getId()==1L);
	}
	
	@Test public void testCreateAndModifyApplications() {
		
		ModelTestUtils.resetTestDb();
		ModelTestUtils.createModel(null);
		ModelManager mm = ModelTestUtils.createModelManager();
		
		//////////////////////////
		// Verify that we can use the backing to create a new application
		Map<Object,Object> vars = new HashMap<Object,Object>();
		Map<Object,Object> parms = new HashMap<Object,Object>();
		AddModifyApplicationBacking amab = null;
		parms.put(FormConstants.PROCESS_TARGET, new String[] {ProcessingTargets.ADDMODIFY_APP} );
		parms.put(FormConstants.APP_ID, new String[] {} );
		parms.put("name", new String[] {"Application.name.3"} );
		parms.put(FormConstants.APP_DESCRIPTION, new String[] {"Application.description.3"} );
		parms.put("deviceTypes", new String[] {"1"} );
		amab = new AddModifyApplicationBacking();
		amab.setModelManager( mm );
		amab.process(null, vars, parms);
		Assert.assertTrue(vars.get(FormConstants.PROCESS_TARGET)!=null && ((String)vars.get(FormConstants.PROCESS_TARGET)).compareTo(ProcessingTargets.ADDMODIFY_APP)==0);
		Assert.assertTrue(vars.get("application")!=null && ((Application)vars.get("application")).getName().compareTo("Application.name.3")==0);
		
		//////////////////////////
		// Verify that inadequate data will throw an exception
		// and that exception will manifest as an event returned
		// that targets the message backing.
		vars = new HashMap<Object,Object>();
		parms = new HashMap<Object,Object>();
		parms.put(FormConstants.PROCESS_TARGET, new String[] {ProcessingTargets.ADDMODIFY_APP} );
		parms.put(FormConstants.APP_ID, new String[] {} );
		parms.put(FormConstants.APP_DESCRIPTION, new String[] {"Application.description.4"} );
		parms.put("deviceTypes", new String[] {"1"} );
		amab = new AddModifyApplicationBacking();
		amab.setModelManager( mm );
		Collection<ProcessingEvent> events = amab.process(null, vars, parms);
		Assert.assertTrue(events.size()>0);
		Integer numFound=0;
		for( ProcessingEvent event : events ) {
			if( event.getTargets()[0].compareTo(ProcessingTargets.MESSAGES)==0 ) {
				numFound++;
			}
		}
		Assert.assertTrue(numFound==1);
		
		//////////////////////////
		// Verify that we can use the backing to modify an application
		vars = new HashMap<Object,Object>();
		parms = new HashMap<Object,Object>();
		parms.put(FormConstants.PROCESS_TARGET, new String[] {ProcessingTargets.ADDMODIFY_APP} );
		// we happen to know that the model creates an application with id 1 that is not named Application.name.3
		parms.put(FormConstants.APP_ID, new String[] {"1"} );
		parms.put("name", new String[] {"Application.name.1"} );
		parms.put(FormConstants.APP_DESCRIPTION, new String[] {"Application.description.1_modified"} );
		parms.put("deviceTypes", new String[] {"1","2"} );
		amab = new AddModifyApplicationBacking();
		amab.setModelManager( mm );
		amab.process(null, vars, parms);
		Assert.assertTrue(vars.get(FormConstants.PROCESS_TARGET)!=null && ((String)vars.get(FormConstants.PROCESS_TARGET)).compareTo(ProcessingTargets.ADDMODIFY_APP)==0);
		Assert.assertTrue(vars.get("application")!=null && ((Application)vars.get("application")).getDescription().compareTo("Application.description.1_modified")==0);
		// verify that the applicaiton we modified is otherwise uncorrupted.
		Application app = mm.getModelService().findByPrimaryKey(Application.class,1L);
		Assert.assertTrue(app.getName()!=null && app.getName().compareTo("Application.name.1")==0 );
	}
	
	@Test public void testDelete() {
		
		ModelTestUtils.resetTestDb();
		ModelTestUtils.createModel(null);
		ModelManager mm = ModelTestUtils.createModelManager();
		
		Map<Object,Object> vars = new HashMap<Object,Object>();
		Map<Object,Object> parms = new HashMap<Object,Object>();
		AddModifyApplicationBacking amab = null;
		
		//////////////////////////
		// Verify that trying to delete an application
		// without submitting the correct deleteConfirm 
		// results in no action, except message
		vars = new HashMap<Object,Object>();
		parms = new HashMap<Object,Object>();
		parms.put(FormConstants.PROCESS_TARGET, new String[] {ProcessingTargets.ADDMODIFY_APP} );
		parms.put(FormConstants.DELETE, new String[] {"Delete!"} );
		parms.put("deleteConfirm", new String[] {"this is squirreled up"} );
		parms.put(FormConstants.APP_ID, new String[] {"1"} );
		parms.put("name", new String[] {"Application.name.1"} );
		parms.put(FormConstants.APP_DESCRIPTION, new String[] {"Application.description.1_modified"} );
		parms.put("deviceTypes", new String[] {"1","2"} );
		amab = new AddModifyApplicationBacking();
		amab.setModelManager( mm );
		Collection<ProcessingEvent> events = amab.process(null, vars, parms);
		Assert.assertTrue(ProcessingUtils.containsTarget(events, ProcessingTargets.MESSAGES));
		Assert.assertTrue(mm.getModelService().findByPrimaryKey(Application.class,1L)!=null);
		
		//////////////////////////
		// Verify that we can use the backing to delete an application
		vars = new HashMap<Object,Object>();
		parms = new HashMap<Object,Object>();
		parms.put(FormConstants.PROCESS_TARGET, new String[] {ProcessingTargets.ADDMODIFY_APP} );
		parms.put(FormConstants.DELETE, new String[] {"Delete!"} );
		parms.put("deleteConfirm", new String[] {"delete the application"} );
		parms.put(FormConstants.APP_ID, new String[] {"1"} );
		parms.put("name", new String[] {"Application.name.1"} );
		parms.put(FormConstants.APP_DESCRIPTION, new String[] {"Application.description.1_modified"} );
		parms.put("deviceTypes", new String[] {"1","2"} );
		amab = new AddModifyApplicationBacking();
		amab.setModelManager( mm );
		amab.process(null, vars, parms);
		Assert.assertTrue(mm.getModelService().findByPrimaryKey(Application.class,1L)==null);
		Assert.assertTrue(parms.get(FormConstants.APP_ID)==null);
		Assert.assertTrue(ProcessingUtils.containsTarget(events, ProcessingTargets.MESSAGES));
	}
}
