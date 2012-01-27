package com.openmeap.model;

import java.util.*;

import javax.persistence.EntityManager;

import com.openmeap.Event;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.service.*;
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
	
	@Test public void testSaveOrUpdate() {
		
		/////////////////
		// Verify that we can create and save an applicaiton
		// and that the application returned has the pk assigned by the db/orm 
		Application app = new Application();
		app.setName("This is a unique app name, or my name ain't Jon.");
		app = modelService.saveOrUpdate(app);
		Assert.assertTrue(app.getId()!=null);
		
		ApplicationVersion appVer = new ApplicationVersion();
		appVer.setApplication(app);
		appVer.setIdentifier("smacky id");
		appVer = modelService.saveOrUpdate(appVer);
		Assert.assertTrue(appVer.getId()!=null);
		
		/////////////////
		// Verify that we can modify an application
		app = modelService.findByPrimaryKey(Application.class,1L);
		String origName = app.getName();
		app.setName("picard");
		modelService.saveOrUpdate(app);
		app = modelService.findByPrimaryKey(Application.class,1L);
		Assert.assertTrue(app.getName()!=null && app.getName().compareTo("picard")==0 );
		Assert.assertTrue(app.getId()!=null);
		// put the app back the way we found it
		app.setName(origName);
		modelService.saveOrUpdate(app);
	}
	
	@Test public void testFind() {
		Application appFound = modelService.findByPrimaryKey(Application.class,1L);
		Assert.assertTrue( appFound.getName()!=null && appFound.getName().compareTo("Application.name")==0 );
	}
	
	@Test public void testFindAll() {
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
	
	@Test public void testFindAppVersionByNameAndId() {
		ApplicationVersion app = modelService.findAppVersionByNameAndId("Application.name","ApplicationVersion.identifier.2");
		Assert.assertTrue(app!=null);
	}
	
	@Test public void testFireEventHandlers() {
		List<ModelServiceEventNotifier> handlers = new ArrayList<ModelServiceEventNotifier>();
		class MockUpdateNotifier implements ModelServiceEventNotifier {
			public Boolean eventFired = false;
			public Boolean getEventFired() {
				return eventFired;
			}
			@Override
			public <E extends Event<ModelEntity>> void notify(E event) {
				eventFired = true;
			}
		};	
		handlers.add(new MockUpdateNotifier());
		modelService.setEventNotifiers(handlers);
		Application app = modelService.findByPrimaryKey(Application.class, 1L);
		modelService.saveOrUpdate(app);
		Assert.assertTrue(((MockUpdateNotifier)modelService.getEventNotifiers().toArray()[0]).getEventFired());
	}
	
	// putting this last because it corrupts the model
	@Test public void testDelete() {
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
		Assert.assertTrue(appVer==null);
	}
}
