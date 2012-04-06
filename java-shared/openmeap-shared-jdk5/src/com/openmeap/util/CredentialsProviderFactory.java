package com.openmeap.util;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

abstract public class CredentialsProviderFactory {
	
	static private CredentialsProviderFactory credentialsProviderFactory = new CredentialsProviderFactory() {
		public CredentialsProvider newCredentialsProvider() {
			return new BasicCredentialsProvider();
		}
	};
	
	abstract public CredentialsProvider newCredentialsProvider();
	
	static public void setDefaultCredentialsProviderFactory(CredentialsProviderFactory factory) {
		credentialsProviderFactory = factory;
	}
	static public CredentialsProviderFactory getDefaultCredentialsProviderFactory() {
		return credentialsProviderFactory;
	}
	static public CredentialsProvider newDefaultCredentialsProvider() {
		return credentialsProviderFactory.newCredentialsProvider();
	}
}
