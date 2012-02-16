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

import java.lang.reflect.Method;
import java.util.*;

import javax.persistence.*;

import com.openmeap.model.AbstractModelEntity;

@Entity @Table(name="application")
public class Application extends AbstractModelEntity {
	
	private Long id;
	private Map<String,ApplicationVersion> versions;
	private String name;
	private String description;
	private String proxyAuthSalt;
	private String versionAdmins;
	private String admins;
	private String initialVersionIdentifier;
	private List<Deployment> deployments;
	private Integer deploymentHistoryLength = 10;
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Override @Transient public Long getPk() { return getId(); }
	@Override public void setPk( Object pkValue ) { setId((Long)pkValue); }
	
	@Column(name="name",unique=true,nullable=false)
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name=name;
	}
	
	@Column(name="description",length=4000)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @param admins A space-delimited list of users/roles that may modify the application (except name)
	 */
	public void setAdmins(String admins) {
		this.admins = admins;
	}
	@Column(name="admins",length=4000)
	public String getAdmins() {
		return admins;
	}
	
	/**
	 * @param admins A space-delimited list of users/roles that may create/modify versions for the application
	 */
	public void setVersionAdmins(String admins) {
		versionAdmins = admins;
	}
	@Column(name="version_admins",length=4000)
	public String getVersionAdmins() {
		return versionAdmins;
	}
	
	/**
	 * @param salt Salt to create HMAC SHA-1 authentication tokens with for validation in the proxy
	 */
	public void setProxyAuthSalt(String salt) {
		this.proxyAuthSalt = salt;
	}
	@Column(name="proxy_auth_salt")
	public String getProxyAuthSalt() {
		return proxyAuthSalt;
	}
	
	/**
	 * @param versions All versions of the application ever deployed.
	 */
	public void setVersions(Map<String,ApplicationVersion> versions) {
		this.versions = versions;
	}
	@OneToMany(mappedBy="application",fetch=FetchType.LAZY,cascade={CascadeType.ALL},targetEntity=ApplicationVersion.class)
	@MapKey(name="identifier")
	public Map<String,ApplicationVersion> getVersions() {
		return versions;
	}
	public void addVersion(ApplicationVersion version) {
		if( versions==null ) {
			versions = new HashMap<String,ApplicationVersion>();
		}
		version.setApplication(this);
		versions.put(version.getIdentifier(), version);
	}
	public void removeVersion(ApplicationVersion version) {
		if( versions==null ) {
			return;
		}
		ApplicationVersion v = versions.get(version.getIdentifier());
		if( v!=null ) {
			v.setApplication(null);
			versions.remove(v.getIdentifier());
		}
	}
	
	/**
	 * @param deployments Deployments created for the application
	 */
	public void setDeployments(List<Deployment> deployments) {
		this.deployments = deployments;
	}
	@OneToMany(mappedBy="application",fetch=FetchType.LAZY,cascade={CascadeType.ALL},targetEntity=Deployment.class)
	public List<Deployment> getDeployments() {
		return deployments;
	}	
	public void addDeployment(Deployment d) {
		d.setApplication(this);
		if( deployments == null ) {
			deployments = new ArrayList<Deployment>();
		}
		deployments.add(d);
	}
	public void removeDeployment(Deployment d) {
		d.setApplication(null);
		if( deployments == null ) {
			return;
		}
		int idx = deployments.indexOf(d);
		if( idx!=(-1) ) {
			deployments.remove(idx);
		}
	}
	
	/**
	 * The number of deployments to keep in history.
	 * @return
	 */
	@Column(name="depl_hist_len")
	public Integer getDeploymentHistoryLength() {
		return deploymentHistoryLength;
	}
	public void setDeploymentHistoryLength(Integer deploymentHistoryLength) {
		this.deploymentHistoryLength = deploymentHistoryLength;
	}
	
	@Column(name="initial_version_id",length=255)
	public String getInitialVersionIdentifier() {
		return initialVersionIdentifier;
	}
	public void setInitialVersionIdentifier(String initialVersionIdentifier) {
		this.initialVersionIdentifier = initialVersionIdentifier;
	}
	
	public int hashCode() {
		return getName()!=null?getName().hashCode():(-1);
	}
	
	public boolean equals(Application app) {
		return app.getName()!=null && getName()!=null && getName().equals(app.getName());
	}
	
	public Map<Method,String> validate() {
		try {
			Map<Method,String> errors = new HashMap<Method,String>();
			
			if( this.getName()==null )
				errors.put( this.getClass().getMethod("getName"), "must contain a name");
			
			if( this.getProxyAuthSalt()==null || this.getProxyAuthSalt().length()==0 )
				this.setProxyAuthSalt(UUID.randomUUID().toString());
			
			if( errors.size()>0 )
				return errors;
			
			else return null;
		} catch( NoSuchMethodException nsme ) {
			throw new RuntimeException(nsme);
		}
	}
}
