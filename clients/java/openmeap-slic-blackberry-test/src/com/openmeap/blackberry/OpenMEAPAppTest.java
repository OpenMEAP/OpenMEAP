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

import com.openmeap.blackberry.digest.Md5DigestInputStream;
import com.openmeap.blackberry.digest.Sha1DigestInputStream;
import com.openmeap.digest.DigestInputStreamFactory;

import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.ScreenUiEngineAttachedListener;
import net.rim.device.api.ui.UiApplication;

/**
 * This is the set of unit-tests for blackberry
 */
public class OpenMEAPAppTest extends UiApplication implements ScreenUiEngineAttachedListener
{
	public static final String HTTP_HOST  = "10.0.2.15:8080";
	
	OpenMEAPAppTestScreen screen;
	
	public OpenMEAPAppTest() {
		super();
		
		DigestInputStreamFactory.setDigestInputStreamForName("MD5", Md5DigestInputStream.class);
    	DigestInputStreamFactory.setDigestInputStreamForName("SHA1", Sha1DigestInputStream.class);
		
		screen = new OpenMEAPAppTestScreen();
		screen.addScreenUiEngineAttachedListener(this);
		pushScreen(screen);
	}
	
    /**
     * Entry point for application
     * @param args Command line arguments (not used)
     */ 
    public static void main(String[] args)
    {
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
        OpenMEAPAppTest theApp = new OpenMEAPAppTest();
        theApp.enterEventDispatcher();
    }

	public void onScreenUiEngineAttached(Screen screen, boolean attached) {
		if(attached) {
			this.screen.append("<div style='width:100%;height:100%;overflow:scroll;'>");
			invokeLater(new DigestInputStreamTest(this.screen));
			invokeLater(new HttpRequestExecuterImplTest(this.screen));
			this.screen.append("</div>");
		}
	}    
}
