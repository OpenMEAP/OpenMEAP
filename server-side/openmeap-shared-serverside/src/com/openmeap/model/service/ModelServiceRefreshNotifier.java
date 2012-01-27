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

package com.openmeap.model.service;

import com.openmeap.Event;
import com.openmeap.cluster.AbstractClusterServiceMgmtNotifier;
import com.openmeap.cluster.ClusterNotificationException;
import com.openmeap.cluster.ClusterServiceNotifierConfig;
import com.openmeap.constants.ServletNameConstants;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.model.*;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationInstallation;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.GlobalSettings;

import java.util.*;
import java.net.*;

import org.apache.http.*;
import org.slf4j.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.openmeap.util.*;

public class ModelServiceRefreshNotifier 
		extends AbstractClusterServiceMgmtNotifier<ModelEntity>
		implements ModelServiceEventNotifier, ApplicationContextAware {

	private Logger logger = LoggerFactory.getLogger(ModelServiceRefreshNotifier.class);
	private String configBeanName;
	private ApplicationContext applicationContext;
	
	@Override
	protected void onBeforeNotify(final Event<ModelEntity> event) {
		if( getConfig() == null ) {
			setConfig( (ClusterServiceNotifierConfig)applicationContext.getBean(configBeanName) );
		}
	}
	
	/**
	 * This MUST remain state-less
	 * 
	 * @param <T>
	 * @param thisUrl
	 * @param obj
	 */
	@Override
	protected void makeRequest(final URL url, final Event<ModelEntity> mesg) throws ClusterNotificationException {
		
		HttpResponse httpResponse = null;
		String simpleName = null;
		
		String thisUrl = url.toString() + "/" + ServletNameConstants.SERVICE_MANAGEMENT + "/";
		
		ModelEntity obj = mesg.getPayload();
		
		// TODO: move these into a list of class-name strings, then spring configure
		// I am not using obj.getClass().getSimpleName() because of Hibernate Proxy object wrapping
		if( obj instanceof Application )
			simpleName = "Application";
		else if( obj instanceof ApplicationVersion)
			simpleName = "ApplicationVersion";
		else if( obj instanceof ApplicationArchive)
			simpleName = "ApplicationArchive";
		else if( obj instanceof ApplicationInstallation)
			simpleName = "ApplicationInstallation";
		else if( obj instanceof GlobalSettings)
			simpleName = "GlobalSettings";
		else return;
		
		Map<String,Object> parms = new HashMap<String,Object>();
		parms.put(UrlParamConstants.ACTION, ModelEntityModifyEvent.NAME);
		parms.put(UrlParamConstants.AUTH_TOKEN, newAuthToken());
		parms.put(UrlParamConstants.REFRESH_TYPE, simpleName);
		parms.put(UrlParamConstants.REFRESH_OBJ_PKID, obj.getPk());
		try {
			logger.debug("Refresh post to {} for {} with id {}",new Object[]{thisUrl,simpleName,obj.getPk()});
			httpResponse = getHttpRequestExecuter().postData(thisUrl,parms);
			Utils.consumeInputStream(httpResponse.getEntity().getContent());
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if( statusCode!=200 ) {
				String exMesg = "HTTP "+statusCode+" returned for refresh post to "+thisUrl+" for "+simpleName+" with id "+obj.getPk();
				logger.error(exMesg);
				throw new ClusterNotificationException(exMesg);
			}
		} catch( Exception e ) {
			String exMesg = "Refresh post to "+thisUrl+" for "+simpleName+" with id "+obj.getPk()+" threw an exception";
			logger.error(exMesg,e);
			throw new ClusterNotificationException(exMesg,e);
		}
	}

	// ACCESSORS
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public String getConfigBeanName() {
		return configBeanName;
	}
	public void setConfigBeanName(String configBeanName) {
		this.configBeanName = configBeanName;
	}

}
