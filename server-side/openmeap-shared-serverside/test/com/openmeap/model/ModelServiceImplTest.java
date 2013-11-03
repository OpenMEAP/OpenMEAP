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

package com.openmeap.model;

import java.util.*;

import javax.persistence.EntityManager;

import com.openmeap.event.Event;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.event.handler.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.*;

/**
 * This should be the only test that actually has to touch the database
 * 
 * NOTE: in windows, close the test.db file before running
 * 
 * @author schang
 */
public class ModelServiceImplTest {
	
	private static ModelService modelService = null;
	
	@BeforeClass static public void beforeClass() {
		if( modelService == null ) {
			ModelTestUtils.resetTestDb();
			ModelTestUtils.createModel(null);
			modelService = ModelTestUtils.createModelService();
		}
	}
	
	@AfterClass static public void afterClass() {
		ModelTestUtils.resetTestDb();
	}
	
	@Test public void testInSequence() {
		testSaveOrUpdate();
		testFind();
		testFindAll();
		testGetApplicationArchiveByDeployment();
		testFindAppVersionByNameAndId();
		testDelete();
	}
	
	public void testSaveOrUpdate() {
		
		/////////////////
		// Verify that we can create and save an applicaiton
		// and that the application returned has the pk assigned by the db/orm 
		Application app = new Application();
		app.setName("This is a unique app name, or my name ain't Jon.");
		try {
			app = modelService.begin().saveOrUpdate(app);
			modelService.commit();
		} catch(Exception e) {
			modelService.rollback();
			throw new RuntimeException(e);
		}
		Assert.assertTrue(app.getId()!=null);
		
		ApplicationVersion appVer = new ApplicationVersion();
		appVer.setApplication(app);
		appVer.setIdentifier("smacky id");
		try {
			appVer = modelService.begin().saveOrUpdate(appVer);
			modelService.commit();
		} catch(Exception e) {
			modelService.rollback();
			throw new RuntimeException(e);
		}
		Assert.assertTrue(appVer.getId()!=null);
		
		/////////////////
		// Verify that we can modify an application
		app = modelService.findByPrimaryKey(Application.class,1L);
		String origName = app.getName();
		app.setName("picard");
		try {
			app = modelService.begin().saveOrUpdate(app);
			modelService.commit();
		} catch(Exception e) {
			modelService.rollback();
			throw new RuntimeException(e);
		}
		app = modelService.findByPrimaryKey(Application.class,1L);
		Assert.assertTrue(app.getName()!=null && app.getName().compareTo("picard")==0 );
		Assert.assertTrue(app.getId()!=null);
		// put the app back the way we found it
		app.setName(origName);
		try {
			app = modelService.begin().saveOrUpdate(app);
			modelService.commit();
		} catch(Exception e) {
			modelService.rollback();
			throw new RuntimeException(e);
		}
	}
	
	public void testFind() {
		Application appFound = modelService.findByPrimaryKey(Application.class,1L);
		Assert.assertTrue( appFound.getName()!=null && appFound.getName().compareTo("Application.name")==0 );
	}
	
	public void testFindAll() {
		List<ApplicationVersion> dtl = modelService.findAll(ApplicationVersion.class);
		Assert.assertTrue(dtl.size()==3);
		for( String toFind : new String[] {"ApplicationVersion.identifier.1","ApplicationVersion.identifier.2","smacky id"} ) {
			Boolean found=false;
			for( ApplicationVersion dt : dtl ) {
				found = dt.getIdentifier().compareTo(toFind)==0;
				if( found ) break;
			}
			Assert.assertTrue("expecting a ApplicationVersion with name "+toFind,found);
		}
	}
	
	public void testGetApplicationArchiveByDeployment() {
		Application app = modelService.findApplicationByName("Application.name");
		Deployment d = modelService.getLastDeployment(app);
		ApplicationArchive a = modelService.getApplicationArchiveByDeployment(d);
		Assert.assertNotNull(a);
	}
	
	public void testFindAppVersionByNameAndId() {
		ApplicationVersion app = modelService.findAppVersionByNameAndId("Application.name","ApplicationVersion.identifier.2");
		Assert.assertTrue(app!=null);
	}
	
	// putting this last because it corrupts the model
	public void testDelete() {
		// verify that we 
		Application app = modelService.findByPrimaryKey(Application.class, 1L);
		ApplicationVersion appVer = modelService.findByPrimaryKey(ApplicationVersion.class, 1L);
		modelService.delete(appVer);
		appVer = modelService.findByPrimaryKey(ApplicationVersion.class,1L);
		Assert.assertTrue(appVer==null);
		
		app = modelService.findByPrimaryKey(Application.class, 1L);
		modelService.delete(app);
		app = modelService.findByPrimaryKey(Application.class, 1L);
		appVer = modelService.findByPrimaryKey(ApplicationVersion.class,2L);
		Assert.assertTrue(appVer!=null);
	}
}
