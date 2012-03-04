package com.openmeap.model.service;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.Event;
import com.openmeap.cluster.AbstractClusterServiceMgmtNotifier;
import com.openmeap.cluster.ClusterNotificationException;
import com.openmeap.constants.FormConstants;
import com.openmeap.constants.ServletNameConstants;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.model.ArchiveUploadEvent;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.util.Utils;

abstract public class AbstractArchiveEventNotifier extends
		AbstractClusterServiceMgmtNotifier<ApplicationArchive> {
	
	private ModelManager modelManager;
	private Logger logger = LoggerFactory.getLogger(AbstractArchiveEventNotifier.class);
	
	abstract protected String getArchiveEventActionName();
	
	protected void addRequestParameters(ApplicationArchive archive, Map<String,Object> parms) {
	}
	
	@Override
	protected void makeRequest(URL url, Event<ApplicationArchive> message) throws ClusterNotificationException {
		try {
			ArchiveUploadEvent event = (ArchiveUploadEvent)message;
			ApplicationArchive archive = event.getPayload();
	
			String hash = archive.getHash();
			String hashType = archive.getHashAlgorithm();
			String authToken = newAuthToken();
			
			Map<String,Object> parms = new HashMap<String,Object>();;
			parms.put(UrlParamConstants.APPARCH_HASH, hash);
			parms.put(UrlParamConstants.APPARCH_HASH_ALG, hashType);
			
			// TODO: the clusternode should know...this url should not have to be passed this way
			parms.put(UrlParamConstants.CLUSTERNODE_KEY, url.toString());
			
			String sendUrl = url.toString()+"/"+ServletNameConstants.SERVICE_MANAGEMENT
				+"/?"+UrlParamConstants.ACTION+"="+getArchiveEventActionName()
				+"&"+UrlParamConstants.AUTH_TOKEN+"="+authToken;
			
			addRequestParameters(archive,parms);
			
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

	/*
	 * ACCESSORS
	 */
	
	public ModelManager getModelManager() {
		return modelManager;
	}
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
}
