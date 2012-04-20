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

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.ui.container.MainScreen;

import com.openmeap.util.StringUtils;

public class OpenMEAPAppTestScreen extends MainScreen {
	
	BrowserField browserField;
	private static String PASS = "<span style='color:#0b0;'>PASS";
	private static String FAIL = "<span style='color:#b00;'>FAIL";
	private static String CLOSE = "</span><br/>";
	
	public OpenMEAPAppTestScreen()
    {        
		setTitle("MyTitle");
		BrowserFieldConfig config = new BrowserFieldConfig();
		config.setProperty(BrowserFieldConfig.JAVASCRIPT_ENABLED, Boolean.TRUE);
		browserField = new BrowserField(config);
    	add(browserField);
    	browserField.displayContent("<html><body></body></html>", "file:///Store");
    }

	public void assertTrue(final String mesg, final boolean testResult) {
		String prnt=testResult?"PASS":"FAIL";
		append(
				(testResult?PASS:FAIL)
				+(mesg!=null?" "+mesg:"")
				+CLOSE
			);
	}
	
	public void append(final String html) {
		getApplication().invokeLater(new Runnable() {
			public void run() {
				browserField.executeScript("document.body.innerHTML=document.body.innerHTML+\""+escape(html)+"\"");
			}
		});
	}
	
	public void wrap(final String start, final String end) {
		getApplication().invokeLater(new Runnable() {
			public void run() {
				browserField.executeScript("document.body.innerHTML=\""+escape(start)+"\"+document.body.innerHTML+\""+escape(end)+"\"");
			}
		});
	}
	
	private String escape(String html) {
		return StringUtils.replaceAll(html, "\"", "\\\"");
	}
}
