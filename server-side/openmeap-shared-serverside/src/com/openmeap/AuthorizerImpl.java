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

package com.openmeap;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;


import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.GlobalSettings;

/**
 * Request-scoped implementation of the Authorizer interface
 * @author schang
 */
public class AuthorizerImpl implements Authorizer {
	
	/**
	 * Role may login and poke about, but that's about it.
	 */
	private static String USER_ROLE = "openmeap-user";
	
	/**
	 * Role may do anything
	 */
	private static String ADMIN_ROLE = "openmeap-admin";
	
	/**
	 * Role may do anything with the applications
	 */
	private static String APP_MODIFY_ROLE = "openmeap-application-admin";
	
	/**
	 * Role may create/delete/modify any application's versions
	 */
	private static String VER_ADMIN_ROLE = "openmeap-version-admin";
	
	/**
	 * Role may modify existing versions only
	 */
	private static String VER_MODIFY_ROLE = "openmeap-version-modifier";
	
	private HttpServletRequest request;
	
	public Boolean may(Action action, Object object) {
		// TODO: make this a bit less reflectiony
		
		// the admin may do anything
		if( request.isUserInRole(ADMIN_ROLE) ) {
			return Boolean.TRUE;
		}

		if( object instanceof Application ) {
			return mayApplication(action,(Application)object);
		} else if( object instanceof ApplicationVersion ) {
			return mayAppVersion(action,(ApplicationVersion)object);
		} else if( object instanceof GlobalSettings ) {
			return Boolean.FALSE;
		}
		
		return Boolean.FALSE;
	}
	
	private Boolean isUserOrRoleInList(String userList) {
		List<String> rolesUsers = Arrays.asList(userList.trim().split("\\s+"));
		String userName = request.getUserPrincipal().getName();
		if( rolesUsers.contains(userName) ) {
			return Boolean.TRUE;
		}
		for( String roleOrUser : rolesUsers ) {
			if( request.isUserInRole(roleOrUser) ) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
	
	private Boolean mayApplication(Action action, Application app) {
		
		if( app!=null && app.getAdmins()!=null 
				&& request.getUserPrincipal()!=null && isUserOrRoleInList(app.getAdmins()) ) {
			return Boolean.TRUE;
		}
		
		if( request.isUserInRole(APP_MODIFY_ROLE) && action == Action.MODIFY ) {
			return Boolean.TRUE;
		}
		
		return Boolean.FALSE;
	}
	
	private Boolean mayAppVersion(Action action, ApplicationVersion appVer) {
		
		Application app = appVer!=null && appVer.getApplication()!=null ? appVer.getApplication() : null;
		if( app!=null && app.getVersionAdmins()!=null 
				&& request.getUserPrincipal()!=null && isUserOrRoleInList(app.getVersionAdmins()) ) {
			return Boolean.TRUE;
		}
		
		if( request.isUserInRole(VER_ADMIN_ROLE) ) {
			return Boolean.TRUE;			
		}
		if( request.isUserInRole(VER_MODIFY_ROLE) && action == Action.MODIFY ) {
			return Boolean.TRUE;
		}
		
		return Boolean.FALSE;
	}

	public HttpServletRequest getRequest() {
		return request;
	}
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

}
