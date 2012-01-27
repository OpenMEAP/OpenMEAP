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

package com.openmeap.admin.web.backing;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import javax.persistence.PersistenceException;

import static com.openmeap.util.ParameterMapUtils.*;

import com.openmeap.web.AbstractTemplatedSectionBacking;
import com.openmeap.web.ProcessingContext;
import com.openmeap.web.ProcessingEvent;
import com.openmeap.model.InvalidPropertiesException;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.Authorizer.Action;
import com.openmeap.admin.web.ProcessingTargets;
import com.openmeap.admin.web.backing.event.MessagesEvent;

public class GlobalSettingsBacking extends AbstractTemplatedSectionBacking {
	private ModelManager modelManager;
	
	private final static String PROCESS_TARGET_PARAM     = "processTarget";
	private final static String AUTH_SALT_PARAM          = "authSalt";
	private final static String AUTH_SALT_VERIFY_PARAM   = "authSaltVerify";
	private final static String STORAGE_PATH_PARAM       = "tempStoragePathPrefix";
	private final static String CLUSTER_NODES_VAR        = "clusterNodes";
	private final static String CLUSTER_NODE_URLS_PARAM  = "clusterNodeUrl[]";
	private final static String CLUSTER_NODE_PATHS_PARAM = "clusterNodePath[]";
	private final static String EXT_SVC_URL_PREFIX_PARAM = "externalServiceUrlPrefix";
	private final static String MAX_FILE_UPLOAD_SIZE_PARAM = "maxFileUploadSize";
	
	public GlobalSettingsBacking() {
		processingTargetIds.add(ProcessingTargets.GLOBAL_SETTINGS);
	}
	
	@Override
	public Collection<ProcessingEvent> process(ProcessingContext context, Map<Object,Object> templateVariables, Map<Object, Object> parameterMap) {
		
		List<ProcessingEvent> events = new ArrayList<ProcessingEvent>();
		GlobalSettings settings = modelManager.getGlobalSettings();
		
		// setup the variables required for form render
		templateVariables.put(PROCESS_TARGET_PARAM, ProcessingTargets.GLOBAL_SETTINGS);
		
		Boolean hasPerm = modelManager.getAuthorizer().may(Action.MODIFY, settings);
		templateVariables.put("mayModify",hasPerm);
		if ( hasPerm == Boolean.FALSE ){
			events.add(new MessagesEvent("The user logged in does not have permissions to change the global settings."));
		}
		
		if( !empty(PROCESS_TARGET_PARAM, parameterMap) ) {
			// process a post to the form
			
			if( !empty(EXT_SVC_URL_PREFIX_PARAM,parameterMap) ) {
				String svcUrl = firstValue(EXT_SVC_URL_PREFIX_PARAM,parameterMap);
				settings.setExternalServiceUrlPrefix(svcUrl);
			}
			
			if( !empty(MAX_FILE_UPLOAD_SIZE_PARAM,parameterMap) ) {
				Integer maxFileUploadSize = Integer.valueOf(firstValue(MAX_FILE_UPLOAD_SIZE_PARAM,parameterMap));
				settings.setMaxFileUploadSize(maxFileUploadSize);
			}
			
			// process the storage path parameter
			if( !empty(STORAGE_PATH_PARAM,parameterMap) ) {
				String path = firstValue(STORAGE_PATH_PARAM,parameterMap);
				File f = new File(path);
				if( !( f.exists() && f.canWrite() && f.canRead() ) ) {
					events.add(new MessagesEvent("The temporary local storage path must exist and be readable, writable, and executable by the system user this virtual machine is running as."));
				} else {
					settings.setTemporaryStoragePath( path );
				}
			}
			
			// process auth salt
			if( !empty(AUTH_SALT_PARAM, parameterMap) ) {
				if( empty(AUTH_SALT_VERIFY_PARAM,parameterMap) || !equalsEachOther(AUTH_SALT_PARAM,AUTH_SALT_VERIFY_PARAM,parameterMap)) {
					events.add(new MessagesEvent("Authentication salt and salt verify must match"));
				} else {
					settings.setServiceManagementAuthSalt( firstValue(AUTH_SALT_PARAM,parameterMap) );
				}
			}
			
			// process the ClusterNode objects
			if( parameterMap.get(CLUSTER_NODE_URLS_PARAM)!=null ) {
				String[] clusterNodeUrls = (String[])parameterMap.get(CLUSTER_NODE_URLS_PARAM);
				String[] clusterNodePaths = (String[])parameterMap.get(CLUSTER_NODE_PATHS_PARAM);
				int end = clusterNodeUrls.length;
				
				// make sure there is a map in cluster nodes
				Map<String,ClusterNode> clusterNodes = settings.getClusterNodes();
				if( clusterNodes==null ) {
					clusterNodes = new HashMap<String,ClusterNode>();
					settings.setClusterNodes(clusterNodes);
				} 
				
				// iterate over each node configuration, updating the clusterNodes as per input
				for( int i=0; i<end; i++ ) {
					String thisNodeUrl = clusterNodeUrls[i].trim();
					String thisNodePath = clusterNodePaths[i].trim();
					if( thisNodeUrl.length()==0 ) {
						events.add(new MessagesEvent("A cluster node must specify a service url it is internally accessible via the admin service."));
						continue;
					}
					if( thisNodePath.length()==0 ) {
						events.add(new MessagesEvent("The cluster node with url "+thisNodeUrl+" should specify a path to store application archives at."));
					}
					if( clusterNodes.containsKey(thisNodeUrl) ) {
						clusterNodes.get(thisNodeUrl).setFileSystemStoragePathPrefix(thisNodePath);
					} else {
						ClusterNode thisNode = new ClusterNode();
						thisNode.setServiceWebUrlPrefix(thisNodeUrl);
						thisNode.setFileSystemStoragePathPrefix(thisNodePath);
						clusterNodes.put(thisNodeUrl, thisNode);
					}
				}
				
				// remove any nodes that no longer appear
				List<String> urls = Arrays.asList(clusterNodeUrls);
				List<String> toRemove = new ArrayList<String>();
				for( String url : clusterNodes.keySet() ) {
					if( !urls.contains(url) )
						toRemove.add(url);
				}
				for( String url : toRemove ) {
					clusterNodes.remove(url);
				}
			}
			try {
				modelManager.addModify(settings);
				events.add(new MessagesEvent("The settings were successfully modified."));
			} catch( InvalidPropertiesException ipe ) {
				events.add( new MessagesEvent(ipe.getMessage()) );
			} catch( PersistenceException ipe ) {
				events.add( new MessagesEvent(ipe.getMessage()) );
			}
		} 
		
		if( settings.getExternalServiceUrlPrefix()!=null ) {
			templateVariables.put(EXT_SVC_URL_PREFIX_PARAM, settings.getExternalServiceUrlPrefix());
		}
		if( settings.getTemporaryStoragePath()!=null ) {
			templateVariables.put(STORAGE_PATH_PARAM, settings.getTemporaryStoragePath());
		}
		if( settings.getServiceManagementAuthSalt()!=null ) {
			templateVariables.put(AUTH_SALT_PARAM, settings.getServiceManagementAuthSalt());
			templateVariables.put(AUTH_SALT_VERIFY_PARAM, settings.getServiceManagementAuthSalt());
		}
		if( settings.getClusterNodes()!=null && settings.getClusterNodes().size()>0 ) {
			templateVariables.put(CLUSTER_NODES_VAR, settings.getClusterNodes());
		}
		if( settings.getMaxFileUploadSize()!=null ) {
			templateVariables.put(MAX_FILE_UPLOAD_SIZE_PARAM, settings.getMaxFileUploadSize());
		}
		
		if( events.size()>0 )
			return events;
		return null;
	}
	
	// ACCESSORS
	
	public ModelManager getModelManager() {
		return modelManager;
	}
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
}

