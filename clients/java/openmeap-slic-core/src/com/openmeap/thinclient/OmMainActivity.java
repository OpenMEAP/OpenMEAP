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

import java.io.IOException;

import com.openmeap.thinclient.javascript.Orientation;
import com.openmeap.thinclient.update.UpdateHandler;

public interface OmMainActivity {
	
	public static final String JS_API_NAME = "OpenMEAP_Core";
	
	public void restart();
	public SLICConfig getConfig();
	public Preferences getPreferences(String name);
	public void setTitle(String title);
	public Orientation getOrientation();
	public LocalStorage getStorage();
	public void doToast(String mesg, boolean isLong);
	public UpdateHandler getUpdateHandler();
	public String getRootWebPageContent() throws IOException;
	
	public void setReadyForUpdateCheck(boolean state);
	public boolean getReadyForUpdateCheck();
	
	/**
	 * Creates the default web view and extends the javascript engine with the JsApiCoreImpl attached at "OpenMEAP_Core";
	 * @return
	 */
	public OmWebView createDefaultWebView();
	public void setContentView(OmWebView webView);
	public void runOnUiThread(Runnable runnable);
	public void setWebView(OmWebView webView);
}


