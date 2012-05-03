package com.openmeap.blackberry;

import java.io.InputStream;

import javax.microedition.io.InputConnection;

import com.openmeap.constants.FormConstants;
import com.openmeap.util.Utils;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldNavigationRequestHandler;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.BrowserFieldResourceRequestHandler;

public class AssetsRequestHandler implements BrowserFieldResourceRequestHandler, BrowserFieldNavigationRequestHandler {
	
	private BrowserField browserField = null;
	private static String ASSETS_PREFIX = "assets://";
	private String baseUrl = null;
	
	public AssetsRequestHandler(BrowserField field, String baseUrl) {
		this.browserField = field;
		this.baseUrl = baseUrl;
	}

	public void handleNavigation(BrowserFieldRequest request) throws Exception {
		InputStream stream = System.class.getResourceAsStream( request.getURL().substring(ASSETS_PREFIX.length()) );
		String content = Utils.readInputStream(stream, FormConstants.CHAR_ENC_DEFAULT);
		browserField.displayContent(content.getBytes(FormConstants.CHAR_ENC_DEFAULT), "text/html", baseUrl);
	}

	public InputConnection handleResource(BrowserFieldRequest request) throws Exception {
		return new AssetsInputConnection(request.getURL().substring(ASSETS_PREFIX.length()));
	}

}
