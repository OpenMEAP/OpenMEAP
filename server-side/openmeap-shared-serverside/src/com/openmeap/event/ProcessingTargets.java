/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2013 OpenMEAP, Inc.                                   #
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

package com.openmeap.event;

public class ProcessingTargets {
	public static String NAV_SUB          = "com.openmeap.admin.web.SubNavigationLinks";
	public static String NAV_MAIN         = "com.openmeap.admin.web.MainNavigationLinks";
	public static String ADDMODIFY_APP    = "com.openmeap.admin.web.AddModifyApplicationBacking";
	public static String ADDMODIFY_APPVER = "com.openmeap.admin.web.AddModifyApplicationVersionBacking";
	public static String LISTING_APP      = "com.openmeap.admin.web.ApplicationListingsBacking";
	public static String LISTING_APPVER   = "com.openmeap.admin.web.ApplicationVersionListingsBacking";
	public static String MESSAGES         = "com.openmeap.admin.web.MessagesBacking";
	public static String GLOBAL_SETTINGS  = "com.openmeap.admin.web.GlobalSettingsBacking";
	public static String DEPLOYMENTS      = "com.openmeap.admin.web.DeploymentListingsBacking";
}
