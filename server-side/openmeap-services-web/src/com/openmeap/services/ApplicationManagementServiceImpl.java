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

package com.openmeap.services;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.digest.DigestException;
import com.openmeap.http.HttpRequestExecuter;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.protocol.ApplicationManagementService;
import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.ConnectionOpenRequest;
import com.openmeap.protocol.dto.ConnectionOpenResponse;
import com.openmeap.protocol.dto.Hash;
import com.openmeap.protocol.dto.HashAlgorithm;
import com.openmeap.protocol.dto.UpdateHeader;
import com.openmeap.protocol.dto.UpdateNotification;
import com.openmeap.protocol.dto.UpdateType;
import com.openmeap.util.AuthTokenProvider;
import com.openmeap.util.GenericRuntimeException;

/**
 * Server-side implementation of the ApplicationManagementService
 * 
 * @author schang
 */
public class ApplicationManagementServiceImpl implements ApplicationManagementService {
	
	Logger logger = LoggerFactory.getLogger(ApplicationManagementServiceImpl.class);
	
	private ModelManager modelManager = null;
	
	/**
	 * Set the ModelManager for the service to use when handling CRUD operations.
	 * 
	 * @param modelManager
	 */
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}
	
	public ConnectionOpenResponse connectionOpen(ConnectionOpenRequest request) throws WebServiceException {
		
		String reqAppArchHashVal = StringUtils.trimToNull(request.getApplication().getHashValue());
		String reqAppVerId = StringUtils.trimToNull(request.getApplication().getVersionId());
		String reqAppName = StringUtils.trimToNull(request.getApplication().getName());
		
		ConnectionOpenResponse response = new ConnectionOpenResponse();
		
		GlobalSettings settings = modelManager.getGlobalSettings();
		if( StringUtils.isBlank(settings.getExternalServiceUrlPrefix()) && logger.isWarnEnabled() ) {
			logger.warn("The external service url prefix configured in the admin interface is blank.  "
					+"This will probably cause issues downloading application archives.");
		}
		
		Application application = getApplication(reqAppName,reqAppVerId);
		
		// Generate a new auth token for the device to present to the proxy
		String authToken;
		try {
			authToken = AuthTokenProvider.newAuthToken(application.getProxyAuthSalt());
		} catch (DigestException e) {
			throw new GenericRuntimeException(e);
		}
		response.setAuthToken(authToken);
		
		// If there is a deployment, 
		// and the version of that deployment differs in hash value or identifier
		// then return an update in the response
		Deployment lastDeployment = modelManager.getModelService().getLastDeployment(application);
		Boolean reqAppVerDiffers = lastDeployment!=null && !lastDeployment.getVersionIdentifier().equals(reqAppVerId);
		Boolean reqAppArchHashValDiffers = lastDeployment!=null && reqAppArchHashVal!=null && !lastDeployment.getApplicationArchive().getHash().equals(reqAppArchHashVal);
		
		// we only send an update if 
		//   a deployment has been made
		// and one of the following is true
		//   the app version is different than reported
		//   the app hash value is different than reported
		if( reqAppVerDiffers || reqAppArchHashValDiffers ) {
			
			// TODO: I'm not happy with the discrepancies between the model and schema
			// ...besides, this update header should be encapsulated somewhere else
			ApplicationArchive currentVersionArchive = lastDeployment.getApplicationArchive();
			UpdateHeader uh = new UpdateHeader();
			
			uh.setVersionIdentifier(lastDeployment.getVersionIdentifier());
			uh.setInstallNeeds(Long.valueOf(currentVersionArchive.getBytesLength()+currentVersionArchive.getBytesLengthUncompressed())); 
			uh.setStorageNeeds(Long.valueOf(currentVersionArchive.getBytesLengthUncompressed())); 
			uh.setType( UpdateType.fromValue(lastDeployment.getType().toString()) );
			uh.setUpdateUrl(currentVersionArchive.getDownloadUrl(settings));
			uh.setHash(new Hash());
			uh.getHash().setAlgorithm(
					HashAlgorithm.fromValue(
							currentVersionArchive.getHashAlgorithm()));
			uh.getHash().setValue(currentVersionArchive.getHash());
			
			response.setUpdate(uh);
		}
		
		return response;
	}
	
	public void notifyUpdateResult(UpdateNotification notification) throws WebServiceException {
		// if the application is tracking this, 
		// then flip a flag in the database
		// and run rules
	}
	
	/*
	 * PRIVATE METHODS
	 */
	
	private Application getApplication(String appName, String appVersionId) throws WebServiceException {
		
		if( appName==null || appVersionId==null ) {
			throw new WebServiceException(WebServiceException.TypeEnum.APPLICATION_VERSION_NOTFOUND,
					"Both application name and version id must be specified.");
		}
		
		ModelManager manager = getModelManager();
		
		// we will need to verify that they have the latest version
		Application application = manager.getModelService().findApplicationByName(appName);
		if( application==null ) {
			throw new WebServiceException(WebServiceException.TypeEnum.APPLICATION_NOTFOUND,
					"The application \""+appName+"\" was not found.");
		}
				
		return application;
	}
	
	private HttpRequestExecuter executer = null;
	private String serviceUrl = null;
	@Override
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
	@Override
	public void setHttpRequestExecuter(HttpRequestExecuter executer) {
		this.executer = executer;
	}	
}
