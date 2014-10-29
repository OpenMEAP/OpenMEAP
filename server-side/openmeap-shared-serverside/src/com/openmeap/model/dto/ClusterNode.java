/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
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

package com.openmeap.model.dto;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.JSONGetterSetter;
import com.openmeap.json.JSONProperty;
import com.openmeap.model.event.AbstractModelEntity;

@Entity @Table(name="cluster_node")
public class ClusterNode extends AbstractModelEntity implements HasJSONProperties {
	
	/**
	 * Status resulting from the health check 
	 */
	public static enum Status {
		CONNECT_ERROR,
		GOOD,
		ERROR
	}
	
	/**
	 * This is the url to the deployment of openmeap-service-web.war
	 * It is used for coordinating events from the server running
	 * the openmeap-admin-web.war.
	 */
	private String serviceWebUrlPrefix;
	private String fileSystemStoragePathPrefix;
	private Long id;
	private Status lastStatus;
	private String lastStatusMessage;
	private Date lastStatusCheck;
	
	public ClusterNode() {}
	public ClusterNode(String serviceUrl, String prefix) {
		this.serviceWebUrlPrefix=serviceUrl;
		this.fileSystemStoragePathPrefix=prefix;
	}
	
	static final private JSONProperty[] jsonProperties = new JSONProperty[] {
		new JSONProperty("serviceWebUrlPrefix",String.class,new JSONGetterSetter(){
			public Object getValue(Object src) {
				return ((ClusterNode)src).getServiceWebUrlPrefix();
			}
			public void setValue(Object dest, Object value) {
				((ClusterNode)dest).setServiceWebUrlPrefix((String)value);
			}
		}),
		new JSONProperty("fileSystemStoragePathPrefix",String.class,new JSONGetterSetter(){
			public Object getValue(Object src) {
				return ((ClusterNode)src).getFileSystemStoragePathPrefix();
			}
			public void setValue(Object dest, Object value) {
				((ClusterNode)dest).setFileSystemStoragePathPrefix((String)value);
			}
		})
	};
	@Override @Transient
	public JSONProperty[] getJSONProperties() {
		return jsonProperties;
	}
	
	@Transient public Long getPk() { return getId(); }
	public void setPk( Object pkValue ) { setId((Long)pkValue); }
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id=id;
	}
	
	@Column(name="svc_web_url_prfx",length=256,unique=true)
	public String getServiceWebUrlPrefix() {
		return serviceWebUrlPrefix;
	}
	public void setServiceWebUrlPrefix(String serviceWebUrlPrefix) {
		this.serviceWebUrlPrefix = serviceWebUrlPrefix;
	}
	
	@Column(name="file_sys_strg_path_prfx", length=512)
	public String getFileSystemStoragePathPrefix() {
		return fileSystemStoragePathPrefix;
	}
	public void setFileSystemStoragePathPrefix(String fileSystemStoragePathPrefix) {
		this.fileSystemStoragePathPrefix = fileSystemStoragePathPrefix;
	}
	public String validateFileSystemStoragePathPrefix() {
		if( fileSystemStoragePathPrefix==null ) {
			return "File system storage path prefix should be set";
		}
		File path = new File(fileSystemStoragePathPrefix);
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
			StringBuilder sb = new StringBuilder("The path \""+fileSystemStoragePathPrefix+"\" has the following issues: ");
			sb.append(StringUtils.join(errors,","));
			return sb.toString();
		}
		return null;
	}
	
	public Map<Method,String> validate() {
		try {
			Map<Method,String> validationMap = new HashMap<Method,String>();
			String validateFS = this.validateFileSystemStoragePathPrefix();
			if(validateFS!=null) {
				validationMap.put(this.getClass().getMethod("getFileSystemStoragePathPrefix"), validateFS);
			}
			if( validationMap.size()>0 ) {
				return validationMap;
			}
			return null;
		} catch( NoSuchMethodException nsme ) {
			throw new RuntimeException(nsme);
		}
	}
	
	@Override
	public int hashCode() {
		return serviceWebUrlPrefix.hashCode() + fileSystemStoragePathPrefix.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if( !(o instanceof ClusterNode)) 
			return false;
		ClusterNode cn = (ClusterNode)o;
		return serviceWebUrlPrefix.equals(cn.getServiceWebUrlPrefix());
	}

	synchronized public void setLastStatus(Status lastStatus) {
		this.lastStatus = lastStatus;
	}
	@Transient synchronized public Status getLastStatus() {
		return lastStatus;
	}

	synchronized public void setLastStatusMessage(String lastStatusMessage) {
		this.lastStatusMessage = lastStatusMessage;
	}
	@Transient synchronized public String getLastStatusMessage() {
		return lastStatusMessage;
	}

	synchronized public void setLastStatusCheck(Date lastStatusCheck) {
		this.lastStatusCheck = lastStatusCheck;
	}
	@Transient synchronized public Date getLastStatusCheck() {
		return lastStatusCheck;
	}
}
