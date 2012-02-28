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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Map;
import java.util.HashMap;

import javax.persistence.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.constants.FormConstants;
import com.openmeap.constants.ServletNameConstants;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.model.AbstractModelEntity;
import com.openmeap.util.AuthTokenProvider;

@Entity @Table(name="application_archive")
public class ApplicationArchive extends AbstractModelEntity {
	
	private Logger logger = LoggerFactory.getLogger(ApplicationArchive.class);
	
	private Long id;
	private String fileDataUrl;
	private String mimeType;
	private String hash;
	private String hashAlgorithm;
	private ApplicationVersion version;
	private Integer bytesLength;
	private Integer bytesLengthUncompressed;
	
	final static public String URL_TEMPLATE = "${globalSettings.externalServiceUrlPrefix}/"+ServletNameConstants.APPLICATION_MANAGEMENT
		+"/?"+UrlParamConstants.ACTION+"=archiveDownload"
		+"&"+UrlParamConstants.APP_NAME+"=${appName}"
		+"&"+UrlParamConstants.AUTH_TOKEN+"=${newAuthToken}"
		+"&"+UrlParamConstants.APP_VERSION+"=${appVersion}";
	
	final static public String HASH_BASED_URL_TEMPLATE = "${globalSettings.externalServiceUrlPrefix}/"+ServletNameConstants.APPLICATION_MANAGEMENT
		+"/?"+UrlParamConstants.ACTION+"=archiveDownload"
		+"&"+UrlParamConstants.APPARCH_HASH+"=${hash}"
		+"&"+UrlParamConstants.AUTH_TOKEN+"=${newAuthToken}"
		+"&"+UrlParamConstants.APPARCH_HASH_ALG+"=${hashAlgorithm}";
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Transient public Long getPk() { return getId(); }
	public void setPk( Object pkValue ) { setId((Long)pkValue); }
	
	@Transient public File getFile(String pathPrefix) {
		return getFile(pathPrefix,getHashAlgorithm(),getHash());
	}
	@Transient static public File getFile(String pathPrefix, String hashAlg, String hash) {
		if( pathPrefix!=null && hashAlg!=null && hash!=null ) {
			return new File(pathPrefix, hash+".zip"); 
		} else return null;
	}
	
	@Transient public File getExplodedPath(String pathPrefix) {
		if( pathPrefix!=null && getHash()!=null && getHashAlgorithm()!=null ) {
			return new File(pathPrefix, getHash()); 
		} else return null;
	}
	
	@Transient public String getDownloadUrl(GlobalSettings settings) {
		return substituteArchiveVariables(settings,getUrl());
	}
	
	/**
	 * Creates a url for downloading the zip file directly.
	 * 
	 * Introduced so that the specific archive of a deployment
	 * can be retrieved, freeing up the version archive to be modified.
	 * 
	 * That a version archive may be modified is intended to support development,
	 * more than anything else.
	 * 
	 * @param settings
	 * 
	 * @return If the url matches the URL_TEMPLATE, then a HASH_BASED_URL_TEMPLATE run through the substitution method.  Else the url.
	 */
	@Transient public String getDirectDownloadUrl(GlobalSettings settings) {
		if( URL_TEMPLATE.equals(this.getUrl()) ) {
			return substituteArchiveVariables(settings,HASH_BASED_URL_TEMPLATE);
		}
		return this.getUrl();
	}
	
	@Transient public String getViewUrl(GlobalSettings settings) {
		return substituteArchiveVariables(settings,"/openmeap-admin-web/web-view/${appName}/${appVersion}/${newAuthToken}/index.html");
	}
	
	@Transient private String substituteArchiveVariables(GlobalSettings settings,String url) {
		String externalServiceUrlPrefix = settings.getExternalServiceUrlPrefix();
		String authSalt = this.getVersion().getApplication().getProxyAuthSalt();
		String newAuthToken = AuthTokenProvider.newAuthToken(authSalt!=null?authSalt:"");
		
		String replUrl = url;
		try {
			// TODO: replace these with constants
			replUrl = replUrl.replace("${globalSettings.externalServiceUrlPrefix}", externalServiceUrlPrefix!=null?externalServiceUrlPrefix:"");
			replUrl = replUrl.replace("${appName}", URLEncoder.encode(getVersion().getApplication().getName(),FormConstants.CHAR_ENC_DEFAULT));
			replUrl = replUrl.replace("${appVersion}", URLEncoder.encode(getVersion().getIdentifier(),FormConstants.CHAR_ENC_DEFAULT));
			replUrl = replUrl.replace("${newAuthToken}", URLEncoder.encode(newAuthToken,FormConstants.CHAR_ENC_DEFAULT));
			replUrl = replUrl.replace("${hash}", URLEncoder.encode(hash,FormConstants.CHAR_ENC_DEFAULT));
			replUrl = replUrl.replace("${hashAlgorithm}", URLEncoder.encode(hashAlgorithm,FormConstants.CHAR_ENC_DEFAULT));
		} catch(UnsupportedEncodingException uee) {
			logger.error("Exception thrown encoding url parameters for url: {}",uee);
		}
		return replUrl;
	}
	
	/**
	 * @return Either an http, https, or app schemed url to the actual version archive.
	 */
	@Column(name="url")
	public String getUrl() {
		/*
		 * Justification:
		 * This could have been a LOB, but:
		 *	- the device will be able to download the archive from anywhere
		 *	- it's simpler to pull a regular http url than to facilitate this via SOAP
		 *	- storing this as a reference reduces load on the database 
		 */
		return fileDataUrl;
	}
	public void setUrl(String url) {
		this.fileDataUrl = url;
	}
	
	/**
	 * @return A hash the artifact residing at the url can be verified with.
	 */
	@Column(name="hash")
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	/**
	 * @return The algorithm ran against the archive to generate the hash.
	 */
	@Column(name="hash_algorithm")
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}
	public void setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}
	
	/**
	 * @return The version of the application the archive is associated to.
	 */
	@OneToOne(fetch=FetchType.EAGER,cascade={CascadeType.ALL},targetEntity=ApplicationVersion.class)
	@JoinColumn(name="version_id")
	public ApplicationVersion getVersion() {
		return version;
	}
	public void setVersion(ApplicationVersion version) {
		this.version = version;
	}
	
	@Column(name="bytes_length")
	public Integer getBytesLength() {
		return bytesLength;
	}
	public void setBytesLength(Integer bytesLength) {
		this.bytesLength=bytesLength;
	}
	
	@Column(name="bytes_length_uncompressed")
	public Integer getBytesLengthUncompressed() {
		return bytesLengthUncompressed;
	}
	public void setBytesLengthUncompressed(Integer bytesLength) {
		bytesLengthUncompressed = bytesLength;
	}
	
	public Map<Method,String> validate() {
		try {
			Map<Method,String> errors = new HashMap<Method,String>();
			if( this.getHash()==null ) {
				errors.put( this.getClass().getMethod("getHash"), "The archive must have a hash");
			}
			if( this.getHashAlgorithm()==null ) {
				errors.put( this.getClass().getMethod("getHashAlgorithm"), "The archive must have a valid hash algorithm.  The valid hash algorithms are enumerated in the openmeap protocol xsd.");
			} else {
				try {
					com.openmeap.protocol.dto.HashAlgorithm.fromValue(this.getHashAlgorithm());
				} catch( IllegalArgumentException iae ) {
					errors.put( this.getClass().getMethod("getHashAlgorithm"), "The archive must have a valid hash algorithm.  The valid hash algorithms are enumerated in the openmeap protocol xsd.");
				}
			}
			if( this.getUrl()==null ) {
				errors.put( this.getClass().getMethod("getUrl"), "The archive must have a url.");
			}
			if( this.getBytesLength()==null || this.getBytesLength()<1 ) {
				errors.put( this.getClass().getMethod("getBytesLength"), "The archive must be a number of bytes greater than 1.");
			}
			if( this.getBytesLengthUncompressed()==null || this.getBytesLengthUncompressed()<1 ) {
				errors.put( this.getClass().getMethod("getBytesLengthUncompressed"), "The uncompressed archive must be a number of bytes greater than 1.");
			}
			if( errors.size()>0 ) {
				return errors;
			}
			return null;
		} catch( NoSuchMethodException nsme ) {
			throw new RuntimeException(nsme);
		}
	}
}
