package com.openmeap.util;

import org.hibernate.ejb.Ejb3Configuration;

public class Ejb3ConfigurationFactory {
	
	static public Ejb3Configuration create(String persistenceUnit) {
		Ejb3Configuration conf = new Ejb3Configuration();
		conf.configure(persistenceUnit,null);
		return conf;
	}
}
