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

package com.openmeap.model.service;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import com.openmeap.Event;
import com.openmeap.cluster.AbstractClusterServiceMgmtNotifier;
import com.openmeap.cluster.ClusterNotificationException;
import com.openmeap.constants.FormConstants;
import com.openmeap.constants.ServletNameConstants;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.model.ArchiveUploadEvent;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.util.AuthTokenProvider;
import com.openmeap.util.Utils;

public class ArchiveUploadNotifier extends
		AbstractClusterServiceMgmtNotifier<ApplicationArchive> {

	private ModelManager modelManager;
	private Logger logger = LoggerFactory.getLogger(ArchiveUploadNotifier.class);
	
	@Override
	protected void makeRequest(URL url, Event<ApplicationArchive> message) throws ClusterNotificationException {
		try {
			ArchiveUploadEvent event = (ArchiveUploadEvent)message;
			ApplicationArchive archive = event.getPayload();
	
			String hash = archive.getHash();
			String hashType = archive.getHashAlgorithm();
			String authToken = newAuthToken();
			Map<String,Object> parms = new HashMap<String,Object>();
			parms.put(UrlParamConstants.APPARCH_HASH, hash);
			parms.put(UrlParamConstants.APPARCH_HASH_ALG, hashType);
			parms.put(UrlParamConstants.APPARCH_FILE, archive.getFile(modelManager.getGlobalSettings().getTemporaryStoragePath()));
			
			// TODO: the clusternode should know...this url should not have to be passed this way
			parms.put(UrlParamConstants.CLUSTERNODE_KEY, url.toString());
			
			String sendUrl = url.toString()+"/"+ServletNameConstants.SERVICE_MANAGEMENT
				+"/?"+UrlParamConstants.ACTION+"="+ArchiveUploadEvent.NAME
				+"&"+UrlParamConstants.AUTH_TOKEN+"="+authToken;
			
			logger.debug("Notification to {} with params {}",sendUrl,parms);

			HttpResponse response = this.getHttpRequestExecuter().postData( sendUrl, parms );
			int statusCode = response.getStatusLine().getStatusCode();
			
			logger.debug("Notification to {} returned status code {}",sendUrl,statusCode);
			
			if( statusCode!=200 ) {
				logger.error(Utils.readInputStream(response.getEntity().getContent(), FormConstants.CHAR_ENC_DEFAULT));
				throw new ClusterNotificationException(String.format("Notification to %s returned status code %s",sendUrl,statusCode));
			} else {
				Utils.consumeInputStream(response.getEntity().getContent());
			}
		} catch( Exception e ) {
			throw new ClusterNotificationException(e.getMessage(),e);
		}
	}

	public ModelManager getModelManager() {
		return modelManager;
	}
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}	
	
}
