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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.openmeap.model.AbstractModelEntity;
import com.openmeap.model.ModelEntity;

@Entity @Table(name="deployment")
public class Deployment extends AbstractModelEntity {

	public enum Type {
		/**
		 * This deployment type is administered by the customer-supplied javascript.
		 * It is considered optional and the consumer should be able to opt-out
		 * of it till such time as the customer decides to create a REQUIRED
		 * deployment.
		 */
		OPTIONAL,
		
		/**
		 * The update is administered by the customer-supplied javascript.
		 * It is not optional, though it is not handled by the SLIC differently
		 * than an optional update.  Enforcement of the deployment type
		 * is up to the customer-supplied javascript.
		 */
		REQUIRED,
		
		/**
		 * The entire update is administered by the container, 
		 * having no interaction with the customer-supplied javascript.
		 * This deployment type is intended for emergency situations.
		 */
		IMMEDIATE
	}
	
	private Long id;
	private Application application;
	private ApplicationVersion applicationVersion;
	private Date createDate;
	private Deployment.Type type;
	private String hash;
	private String hashAlgorithm;
	private String downloadUrl;
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Override @Transient public Long getPk() { return getId(); }
	@Override public void setPk( Object pkValue ) { setId((Long)pkValue); }

	@Column(name="type",nullable=false)
	public Deployment.Type getType() {
		return type;
	}
	public void setType(Deployment.Type type) {
		this.type = type;
	}

	@ManyToOne(fetch=FetchType.LAZY,cascade={},targetEntity=Application.class,optional=false)
	public Application getApplication() {
		return application;
	}
	public void setApplication(Application application) {
		this.application = application;
	}
	
	@ManyToOne(fetch=FetchType.LAZY,cascade={},targetEntity=ApplicationVersion.class,optional=false)
	public ApplicationVersion getApplicationVersion() {
		return applicationVersion;
	}
	public void setApplicationVersion(ApplicationVersion applicationVersion) {
		this.applicationVersion = applicationVersion;
	}

	@Basic(optional=false)
	@Column(name="create_date",insertable=true,updatable=false,columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	@Column(name="hash_alg")
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}
	public void setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}
	
	@Column(name="download_url")
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	
	@Override
	public void remove() {
		if( getApplication()!=null ) {
			getApplication().removeDeployment(this);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if( ! ( o instanceof Deployment ) )
			return false;
		Deployment d = (Deployment)o;
		return d.hashCode()==o.hashCode();
	}
	
	@Override 
	public String toString() {
		return "Deployment("+(application!=null?application.getName():null)+", "
				+(applicationVersion!=null?applicationVersion.getIdentifier():null)+", "
				+getCreateDate()+", "+getType()+")";
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public Map<Method, String> validate() {
		// TODO Auto-generated method stub
		return null;
	}
}
