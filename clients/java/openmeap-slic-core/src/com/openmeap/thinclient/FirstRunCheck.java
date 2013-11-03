/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

import java.io.ByteArrayInputStream;

import com.openmeap.http.HttpRequestExecuter;
import com.openmeap.http.HttpResponse;
import com.openmeap.util.Utils;

/**
 * Makes the first run hit to usage.openmeap.com.
 *
 * Dear Developer/Project Management,
 *
 * We've created this function to make a single hit to our tracking url
 * per unique install.  An effort has been made to make it secure and
 * non-reversible.  An effort has been made so that, even should it fail,
 * it only happens once.
 *
 * We're hoping you'll leave this code functional in your production 
 * release so that we can build-up value by tracking unique installs.
 *
 * Yours truly, OpenMEAP
 */
public class FirstRunCheck implements Runnable {
	private SLICConfig config;
	private String macAddress;
	private HttpRequestExecuter executer;
	public FirstRunCheck(SLICConfig config, String macAddress, HttpRequestExecuter executer) {
		this.config = config;
		this.macAddress = macAddress;
		this.executer = executer;
	}
	public void run() {
		if( config.isDevelopmentMode().equals(Boolean.TRUE) ) {
			return;
		}
		if( config.getNotFirstRun()==null ) {
			config.setNotFirstRun(Boolean.TRUE);
			try {
				String macWithSalt = macAddress+".OPENMEAP#$!@3__234";
				String hashValue = Utils.hashInputStream("sha1", new ByteArrayInputStream(macWithSalt.getBytes("UTF-8")));
				HttpResponse response = executer.get("http://usage.openmeap.com/tracker.gif?hash="+hashValue);
				Utils.consumeInputStream(response.getResponseBody());
			} catch( Exception ioe ) {
				// again, we don't want to be a bother here...let's just bail
				return;
			}
		}
	}
}
