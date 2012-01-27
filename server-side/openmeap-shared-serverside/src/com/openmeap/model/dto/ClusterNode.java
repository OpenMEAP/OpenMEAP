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

package com.openmeap.model.dto;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.openmeap.model.AbstractModelEntity;
import com.openmeap.model.ModelEntity;

@Entity @Table(name="cluster_node")
public class ClusterNode extends AbstractModelEntity {
	
	/**
	 * This is the url to the deployment of openmeap-service-web.war
	 * It is used for coordinating events from the server running
	 * the openmeap-admin-web.war.
	 */
	private String serviceWebUrlPrefix;
	private String fileSystemStoragePathPrefix;
	
	@Transient public String getPk() { return getServiceWebUrlPrefix(); }
	public void setPk( Object pkValue ) { setServiceWebUrlPrefix((String)pkValue); }
	
	@Id @Column(name="svc_web_url_prfx",length=256)
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
}
