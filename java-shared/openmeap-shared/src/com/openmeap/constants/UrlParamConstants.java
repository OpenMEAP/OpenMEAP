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

public class UrlParamConstants {
	
	/**
	 * The action of the request
	 */
	final static public String ACTION = "action";
	
	/**
	 * Authentication, either for service or for application (context dependent)
	 */
	final static public String AUTH_TOKEN = "auth";
	
	/**
	 * The Application.name
	 */
	final static public String APP_NAME = "app-name";
	
	/**
	 * The ApplicationVersion.identifier
	 */
	final static public String APP_VERSION = "app-version";
	
	/**
	 * The assigned/generated device uuid
	 */
	final static public String DEVICE_UUID = "device-uuid";
	
	/**
	 * The version of the SLIC container
	 */
	final static public String SLIC_VERSION = "slic-version";
	
	/**
	 * ModelServiceRefreshNotifier object-to-refresh type (Class.simpleName)
	 */
	final static public String REFRESH_TYPE = "type";
	
	/**
	 * ModelServiceRefreshNotifier object-to-refresh pk id
	 */
	final static public String REFRESH_OBJ_PKID = "id";
	
	/**
	 * ApplicationArchive hash
	 */
	final static public String APPARCH_HASH = "hash";
	
	/**
	 * ApplicationArchive hash-algorithm
	 */
	final static public String APPARCH_HASH_ALG = "hash-algorithm";
	
	final static public String APPARCH_FILE = "file";
	
	final static public String CLUSTERNODE_KEY = "clusterNodeKey";
	
}
