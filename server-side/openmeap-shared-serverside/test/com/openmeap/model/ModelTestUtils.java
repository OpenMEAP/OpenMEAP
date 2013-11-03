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
import java.io.File;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.UrlResource;

import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationInstallation;

public class ModelTestUtils {
	
	public static String OPENMEAP_TEST_DB = "openmeap_test.db";
	
	private BeanFactory modelBeans = null; 

	static public BeanFactory newModelBeans() {
		return new XmlBeanFactory(new UrlResource(ModelTestUtils.class.getResource("mock/mockModelBeans.xml")) );
	}
	
	private static ClassPathXmlApplicationContext persistenceBeans = null;
	
	/**
	 * I used this mainly to generate the DDL.
	 * @param argv
	 */
	static public void main(String[] argv) {
		//resetTestDb();
		//getPersistenceBean("modelManager");
		if( persistenceBeans==null ) {
			
			System.setProperty("hibernate.show_sql","true");
			System.setProperty("hibernate.hbm2ddl.auto","update");
			System.setProperty("hibernate.dialect","org.hibernate.dialect.MySQLDialect");
			System.setProperty("hibernate.connection.driver_class","com.mysql.jdbc.Driver");
			System.setProperty("hibernate.connection.url","jdbc:mysql://localhost:3306/openmeap");
			System.setProperty("hibernate.connection.username","openmeap");
			System.setProperty("hibernate.connection.password","password");
			
			persistenceBeans=new ClassPathXmlApplicationContext(
				new String[]{"/META-INF/persistenceContext.xml",
				"/META-INF/test/persistenceContext.xml"}
			);
		}
		createModel(null);
	}
	
	static public synchronized Object getPersistenceBean(String name) {
		if( persistenceBeans==null ) {
			
			System.setProperty("hibernate.show_sql","true");
			System.setProperty("hibernate.hbm2ddl.auto","update");
			System.setProperty("hibernate.dialect","org.hibernate.dialect.SQLite3Dialect");
			System.setProperty("hibernate.connection.driver_class","org.sqlite.JDBC");
			System.setProperty("hibernate.connection.url","jdbc:sqlite:"+OPENMEAP_TEST_DB);
			
			persistenceBeans=new ClassPathXmlApplicationContext(
				new String[]{"/META-INF/persistenceContext.xml",
				"/META-INF/test/persistenceContext.xml"}
			);
		}
		return persistenceBeans.getBean(name);
	}
	
	static public void resetTestDb() {
		if( persistenceBeans!=null ) {
			EntityManagerFactory emf = getEntityManagerFactory();
			emf.close();
			persistenceBeans.close();
		}
		persistenceBeans=null;
		File testDbFile = new File(OPENMEAP_TEST_DB);
		if( testDbFile.exists() && !testDbFile.delete() ) {
			throw new RuntimeException("Could not delete "+OPENMEAP_TEST_DB);
		}
	}

	static public ModelManager createModelManager() {
		return (ModelManager)getPersistenceBean("modelManager");
	}

	static public ModelService createModelService() {
		return (ModelServiceImpl)getPersistenceBean("modelService");
	}
	
	static public EntityManagerFactory getEntityManagerFactory() {
		return (EntityManagerFactory)getPersistenceBean("www_entityManagerFactory");
	}
	
	static public EntityManager createEntityManager() {
		return (EntityManager)getPersistenceBean("entityManager");
	}
	
	static public void createModel(EntityManager em) {
		if( em==null ) {
			em = createEntityManager();
		}
		try {
			Map<String,Map<String,? extends ModelEntity>>  modelBeans = (Map<String,Map<String,? extends ModelEntity>> )ModelTestUtils.newModelBeans().getBean("mockModel");
			
			// we need to set all (except the Device.uuid) pk's to null,
			// so the entity manager doesn't flip out, thinking we've passed it
			// a detached entity for persistence.
			for( Map.Entry<String,Map<String,? extends ModelEntity>> classes : modelBeans.entrySet() ) 
				for( ModelEntity member : classes.getValue().values() ) 
					if( ! ( member instanceof ApplicationInstallation ) )
						member.setPk(null);
			
			em.getTransaction().begin();
			
			for( String className : new String[] {"GlobalSettings","Application","ApplicationArchive","ApplicationVersion","Deployment","ApplicationInstallation"} ) {
				Map<String,? extends ModelEntity> members = modelBeans.get(className);
				for( ModelEntity member : members.values() ) {
					if( className.equals("Application") ) {
						((Application)member).setDeployments(null);
					}
					em.persist(member);
					em.flush();
				}
			}
			
			em.getTransaction().commit();
			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

