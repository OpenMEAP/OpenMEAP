/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
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

package com.openmeap.model.event.notifier;

import java.net.URL;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.cluster.ClusterNotificationException;
import com.openmeap.constants.ServletNameConstants;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.event.Event;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.ModelServiceOperation;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationInstallation;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.ModelEntityEventAction;
import com.openmeap.util.Utils;

public class ModelServiceRefreshNotifier 
		extends AbstractModelServiceClusterServiceMgmtNotifier<ModelEntity> {

	private Logger logger = LoggerFactory.getLogger(ModelServiceRefreshNotifier.class);
	
	@Override
	public Boolean notifiesFor(ModelServiceOperation operation, ModelEntity payload) {
		if(operation==ModelServiceOperation.SAVE_OR_UPDATE) {
			return true;
		}
		return false;
	}
	
	@Override
	public <E extends Event<ModelEntity>> void onInCommitAfterCommit(final E event, List<ProcessingEvent> events) throws ClusterNotificationException {
		notify(event,events);
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
		
		com.openmeap.http.HttpResponse httpResponse = null;
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
		
		Hashtable<String,Object> parms = new Hashtable<String,Object>();
		parms.put(UrlParamConstants.ACTION, ModelEntityEventAction.MODEL_REFRESH.getActionName());
		parms.put(UrlParamConstants.AUTH_TOKEN, newAuthToken());
		parms.put(UrlParamConstants.REFRESH_TYPE, simpleName);
		parms.put(UrlParamConstants.REFRESH_OBJ_PKID, obj.getPk());
		try {
			logger.debug("Refresh post to {} for {} with id {}",new Object[]{thisUrl,simpleName,obj.getPk()});
			httpResponse = getHttpRequestExecuter().postData(thisUrl,parms);
			Utils.consumeInputStream(httpResponse.getResponseBody());
			int statusCode = httpResponse.getStatusCode();
			if( statusCode!=200 ) {
				String exMesg = "HTTP "+statusCode+" returned for refresh post to "+thisUrl+" for "+simpleName+" with id "+obj.getPk();
				logger.error(exMesg);
				throw new ClusterNotificationException(url,exMesg);
			}
		} catch( Exception e ) {
			String exMesg = "Refresh post to "+thisUrl+" for "+simpleName+" with id "+obj.getPk()+" threw an exception";
			logger.error(exMesg,e);
			throw new ClusterNotificationException(url,exMesg,e);
		}
	}

	@Override
	protected String getEventActionName() {
		return "refresh";
	}
}
