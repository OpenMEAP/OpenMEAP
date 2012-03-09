package com.openmeap.util;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.hibernate.ejb.Ejb3Configuration;

public class Ejb3ConfigurationFactory {
	
	private Map<String,String> properties;
	private String configuration = "hibernate.cfg.xml";
	private String targetEnvironment = "javase";

	public Ejb3Configuration create(String persistenceUnit) {
		
		Ejb3Configuration conf = new Ejb3Configuration();
		Properties jpaProperties = new Properties();
		for( PropertyAssociation property : PropertyAssociation.values() ) {
			String value = properties.get(property.toString());
			if( StringUtils.isBlank(property.getSystemProperty()) && StringUtils.isNotBlank(value) ) {
				property.setSystemProperty(value);
				jpaProperties.put(property.getJpaPropertyName(), value);
				jpaProperties.put(property.getSystemPropertyName(), value);
			}
		}
		
		if(targetEnvironment.equals("javase")) {
			conf.addProperties(jpaProperties).configure(configuration);
		} else if(targetEnvironment.equals("javaee")) {
			conf.configure(configuration,jpaProperties);
		}
		
		return conf;
	}
	
	private enum PropertyAssociation {
		OPENMEAP_JPA_SHOWSQL("hibernate.show_sql","show_sql"),
		OPENMEAP_JPA_GENERATEDDL("hibernate.hbm2ddl.auto","hbm2ddl.auto"),
		OPENMEAP_JPA_DIALECT("hibernate.dialect","dialect"),
		OPENMEAP_JDBC_DRIVERCLASS("hibernate.connection.driver_class","connection.driver_class"),
		OPENMEAP_JDBC_URL("hibernate.connection.url","connection.url"),
		OPENMEAP_JDBC_USERNAME("hibernate.connection.username","connection.username"),
		OPENMEAP_JDBC_PASSWORD("hibernate.connection.password","connection.password"),
		OPENMEAP_HIBERNATE_CFG("hibernate.ejb.cfgfile","hibernate.ejb.cfgfile");
		private String systemPropertyName = null;
		private String jpaPropertyName = null;
		private PropertyAssociation(String systemPropertyName, String jpaPropertyName) {
			this.systemPropertyName = systemPropertyName;
			this.jpaPropertyName = jpaPropertyName;
		}
		public String getJpaPropertyName() {
			return jpaPropertyName;
		}
		public String getSystemPropertyName() {
			return systemPropertyName;
		}
		public String getSystemProperty() {
			return System.getProperty(this.systemPropertyName);
		}
		public String setSystemProperty(String value) {
			return System.setProperty(this.systemPropertyName,value);
		}
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	public String getConfiguration() {
		return configuration;
	}
	public void setConfiguration(String configPath) {
		this.configuration = configPath;
	}
	
	public String getTargetEnvironment() {
		return targetEnvironment;
	}
	public void setTargetEnvironment(String targetEnvironment) {
		this.targetEnvironment = targetEnvironment;
	}
}
