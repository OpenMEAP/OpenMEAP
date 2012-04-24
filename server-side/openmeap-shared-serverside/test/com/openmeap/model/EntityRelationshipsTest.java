package com.openmeap.model;

import org.apache.log4j.*;

import java.util.*;

import org.junit.*;
import javax.persistence.*;
import org.hibernate.ejb.*;
import org.junit.Assert;

import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationInstallation;
import com.openmeap.model.dto.ApplicationVersion;

/**
 * This body of tests really just validates that the model relationships function the way i expect.
 * 
 * NOTE: in windows, close the test.db file before running
 * 
 * @author schang
 */
public class EntityRelationshipsTest {
	
	private static EntityManager em = null;
	
	@BeforeClass static public void beforeClass() {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.TRACE);
		
		ModelTestUtils.resetTestDb();
		ModelTestUtils.createModel(null);
		em = ModelTestUtils.createEntityManager();
	}
	
	@AfterClass static public void afterClass() {
		ModelTestUtils.resetTestDb();
	}
	
	@Test public void testModel() {
		
		assertInserts(em);
		
		makeModifications(em);
		
		ApplicationArchive aa = null;
		ApplicationVersion av = null;
		Application app = null;
		
		// Verify that deleting an application version 
		// deletes only the version and archive
		av = em.find(ApplicationVersion.class, 1L);
		app = em.find(Application.class, 1L);
		
		em.getTransaction().begin();
		app.removeVersion(av);
		em.remove(av);
		em.getTransaction().commit();
		
		app = em.find(Application.class, 1L);
		aa = em.find(ApplicationArchive.class, 1L);
		Assert.assertTrue(app!=null);
		Assert.assertTrue(aa!=null);		
		
		// Verify that deleting an application deletes all the crap associated to it
		app = em.find(Application.class, 1L);
		Assert.assertTrue(app!=null);
		
		em.getTransaction().begin();
		em.remove(app);
		em.getTransaction().commit();
		
		av = em.find(ApplicationVersion.class, 2L);
		Assert.assertTrue(av!=null);
		
		em.clear();
	}
	
	public void makeModifications(EntityManager em) {
		Application app = em.find(Application.class,1L);
		app.setDescription("BUNKER");
		em.persist(app);
	}

	public void assertInserts(EntityManager em) {
		// TODO: flush this whole test out much more
		
		Query q = em.createQuery("select distinct a from Application a");
		
		@SuppressWarnings("unchecked")
		List<Application> result = (List<Application>)q.getResultList();
		
		Assert.assertTrue(result.size()==1);
		Application a = result.get(0);
		
		// Application simple types
		//Assert.assertTrue(a.getCurrentVersion()!=null);
		Assert.assertTrue(a.getName().compareTo("Application.name")==0);
		Assert.assertTrue(a.getDescription().compareTo("Application.description")==0);
		
		// Versions and CurrentVersion
		Assert.assertTrue(a.getVersions()!=null && a.getVersions().entrySet().size()==2);
		Assert.assertTrue(a.getDeployments().size()==3);
	}
}
