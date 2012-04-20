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

package com.openmeap.blackberry;

import java.io.IOException;
import java.io.InputStream;

import com.openmeap.constants.FormConstants;
import com.openmeap.thinclient.LocalStorage;
import com.openmeap.thinclient.SLICConfig;
import com.openmeap.util.GenericRuntimeException;
import com.openmeap.util.Utils;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldConnectionManager;
import net.rim.device.api.browser.field2.BrowserFieldController;
import net.rim.device.api.browser.field2.ProtocolController;
import net.rim.device.api.ui.container.MainScreen;

/**
 * A class extending the MainScreen class, which provides default standard
 * behavior for BlackBerry GUI applications.
 */
public final class OpenMEAPScreen extends MainScreen
{
	private SLICConfig config;
	private LocalStorage localStorage;
	private String DIRECTORY_INDEX = "index.html";
	
    /**
     * Creates a new OpenMEAPScreen object
     */
    public OpenMEAPScreen(SLICConfig config, LocalStorage localStorage)
    {        
    	this.config = config;
    	this.localStorage = localStorage;
    	
        // Set the displayed title of the screen
        setTitle(config.getApplicationTitle());
        
		try {
			createBrowserField();
		} catch (IOException e) {
			throw new GenericRuntimeException(e);
		}
    }
    
    public BrowserField createBrowserField() throws IOException {
    	
    	String baseUrl = getBaseUrl();
    	
    	BrowserField browserField = new BrowserField();

    	AssetsRequestHandler handler = new AssetsRequestHandler(browserField,baseUrl);
    	ProtocolController controller = (ProtocolController)browserField.getController();
    	controller.setNavigationRequestHandler("assets", handler);
    	controller.setResourceRequestHandler("assets", handler);
    	
    	add(browserField);
    	browserField.requestContent(baseUrl+DIRECTORY_INDEX);
    	return browserField;
    }
    
    /**
     * Loads in the content of the index document page to load into the WebView
     * 
     * @return The url of the index document
     * @throws IOException
     */
    protected String getBaseUrl() throws IOException {
    	// storage location will be null until the first successful update
    	if( config.shouldUseAssetsOrSdCard().booleanValue() ) {
   			return config.getPackagedAppRoot();
    	} else {
    		return config.getStorageLocation();
    	}
    }
}
