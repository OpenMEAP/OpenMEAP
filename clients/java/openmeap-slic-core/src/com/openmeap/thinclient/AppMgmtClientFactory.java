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

package com.openmeap.thinclient;

import java.lang.reflect.Constructor;

import com.openmeap.protocol.ApplicationManagementService;
import com.openmeap.util.HttpRequestExecuter;
import com.openmeap.util.HttpRequestExecuterFactory;

public class AppMgmtClientFactory {
	
	static private Class defaultClient = RESTAppMgmtClient.class;
	
	static public void setDefaultType(Class defaultNew) {
		if( !ApplicationManagementService.class.isAssignableFrom(defaultNew) ) {
			throw new RuntimeException("setDefaultType only accepts ApplicationManagementService subclasses/implementors");
		}
		defaultClient = defaultNew;		
	}
	static public Class getDefaultType() {
		return defaultClient;
	}
	
	static public ApplicationManagementService newDefault(String serviceUrl) {
		try {
			Constructor constructor = defaultClient.getConstructor(new Class[]{String.class, HttpRequestExecuter.class});
			return (ApplicationManagementService)constructor.newInstance(new Object[]{serviceUrl, HttpRequestExecuterFactory.newDefault()});
		} catch( Exception e ) {
			throw new RuntimeException(e);
		}
	}
}
