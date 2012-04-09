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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.openmeap.constants.FormConstants;
import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.JSONProperty;
import com.openmeap.model.AbstractModelEntity;
import com.openmeap.web.form.Parameter;

@Entity @Table(name="application_version")
public class ApplicationVersion extends AbstractModelEntity {
	
	private Date createDate;
	private String identifier;
	private ApplicationArchive archive;
	private Application application;
	private Long id;
	private String notes;
	private Boolean activeFlag = true;
	private String lastModifier;
	
	// TODO: the primary key for this class should really be a composite key
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Transient public Long getPk() { return getId(); }
	public void setPk( Object pkValue ) { setId((Long)pkValue); }
	
	@Basic(optional=false)
	@Column(name="create_date",insertable=false,updatable=false,columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	@Column(name="last_modifier")
	public String getLastModifier() {
		return lastModifier;
	}
	public void setLastModifier(String lastModifier) {
		this.lastModifier = lastModifier;
	}
	
	/**
	 * @return The chosen version identifier
	 */
	@Column(name="identifier",nullable=false)
	@Parameter(FormConstants.APPVER_IDENTIFIER)
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String id) {
		identifier = id;
	}
	
	@Column(name="notes",length=4000)
	@Parameter(FormConstants.APPVER_NOTES)
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	@Column(name="active_flag")
	public Boolean getActiveFlag() {
		return activeFlag;
	}
	public void setActiveFlag(Boolean active) {
		this.activeFlag = active;
	}
	
	/**
	 * @return The binary archive of the application and associated integrity information.
	 */
	@OneToOne(fetch=FetchType.EAGER,cascade={CascadeType.ALL},targetEntity=ApplicationArchive.class)
	@JoinColumn(name="archive_id")
	public ApplicationArchive getArchive() {
		return archive;
	}
	public void setArchive(ApplicationArchive archive) {
		this.archive = archive;
	}
	
	/**
	 * @return The application which this version is of.
	 */
	@ManyToOne(cascade={}) @JoinColumn(name="application_id",nullable=false)
	public Application getApplication() {
		return application;
	}
	public void setApplication(Application app) {
		application = app;
	}
	
	public Map<Method,String> validate() {
		try {
			Map<Method,String> errors = new HashMap<Method,String>();
			if( this.getIdentifier()==null )
				errors.put( this.getClass().getMethod("getIdentifier"), "must contain an identifier");
			if( this.getApplication()==null )
				errors.put( this.getClass().getMethod("getApplication"), "must be associated to an application");
			
			// validate the archive
			if( this.getArchive()==null 
					|| this.getArchive().getVersion()==null 
					|| (this.getArchive().getVersion().getId()!=null 
							&& this.getArchive().getVersion().getId().compareTo(this.getId())!=0 ) )
				errors.put( this.getClass().getMethod("getArchive"), "The version must have an archive");
			else {
				Map<Method,String> archiveErrors = this.getArchive().validate();
				if( archiveErrors!=null ) {
					errors.putAll(archiveErrors);
				}
			}
			
			if( errors.size()>0 )
				return errors;
			return null;
		} catch(NoSuchMethodException nsme) {
			throw new RuntimeException(nsme);
		}
	}
}
