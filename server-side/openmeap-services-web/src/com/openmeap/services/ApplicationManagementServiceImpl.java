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

package com.openmeap.services;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.dto.Application;
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
		
		ConnectionOpenResponse response = new ConnectionOpenResponse();
		
		ModelManager manager = getModelManager();
		GlobalSettings settings = modelManager.getGlobalSettings();
		if( StringUtils.isBlank(settings.getExternalServiceUrlPrefix()) ) {
			logger.warn("The external service url prefix configured in the admin interface is blank.  This will probably cause issues downloading application archives.");
		}
		
		// we will need to verify that they have the latest version
		String appName = request.getApplication().getName();
		String appVersionId = request.getApplication().getVersionId();
		String appArchHashValue = request.getApplication().getHashValue();
		com.openmeap.model.dto.ApplicationVersion appVer = manager.findAppVersionByNameAndId(appName,appVersionId);
		if( appVer==null ) {
			throw new WebServiceException(WebServiceException.TypeEnum.APPLICATION_VERSION_NOTFOUND,
					"The application "+appName+"(version "+appVersionId+") was not found.");
		}
		Application application = appVer.getApplication();
		
		// TODO: run rules against the request
		
		// Generate a new auth token for the device to present to the proxy
		String authToken = AuthTokenProvider.newAuthToken(application.getProxyAuthSalt());
		response.setAuthToken(authToken);
		
		// The application must specify a current version, else is invalid
		Deployment lastDeployment = modelManager.getLastDeployment(application);
		if( modelManager.getLastDeployment(application)==null || lastDeployment.getApplicationVersion()==null ) {
			throw new WebServiceException(WebServiceException.TypeEnum.MISSING_PARAMETER,
					"The application "+appName+" has no deployment history");
		}
		
		// If the version or hash value differ, then send an update
		if( lastDeployment.getApplicationVersion().getIdentifier().compareTo( request.getApplication().getVersionId() )!=0 
				|| (appArchHashValue!=null && lastDeployment.getHash().compareTo(appArchHashValue)!=0) ) {
			// TODO: I'm not happy with the discrepancies between the model and schema...besides, this update header should be encapsulated somewhere else
			ApplicationVersion currentVersion = lastDeployment.getApplicationVersion();
			ApplicationArchive currentVersionArchive = currentVersion.getArchive();
			UpdateHeader uh = new UpdateHeader();
			
			uh.setVersionIdentifier(currentVersion.getIdentifier());
			uh.setInstallNeeds(Long.valueOf(currentVersionArchive.getBytesLength()+currentVersionArchive.getBytesLengthUncompressed())); 
			uh.setStorageNeeds(Long.valueOf(currentVersionArchive.getBytesLengthUncompressed())); 
			uh.setType( UpdateType.valueOf(lastDeployment.getType().toString()) );
			uh.setUpdateUrl(currentVersionArchive.getDownloadUrl(settings));
			uh.setHash(new Hash());
			uh.getHash().setAlgorithm(
					HashAlgorithm.fromValue(
							currentVersionArchive.getHashAlgorithm()));
			uh.getHash().setValue(currentVersionArchive.getHash());
			
			response.setUpdate(uh);
		}
		
		// 1) the device isn't black listed or something
		// 2) the application is tracking individual devices...
		//    many customers may not care about authentication at this level
		if( application.getTrackInstalls() ) {
			
			com.openmeap.model.dto.ApplicationInstallation appInst = manager.findAppInstByUuid(
					request.getApplication().getInstallation().getUuid());
			if( appInst==null ) {
				appInst = new com.openmeap.model.dto.ApplicationInstallation();
				appInst.setApplicationVersion(appVer);
			}
			appInst.setLastAuthentication(new Date());
			
			try {
				modelManager.addModify(appInst);
			} catch(Exception ipe) {
				throw new WebServiceException(WebServiceException.TypeEnum.DATABASE_ERROR,ipe);
			}
		}
		
		return response;
	}
	
	public void notifyUpdateResult(UpdateNotification notification) throws WebServiceException {
		// if the application is tracking this, 
		// then flip a flag in the database
		// and run rules
	}
	
}
