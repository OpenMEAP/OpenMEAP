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

package com.openmeap.thinclient.javascript;

import com.openmeap.thinclient.Preferences;

/**
 * The interface for the Javascript API.
 * 
 * Intended to be implemented in all Java-based SLIC implementations.
 * 
 * @author schang
 */
public interface JsApiCore {

	public String getOrientation();
	
	public void doToast(String mesg);
	
	public void doToast(String mesg, Boolean isLong);
	
	/**
	 * Obtain the name of the device type
	 * @return
	 */
	public String getDeviceType();
	
	/**
	 * Sets the title of the window.
	 * Only effective if the application is configured to have a title.
	 * @param title
	 */
	public void setTitle(String title);
	
	/**
	 * Clear's the cache of the WebView
	 */
	public void clearCache();
	
	/**
	 * Creates or retrieves a preferences object
	 * @param name
	 * @return
	 */
	public Preferences getPreferences(String name);
	
	public Boolean isTimeForUpdateCheck();
	
	/**
	 * Connect to Application management and check for available updates.
	 * 
	 * callBack should be of the form: <code>function(updateHeader) { ... }</code>.
	 * If a new deployment is available, the <code>updateHeader</code> passed in will be of the form:
	 * <code>
	 * 	{
	 * 	};
	 * </code>
	 * 
	 * @param callBack Javascript to execute when done checking.  Should accept a single object parameter 
	 */
	public void checkForUpdates(String callBack);
	
	/**
	 * @param updateHeader The update to perform
	 * @param statusCallBack a javascript function to pass status information back to
	 */
	public void performUpdate(final String header, final String statusCallBack);
	
	/**
	 * Reloads the application
	 */
	public void reload();
}

