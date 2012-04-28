/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2012 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Robert Thacher                              #
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

package com.openmeap.model.event.notifier;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.cluster.AbstractClusterServiceMgmtNotifier;
import com.openmeap.cluster.ClusterNotificationException;
import com.openmeap.constants.FormConstants;
import com.openmeap.constants.ServletNameConstants;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.event.Event;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.event.ModelEntityEvent;
import com.openmeap.util.HttpResponse;
import com.openmeap.util.Utils;

abstract public class AbstractModelServiceClusterServiceMgmtNotifier<T extends ModelEntity> 
		extends AbstractClusterServiceMgmtNotifier<T> 
		implements ModelServiceEventNotifier<T> {
	
	private Logger logger = LoggerFactory.getLogger(AbstractModelServiceClusterServiceMgmtNotifier.class);
	
	/**
	 * @return The action name to call the openmeap-services-web/service-manager/ interface with
	 */
	abstract protected String getEventActionName();
	
	/**
	 * Template method in-case any additional parameters are required.
	 * @param archive
	 * @param parms
	 */
	protected void addRequestParameters(ModelEntity archive, Map<String,Object> parms) {
		// intended to be overridden in subclasses
	}
	
	@Override
	protected void makeRequest(URL url, Event<T> message) throws ClusterNotificationException {
		try {
			ModelEntityEvent event = (ModelEntityEvent)message;
			T modelEntity = (T) event.getPayload();
	
			String authToken = newAuthToken();
			
			Map<String,Object> parms = new HashMap<String,Object>();;

			parms.put(UrlParamConstants.CLUSTERNODE_KEY, url.toString());
			
			String sendUrl = url.toString()+"/"+ServletNameConstants.SERVICE_MANAGEMENT
				+"/?"+UrlParamConstants.ACTION+"="+getEventActionName()
				+"&"+UrlParamConstants.AUTH_TOKEN+"="+authToken;
			
			addRequestParameters(modelEntity,parms);
			
			logger.debug("Notification to {} with params {}",sendUrl,parms);

			HttpResponse response = this.getHttpRequestExecuter().postData( sendUrl, parms );
			int statusCode = response.getStatusCode();
			
			logger.debug("Notification to {} returned status code {}",sendUrl,statusCode);
			
			if( statusCode!=200 ) {
				String responseText = Utils.readInputStream(response.getResponseBody(), FormConstants.CHAR_ENC_DEFAULT);
				logger.error(responseText);
				throw new ClusterNotificationException(url,String.format("Notification to %s returned status code %s and response text was ",sendUrl,statusCode));
			} else {
				Utils.consumeInputStream(response.getResponseBody());
			}
		} catch( Exception e ) {
			throw new ClusterNotificationException(url,e.getMessage(),e);
		}
	}
	
	@Override
	public <E extends Event<T>> void onInCommitAfterCommit(
			E event, List<ProcessingEvent> events)
			throws EventNotificationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <E extends Event<T>> void onInCommitBeforeCommit(
			E event, List<ProcessingEvent> events)
			throws EventNotificationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <E extends Event<T>> void onBeforeOperation(
			E event, List<ProcessingEvent> events)
			throws EventNotificationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <E extends Event<T>> void onAfterOperation(E event,
			List<ProcessingEvent> events) throws EventNotificationException {
		// TODO Auto-generated method stub
		
	}
}
