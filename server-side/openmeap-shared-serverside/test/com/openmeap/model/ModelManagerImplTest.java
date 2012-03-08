package com.openmeap.model;

import java.util.*;
import javax.persistence.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationInstallation;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;

public class ModelManagerImplTest {
	
	private static ModelManager modelManager = null;
	
	@BeforeClass static public void beforeClass() {
		if( modelManager == null ) {
			ModelTestUtils.resetTestDb();
			ModelTestUtils.createModel(null);
			modelManager = ModelTestUtils.createModelManager();
		}
	}
	
	@AfterClass static public void afterClass() {
		ModelTestUtils.resetTestDb();
	}
	
	@Test public void testGetLastDeployment() throws Exception {
		Application app = modelManager.getModelService().findByPrimaryKey(Application.class, 1L);
		Deployment d = modelManager.getModelService().getLastDeployment(app);
		Assert.assertTrue(d!=null && d.getApplicationVersion().getIdentifier().equals("ApplicationVersion.identifier.2"));
	}
	@Test public void testAddModifyApplication() throws Exception {
		
		InvalidPropertiesException e = null;
		Application app = null;
		
		//////////////////////////////////
		// verify that the correct exception is thrown when someone tries
		// to modify a completely invalid Application
		try {
			app = new Application();
			modelManager.addModify(app);
		} catch( InvalidPropertiesException ipe ) {
			e = ipe;
		}
		Assert.assertTrue(e!=null && e.getMethodMap().size()==1);
		Assert.assertTrue( e.getMethodMap().containsKey(Application.class.getMethod("getName")) );
		
		//////////////////////////////////
		// make sure that adding name changes the exception
		e=null;
		try {
			app = new Application();
			app.setName("Application.2.name");
			app = modelManager.addModify(app);
		} catch( InvalidPropertiesException ipe ) {
			e = ipe;
		}
		Assert.assertTrue(e==null);
		Assert.assertTrue( app.getId()!=null && app.getName().compareTo("Application.2.name")==0 );
		
		//////////////////////////////////
		// now modify the application returned by addModifyApplication
		Long id = app.getId();
		app.setName("Application.2.name_modified");
		app = modelManager.addModify(app);
		Application appFound = modelManager.getModelService().findByPrimaryKey(Application.class,id);
		Assert.assertTrue(appFound.getName().compareTo("Application.2.name_modified")==0);
	}
	
	@Test public void testGlobalSettings() throws Exception {
		GlobalSettings settings = new GlobalSettings();
		Boolean peThrown = false;
		try {
			modelManager.addModify(settings);
		} catch(PersistenceException pe) {
			peThrown = true;
		}
		Assert.assertTrue(peThrown);
		
		settings = modelManager.getGlobalSettings();
		Assert.assertTrue(settings.getId().equals(Long.valueOf(1)));
		
		Map<String,ClusterNode> nodes = settings.getClusterNodes();
		nodes.put("http://test",new ClusterNode());
		nodes.get("http://test").setServiceWebUrlPrefix("http://test");
		nodes.get("http://test").setFileSystemStoragePathPrefix("/tmp2");
		settings = modelManager.addModify(settings);
		
		modelManager.getModelService().refresh(settings);
		settings = modelManager.getGlobalSettings();
		Assert.assertTrue(settings.getClusterNodes().size()==3);
		Assert.assertTrue(settings.getClusterNodes().get("http://test")!=null);
	}
	
	@Test public void testAddModifyApplicationVersion() throws Exception {
		
		Boolean thrown = false;
		Application app = modelManager.getModelService().findByPrimaryKey(Application.class,1L);
		InvalidPropertiesException e = null;
		
		////////////////////////////
		// Verify creating a new application version
		ApplicationVersion version = newValidAppVersion(app);
		version = modelManager.addModify(version);
		modelManager.getModelService().delete(version);
		
		////////////////////////////
		// Verify that attempting to create an application version 
		// with no content length specified throws an exception
		version = newValidAppVersion(app);
		version.getArchive().setBytesLength(null);
		try {
			version = modelManager.addModify(version);
		} catch( InvalidPropertiesException ipe ) {
			e=ipe;
			thrown=true;
		}
		Assert.assertTrue("When bytes length is null, an exception should be thrown",thrown);
		Assert.assertTrue(e.getMethodMap().get(ApplicationArchive.class.getMethod("getBytesLength"))!=null);
		
		////////////////////////////
		// Verify that attempting to create an application version 
		// with no content length specified throws an exception
		version.getArchive().setBytesLength(0);
		try {
			version = modelManager.addModify(version);
		} catch( InvalidPropertiesException ipe ) {
			e=ipe;
			thrown=true;
		}
		Assert.assertTrue("When bytes length is 0, an exception should be thrown",thrown);
		Assert.assertTrue(e.getMethodMap().get(ApplicationArchive.class.getMethod("getBytesLength"))!=null);
		
		////////////
		// Verify that trying to add a version with an invalid hash throws an exception
		version.getArchive().setHashAlgorithm("NOT_SUCH_ALGORITHM");
		try {
			version = modelManager.addModify(version);
		} catch( InvalidPropertiesException ipe ) {
			e=ipe;
		}
		Assert.assertTrue(e!=null);
		Assert.assertTrue(e.getMethodMap().size()==2);
		Assert.assertTrue(e.getMethodMap().get(ApplicationArchive.class.getMethod("getHashAlgorithm"))!=null);
		Assert.assertTrue(e.getMethodMap().get(ApplicationArchive.class.getMethod("getBytesLength"))!=null);
	}
	
	@Test public void testAddModifyApplicationInstallation() throws Exception {
		ApplicationInstallation ai = new ApplicationInstallation();
		ai.setApplicationVersion( modelManager.getModelService().findAppVersionByNameAndId("Application.name","ApplicationVersion.identifier.1") );
		ai.setUuid("AppInst.name.1");
		modelManager.addModify(ai);
		ai = modelManager.getModelService().findByPrimaryKey(ApplicationInstallation.class,"AppInst.name.1");
		Assert.assertTrue(ai!=null);
	}
	
	/**
	 * Encapsulated so I can make a bunch of minor variations on a valid app version
	 * to test the model manager rigorously
	 * @param app
	 * @return
	 */
	ApplicationVersion newValidAppVersion(Application app) {
		ApplicationVersion version = new ApplicationVersion();
		version.setIdentifier(UUID.randomUUID().toString());
		version.setArchive(new ApplicationArchive());
		version.getArchive().setVersion(version);
		version.getArchive().setUrl("ApplicationArchive.url.3");
		version.getArchive().setHashAlgorithm("SHA1");
		version.getArchive().setHash("ApplicationArchive.hash.3");
		version.getArchive().setBytesLength(1000);
		version.getArchive().setBytesLengthUncompressed(1000);
		version.setCreateDate(null);
		version.setNotes(null);
		version.setApplication(app);
		return version;
	}
}
