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

package com.openmeap.cluster.dto;

import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.JSONProperty;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.event.ModelEntityEventAction;

public class ClusterNodeRequest implements HasJSONProperties {

	public final static String MODEL_REFRESH = ModelEntityEventAction.MODEL_REFRESH.getActionName();
	public final static String CLEAR_PERSISTENCE_CONTEXT = "clearPersistenceContext";
	public final static String ARCHIVE_DELETE = ModelEntityEventAction.ARCHIVE_DELETE.getActionName();
	public final static String ARCHIVE_UPLOAD = ModelEntityEventAction.ARCHIVE_UPLOAD.getActionName();
	public final static String HEALTH_CHECK = "healthCheck";
	
	private ClusterNode clusterNode;
	private String subject;
	private HasJSONProperties content;
	
	private static JSONProperty[] properties = new JSONProperty[] {
				new JSONProperty("getClusterNode"),
				new JSONProperty("getSubject"),
				new JSONProperty("getContent")
			};
	
	@Override
	public JSONProperty[] getJSONProperties() {
		return properties;
	}

	public void setClusterNode(ClusterNode clusterNode) {
		this.clusterNode = clusterNode;
	}
	public ClusterNode getClusterNode() {
		return clusterNode;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getSubject() {
		return subject;
	}

	public void setContent(HasJSONProperties content) {
		this.content = content;
	}
	public HasJSONProperties getContent() {
		return content;
	}

}
