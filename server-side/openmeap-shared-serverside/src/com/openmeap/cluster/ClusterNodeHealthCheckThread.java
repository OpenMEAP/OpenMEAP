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

package com.openmeap.cluster;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.exception.ExceptionUtils;
import com.openmeap.thirdparty.org.json.me.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.cluster.dto.ClusterNodeRequest;
import com.openmeap.constants.FormConstants;
import com.openmeap.http.HttpRequestExecuter;
import com.openmeap.http.HttpResponse;
import com.openmeap.json.JSONObjectBuilder;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.services.dto.Result;
import com.openmeap.util.AuthTokenProvider;
import com.openmeap.util.Utils;

public class ClusterNodeHealthCheckThread implements Runnable {

	private Logger logger = LoggerFactory.getLogger(ClusterNodeHealthCheckThread.class);
	private HttpRequestExecuter httpRequestExecuter;
	private ModelManager modelManager;
	private GlobalSettings settings;
	private Integer restartInterval = 30000;
	private Integer checkInterval = 2000;
	private Vector<Exception> lastCheckExceptions;

	@Override
	public void run() {
		while(true) {
			try {
				_run();
			} catch(Exception e) {
				logger.error("Health check thread threw an exception.  Trying to restart in 30 seconds.",e);
				try {
					Thread.sleep(restartInterval);
				} catch (InterruptedException e1) {
					logger.error("Nap interrupted!",e1);
				}
			}
		}
	}
		
	private void _run() {
		settings = modelManager.getGlobalSettings();
		JSONObjectBuilder builder = new JSONObjectBuilder();
		ClusterNodeRequest request = new ClusterNodeRequest();
		request.setSubject(ClusterNodeRequest.HEALTH_CHECK);
		lastCheckExceptions = new Vector<Exception>();
		while(true) {
			synchronized(this) {
				lastCheckExceptions.clear();
				for(ClusterNode clusterNode : settings.getClusterNodes()) {
					try {
						request.setClusterNode(clusterNode);
						HttpResponse response = null;
						try {
							response = httpRequestExecuter.postContent(
									clusterNode.getServiceWebUrlPrefix()+"/service-management/?action="+ClusterNodeRequest.HEALTH_CHECK+"&auth="+AuthTokenProvider.newAuthToken(settings.getServiceManagementAuthSalt()), 
									builder.toJSON(request).toString(3), FormConstants.CONT_TYPE_JSON);
						} catch(Exception e) {
							logger.error(clusterNode.getServiceWebUrlPrefix()+" health check returned exception",e);
							Throwable t = ExceptionUtils.getRootCause(e);
							ClusterNode.Status err = null;
							if( t instanceof ConnectException ) {
								err = ClusterNode.Status.CONNECT_ERROR;
							} else {
								err = ClusterNode.Status.ERROR;
							}
							synchronized(clusterNode) {
								clusterNode.setLastStatus(err);
								clusterNode.setLastStatusMessage(t.getMessage());
								clusterNode.setLastStatusCheck(new Date());
							}
							if(response!=null && response.getResponseBody()!=null) {
								Utils.consumeInputStream(response.getResponseBody());
								response.getResponseBody().close();
							}
							continue;
						}
						if( response!=null && response.getStatusCode()==200 ) {
							String json = Utils.readInputStream(response.getResponseBody(), FormConstants.CHAR_ENC_DEFAULT);
							JSONObject jsonObj = new JSONObject(json);
							Result result = (Result) builder.fromJSON(jsonObj, new Result());
							response.getResponseBody().close();
							synchronized(clusterNode) {
								clusterNode.setLastStatus(
										result.getStatus()==Result.Status.SUCCESS
										? ClusterNode.Status.GOOD
										: ClusterNode.Status.ERROR);
								clusterNode.setLastStatusMessage(result.getMessage());
								clusterNode.setLastStatusCheck(new Date());
							}
						} else {
							synchronized(clusterNode) {
								clusterNode.setLastStatus(ClusterNode.Status.ERROR);
								String msg = "Service node "+clusterNode.getServiceWebUrlPrefix()+" returned a non-200 status code "+response.getStatusCode()
									+" "+Utils.readInputStream(response.getResponseBody(), FormConstants.CHAR_ENC_DEFAULT);
								logger.error(msg);
								clusterNode.setLastStatusMessage(msg);
								response.getResponseBody().close();
								clusterNode.setLastStatusCheck(new Date());
							}
						}
					} catch(Exception e) {
						logger.error("Exception performing health check",e);
						lastCheckExceptions.add(e);
					}
				}
			}
			synchronized(lastCheckExceptions) {
				lastCheckExceptions.notifyAll();
			}
			try {
				Thread.sleep(checkInterval);
			} catch (InterruptedException e) {
				logger.error("Nap interrupted!",e);
			}
		}
	}
	
	public synchronized void refreshSettings() {
		modelManager.getModelService().clearPersistenceContext();
		settings = modelManager.getGlobalSettings();
	}
	
	public List<Exception> checkNowAndWait() throws InterruptedException {
		synchronized(lastCheckExceptions) {
			lastCheckExceptions.wait();
			return new ArrayList<Exception>(lastCheckExceptions);
		}
	}
	
	public GlobalSettings getSettings() {
		return settings;
	}

	public void setHttpRequestExecuter(HttpRequestExecuter httpRequestExecuter) {
		this.httpRequestExecuter = httpRequestExecuter;
	}

	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}

	public void setCheckInterval(Integer checkInterval) {
		this.checkInterval = checkInterval;
	}

	public void setRestartInterval(Integer restartInterval) {
		this.restartInterval = restartInterval;
	}

}
