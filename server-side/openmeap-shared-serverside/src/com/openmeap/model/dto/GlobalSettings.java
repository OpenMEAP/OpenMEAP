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

package com.openmeap.model.dto;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.openmeap.constants.FormConstants;
import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.JSONProperty;
import com.openmeap.model.AbstractModelEntity;
import com.openmeap.model.ModelEntity;
import com.openmeap.web.form.Parameter;
import com.openmeap.web.form.Validation;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Do not instantiate this, get it from the ModelManager
 * @author schang
 */
@Entity @Table(name="global_settings")
public class GlobalSettings extends AbstractModelEntity implements HasJSONProperties {
	private Long id = null;
	private List<ClusterNode> clusterNodes = null;
	private String temporaryStoragePath = null;
	private String serviceManagementAuthSalt = null;
	private Integer maxFileUploadSize = 1000000;
	
	static final private JSONProperty[] jsonProperties = new JSONProperty[] {
		new JSONProperty("getExternalServiceUrlPrefix"),
		new JSONProperty("getMaxFileUploadSize"),
		new JSONProperty("getServiceManagementAuthSalt"),
		new JSONProperty("getTemporaryStoragePath"),
		new JSONProperty("getClusterNodes",ClusterNode.class)
	};
	@Override @Transient
	public JSONProperty[] getJSONProperties() {
		return jsonProperties;
	}
	
	/**
	 * This is the external url which is used as the root for uploaded archives.
	 * It should be externally accessible from the organization,
	 * as devices will be connecting to it to download new versions of the software.
	 */
	private String externalServiceUrlPrefix = null;
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Transient public Long getPk() { return getId(); }
	public void setPk( Object pkValue ) { setId((Long)pkValue); }
	
	@Column(name="external_svc_url")
	@Parameter(FormConstants.GLOBAL_SETTINGS_EXTERNAL_SVC_URL)
	public String getExternalServiceUrlPrefix() {
		return externalServiceUrlPrefix;
	}
	public void setExternalServiceUrlPrefix(String externalServiceUrlPrefix) {
		this.externalServiceUrlPrefix = externalServiceUrlPrefix;
	}
	
	@Column(name="max_file_upload_size")
	@Parameter(FormConstants.GLOBAL_SETTINGS_MAX_UPLOAD)
	public Integer getMaxFileUploadSize() {
		return maxFileUploadSize;
	}
	public void setMaxFileUploadSize(Integer size) {
		maxFileUploadSize = size;		
	}
	
	@Column(name="svc_mgmt_auth_salt",length=4000)
	@Parameter(value=FormConstants.GLOBAL_SETTINGS_AUTH_SALT,password=true,validation=@Validation(verify=true))
	public String getServiceManagementAuthSalt() {
		return serviceManagementAuthSalt;
	}
	public void setServiceManagementAuthSalt(String serviceManagementAuthSalt) {
		this.serviceManagementAuthSalt = serviceManagementAuthSalt;
	}
	
	@Column(name="temp_strg_path",length=4000)
	@Parameter(FormConstants.GLOBAL_SETTINGS_STORAGE_PATH_PREFIX)
	public String getTemporaryStoragePath() {
		return temporaryStoragePath;
	}
	public void setTemporaryStoragePath(String temporaryStoragePath) {
		this.temporaryStoragePath = temporaryStoragePath;
	}
	public String validateTemporaryStoragePath() {
		if( temporaryStoragePath==null ) {
			return "Temporary storage path should be set";
		}
		File path = new File(temporaryStoragePath);
		List<String> errors = new ArrayList<String>();
		if( ! path.exists() ) {
			errors.add("does not exist");
		} else {
			if( ! path.canWrite() ) {
				return "not writable";
			}
			if( ! path.canRead() ) {
				return "not readable";
			}
		}
		if( errors.size()>0 ) {
			StringBuilder sb = new StringBuilder("The path \""+temporaryStoragePath+"\" has the following issues: ");
			sb.append(StringUtils.join(errors,","));
			return sb.toString();
		}
		return null;
	}
	
	@Override public int hashCode() {
		return id!=null?id.intValue():0;
	}
	
	@OneToMany(fetch=FetchType.EAGER,cascade={CascadeType.ALL},targetEntity=ClusterNode.class)
	@Lob
	public List<ClusterNode> getClusterNodes() {
		return clusterNodes;
	}
	public void setClusterNodes(List<ClusterNode> clusterNodes) {
		this.clusterNodes = clusterNodes;
	}
	public ClusterNode getClusterNode(String serviceUrlPrefix) {
		for(ClusterNode node : clusterNodes) {
			if( node.getServiceWebUrlPrefix().equals(serviceUrlPrefix)){
				return node;
			}
		}
		return null;
	}
	public Boolean addClusterNode(ClusterNode node){
		
		if(clusterNodes==null) {
			clusterNodes = new ArrayList<ClusterNode>();
		}
		if(!clusterNodes.contains(node)) {
			clusterNodes.add(node);
			return true;
		} 
		return false;
	}
	public Boolean removeClusterNode(ClusterNode node){
		
		if(clusterNodes==null) {
			clusterNodes = new ArrayList<ClusterNode>();
		}
		if(clusterNodes.contains(node)) {
			return clusterNodes.remove(node);
		} 
		return false;
	}
	
	public Map<Method,String> validate() {
		try {
			Map<Method,String> errors = new HashMap<Method,String>();
			String error = validateTemporaryStoragePath();
			if( error!=null ) {
				errors.put(this.getClass().getMethod("getTemporaryStoragePath"),error);
			}
			if( errors.size()>0 )
				return errors;
			return null;
		} catch( NoSuchMethodException nsme ) {
			throw new RuntimeException(nsme);
		}
	}
}
