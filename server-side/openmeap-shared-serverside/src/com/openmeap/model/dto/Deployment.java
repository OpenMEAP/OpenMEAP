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
import java.util.Comparator;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.openmeap.model.ModelEntity;
import com.openmeap.model.event.AbstractModelEntity;

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
	
	static public class DateComparator implements Comparator<Deployment> {
		public int compare(Deployment arg0, Deployment arg1) {
			return arg0.getCreateDate().compareTo(arg1.getCreateDate()) > 0 ? -1 : 1;
		}
	}
	
	private Long id;
	private Application application;
	private ApplicationArchive applicationArchive;
	private String versionIdentifier;
	private Date createDate;
	private Deployment.Type type;
	private String creator;
	
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

	@ManyToOne(fetch=FetchType.LAZY,cascade={},targetEntity=ApplicationArchive.class)
	@JoinColumn(name="archive_id")
	public ApplicationArchive getApplicationArchive() {
		return applicationArchive;
	}
	public void setApplicationArchive(ApplicationArchive applicationArchive) {
		this.applicationArchive = applicationArchive;
	}
	
	@ManyToOne(fetch=FetchType.LAZY,cascade={},targetEntity=Application.class,optional=false)
	@JoinColumn(name="application_id",nullable=false)
	public Application getApplication() {
		return application;
	}
	public void setApplication(Application application) {
		this.application = application;
	}
	
	@Column(name="version_identifier")
	public String getVersionIdentifier() {
		return versionIdentifier;
	}
	public void setVersionIdentifier(String versionIdentifier) {
		this.versionIdentifier = versionIdentifier;
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
	
	@Column(name="creator")
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
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
				+getVersionIdentifier()+", "
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
