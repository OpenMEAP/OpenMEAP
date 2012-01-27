/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.cluster;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.GlobalSettings;

/**
 * This is written kind of wacky because it is used by things that it depends on.
 * @author schang
 */
public class ClusterServiceNotifierConfig {
	
	private Logger logger = LoggerFactory.getLogger(ClusterServiceNotifierConfig.class);
	
	private Collection<URL> webServiceUrls = null;
	private String authSalt = null;
	private ModelManager modelManager = null;
	
	/**
	 * Default constructor
	 */
	public ClusterServiceNotifierConfig() {
		;// intentionally empty
	}
	
	/**
	 * Initializes the configuration object using the GlobalSettings object
	 * from the database.
	 * @param manager
	 */
	public ClusterServiceNotifierConfig(ModelManager manager) {
		modelManager = manager;
	}
	
	private void refreshAuthSalt() {
		GlobalSettings settings = modelManager.getGlobalSettings();
		setAuthSalt(settings.getServiceManagementAuthSalt());
	}
	
	private void refreshUrls() {
		GlobalSettings settings = modelManager.getGlobalSettings();
		Map<String,ClusterNode> nodes = settings.getClusterNodes();
		List<URL> urls = new ArrayList<URL>();
		for( Map.Entry<String,ClusterNode> entry : nodes.entrySet() ) {
			try {
				urls.add( new URL(entry.getKey()) );
			} catch( MalformedURLException mue ) {
				logger.error("A malformed url exception thrown parsing the service url: "+entry.getKey(),mue);
			}
		}
		setServerUrls(urls);
	}
	
	/**
	 * @param authSalt The application wide authentication hash salt.
	 */
	public void setAuthSalt(String authSalt) {
		this.authSalt = authSalt;
	}	
	public String getAuthSalt() {
		if( modelManager!=null ) {
			refreshAuthSalt();
		}
		return authSalt;
	}
	
	/**
	 * 
	 * @param urls
	 */
	public void setServerUrls(final Collection<URL> urls) {
		webServiceUrls = urls;
	}
	public Collection<URL> getWebServiceUrls() {
		if( modelManager!=null ) {
			refreshUrls();
		}
		return webServiceUrls;
	}
}
