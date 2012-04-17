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
import com.openmeap.util.Utils;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.ui.container.MainScreen;

/**
 * A class extending the MainScreen class, which provides default standard
 * behavior for BlackBerry GUI applications.
 */
public final class OpenMEAPScreen extends MainScreen
{
    /**
     * Creates a new OpenMEAPScreen object
     */
    public OpenMEAPScreen()
    {        
        // Set the displayed title of the screen       
        setTitle("MyTitle");
		try {
			createBrowserField();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public BrowserField createBrowserField() throws IOException {
    	BrowserField browserField = new BrowserField();
    	add(browserField);
    	InputStream stream = OpenMEAPScreen.class.getResourceAsStream("test.html");
    	browserField.displayContent(Utils.readInputStream(stream, FormConstants.CHAR_ENC_DEFAULT),"file:///Store");
    	return browserField;
    }
}
