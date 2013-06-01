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

package com.openmeap.android;

import java.io.File;
import java.util.Properties;

import android.os.Environment;

import com.openmeap.thinclient.Preferences;
import com.openmeap.thinclient.SLICConfig;

public class AndroidSLICConfig extends SLICConfig {

	private static String SD_CARD_PREFIX = "sdcard:";
	
	private MainActivity activity;
	
	public AndroidSLICConfig(MainActivity activity, Preferences preferences, Properties properties) {
		super(preferences, properties);
		this.activity = activity;
	}

	public String getProviderAuthority() {
		return this.getProperty("com.openmeap.slic.providerAuthority");
	}
	
	public String getAssetsRootPath() {
		// storage location will be null until the first successful update
    	if( shouldUseAssetsOrSdCard() ) {
    		String root = this.getPackagedAppRoot();    		
    		if( assetsOnExternalStorage() ) {
    			File sdCard = Environment.getExternalStorageDirectory(); 
    			root = sdCard.getAbsolutePath() + "/" + root.substring(SD_CARD_PREFIX.length());
    		}
    		return root;
    	} else {
	    	String path = activity.getFilesDir().getAbsolutePath()+System.getProperty("file.separator");    	
	    	return path;
    	}
	}
	
	/**
     * Get the root uri for resources
     * 
     * There are 3 conditions this will vary under:
     * + the assets are in internal storage under the assets directory
     * + the assets are on the sd card at a pre-specified location
     * + the assets have been previously pulled via update
     * @return
     */
    public String getAssetsBaseUrl() {
    	String rootPath = getAssetsRootPath();
    	if( shouldUseAssetsOrSdCard() ) {
    		if( assetsOnExternalStorage() ) {
    			return "content://com.android.htmlfileprovider/" + rootPath;
    		}
    		return "file:///android_asset/" + rootPath;
    	} else return rootPath;
    }
    
    public Boolean assetsOnExternalStorage() {
    	String root = this.getPackagedAppRoot();
    	return root.startsWith(SD_CARD_PREFIX); 
    }
}
