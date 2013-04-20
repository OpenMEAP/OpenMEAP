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

package com.openmeap.blackberry;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.openmeap.blackberry.javascript.BlackberryJsCoreApi;
import com.openmeap.constants.FormConstants;
import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.UpdateHeader;
import com.openmeap.protocol.json.JsUpdateHeader;
import com.openmeap.thinclient.LocalStorage;
import com.openmeap.thinclient.OmMainActivity;
import com.openmeap.thinclient.OmWebView;
import com.openmeap.thinclient.OmWebViewHelper;
import com.openmeap.thinclient.SLICConfig;
import com.openmeap.util.GenericRuntimeException;
import com.openmeap.util.StringUtils;
import com.openmeap.util.Utils;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldConnectionManager;
import net.rim.device.api.browser.field2.BrowserFieldController;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.ProtocolController;
import net.rim.device.api.ui.container.MainScreen;

/**
 * A class extending the MainScreen class, which provides default standard
 * behavior for BlackBerry GUI applications.
 */
public final class OpenMEAPScreen extends MainScreen implements OmWebView
{
	private SLICConfig config;
	private LocalStorage localStorage;
	private BrowserField browserField;
	private String DIRECTORY_INDEX = "index.html";
	private OpenMEAPApp activity;
	private AssetsRequestHandler handler;
	
    /**
     * Creates a new OpenMEAPScreen object
     */
    public OpenMEAPScreen(OpenMEAPApp activity, SLICConfig config, LocalStorage localStorage) {        
    	this.activity = activity;
    	this.config = config;
    	this.localStorage = localStorage;
    	
        // Set the displayed title of the screen
        setTitle(config.getApplicationTitle());
        
		try {
			browserField = createBrowserField();
		} catch (IOException e) {
			throw new GenericRuntimeException(e);
		}
    }
    
    private BrowserField createBrowserField() throws IOException {
    	
    	BrowserField browserField = new BrowserField();
    	handler = new AssetsRequestHandler(browserField,activity.getBaseUrl());
    	ProtocolController controller = (ProtocolController)browserField.getController();
    	controller.setNavigationRequestHandler("assets", handler);
    	controller.setResourceRequestHandler("assets", handler);
    	try {
			browserField.extendScriptEngine(OmMainActivity.JS_API_NAME, new BlackberryJsCoreApi(activity,this,activity.getUpdateHandler()));
		} catch (Exception e) {
			throw new GenericRuntimeException(e);
		}
    	add(browserField);
    	return browserField;
    }

	public void runJavascript(String stream) {
		browserField.executeScript(stream);
	}

	public void setUpdateHeader(final UpdateHeader update, final WebServiceException err, final Long bytesFree) {
		final OmWebView webView = this;
		activity.runOnUiThread(new Runnable(){
			public void run() {
				OmWebViewHelper.setUpdateHeader(webView,update,err,bytesFree);
			}
		});
	}

	public void performOnResume() {
		activity.getUpdateHandler().clearInterruptFlag();
	}

	public void performOnPause() {
		activity.getUpdateHandler().interruptRunningUpdate();
	}

	public void executeJavascriptFunction(String callBack, String[] arguments) {
		OmWebViewHelper.executeJavascriptFunction(this, callBack, arguments);
	}

	public void loadDataWithBaseURL(String baseUrl, String pageContent, String mimeType, String sourceEncoding, String historyUrl) {
		try {
			handler = new AssetsRequestHandler(browserField,baseUrl);
	    	ProtocolController controller = (ProtocolController)browserField.getController();
	    	controller.setNavigationRequestHandler("assets", handler);
	    	controller.setResourceRequestHandler("assets", handler);
			browserField.displayContent(pageContent.getBytes(sourceEncoding), mimeType, baseUrl);
		} catch (UnsupportedEncodingException e) {
			throw new GenericRuntimeException(e);
		}
	}
	
	public InputStream getContent(String resourcePath) throws Exception {
		return handler.handleResource(new BrowserFieldRequest(resourcePath)).openInputStream();
	}

	public void clearCache(boolean arg0) {
		
	}

	public void clearView() {
		super.deleteAll();
	}
}
