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

package com.openmeap.constants;

import org.apache.http.protocol.HTTP;

public class FormConstants {
	
	final static public String USERAGENT = "User-Agent";
	final static public String USERAGENT_DEFAULT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:9.0.1) Gecko/20100101 Firefox/9.0.1";
	
	final static public String CONTENT_TYPE = "Content-Type";
	final static public String CONT_TYPE_DEFAULT = "application/x-www-form-urlencoded";
	final static public String CONT_TYPE_XML = "text/xml";
	final static public String CONT_TYPE_HTML = "text/html";
	
	final static public String ENCODING_TYPE = "encodingType";
	final static public String ENCTYPE_MULTIPART_FORMDATA = "multipart/form-data";
	
	final static public String CHAR_ENC_DEFAULT = HTTP.UTF_8;
	
	/*
	 * Page bean names
	 */
	
	final static public String PAGE_BEAN_GLOBAL_SETTINGS = "settingsPage";
	final static public String PAGE_BEAN_APP_ADDMODIFY = "addModifyAppPage";
	final static public String PAGE_BEAN_MAIN = "mainOptionsPage";
	final static public String PAGE_BEAN_APPVER_LISTINGS = "appVersionListingsPage";
	final static public String PAGE_BEAN_APPVER_ADDMODIFY = "addModifyAppVersionPage";
	
	/*
	 * Shared form parameter names
	 */
	final static public String PROCESS_TARGET = "processTarget";
	final static public String PAGE_BEAN = "bean";
	final static public String DELETE = "delete";
	final static public String APP_ID = "applicationId";
	final static public String APPVER_ID = "versionId";
	
	/*
	 * App Add/Modify unique parameter constants
	 */
	final static public String APP_DESCRIPTION = "description";
	final static public String APP_ADMINS = "admins";
	final static public String APP_VERSIONADMINS = "versionAdmins";
	final static public String APP_DEPL_HIST_LEN = "deploymentHistoryLength";
	
	/*
	 * App Version Add/Modify unique parameter constants
	 */
	final static public String APPVER_IDENTIFIER = "identifier";
	final static public String APPVER_NOTES = "notes";
	final static public String UPLOAD_ARCHIVE = "uploadArchive";
	
	/*
	 * Global settings unique parameter constants
	 */
	final static public String GLOBAL_SETTINGS_EXTERNAL_SVC_URL = "externalServiceUrlPrefix";
	final static public String GLOBAL_SETTINGS_MAX_UPLOAD = "maxFileUploadSize";
	final static public String GLOBAL_SETTINGS_AUTH_SALT = "authSalt";		
	final static public String GLOBAL_SETTINGS_STORAGE_PATH_PREFIX = "tempStoragePathPrefix";
	
	/*
	 * App Add/Modify unique parameter string values
	 */
	final static public String APP_DELETE_CONFIRM_TEXT = "delete the application";
	
}
