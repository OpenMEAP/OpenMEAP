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

package com.openmeap.android.javascript;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.widget.Toast;

import com.openmeap.android.MainActivity;
import com.openmeap.android.SharedPreferencesImpl;
import com.openmeap.android.WebView;
import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.UpdateHeader;
import com.openmeap.protocol.json.JsError;
import com.openmeap.protocol.json.JsUpdateHeader;
import com.openmeap.thinclient.Preferences;
import com.openmeap.thinclient.javascript.JsApiCore;
import com.openmeap.thinclient.javascript.Orientation;
import com.openmeap.thinclient.update.UpdateException;
import com.openmeap.thinclient.update.UpdateStatus;
import com.openmeap.thinclient.update.UpdateHandler;

public class JsApiCoreImpl implements JsApiCore {
	
	private WebView webView = null;
	private MainActivity activity = null;
	private UpdateHandler updateHandler = null;
	
	public JsApiCoreImpl(MainActivity activity, WebView webView, UpdateHandler updateHandler) {
		this.webView = webView;
		this.activity = activity;
		this.updateHandler = updateHandler;
	}
	
	public Preferences getPreferences(String name) {
		return new SharedPreferencesImpl(activity.getSharedPreferences(name, 0));
	}
	
	public void setTitle(String title) {
		activity.setTitle(title);
	}
	
	public void clearCache() {
		webView.clearCache(true);
	}

	public String getOrientation() {
		Configuration config = activity.getResources().getConfiguration();
		return config.orientation == Configuration.ORIENTATION_LANDSCAPE ? Orientation.LANDSCAPE.toString() : 
			config.orientation == Configuration.ORIENTATION_PORTRAIT ? Orientation.PORTRAIT.toString() :
				config.orientation == Configuration.ORIENTATION_SQUARE ? Orientation.SQUARE.toString() :
					Orientation.UNDEFINED.toString();
	}
	
	public void doToast(String mesg) { doToast(mesg,true); }
 
	public void doToast(String mesg, Boolean isLong) {
		Context context = activity.getApplicationContext();
		CharSequence text = mesg;
		int duration = isLong != null && isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	public String getDeviceType() {
		return this.activity.getConfig().getDeviceType();
	}
	
	public void reload() {
		try {
			activity.restart();
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public Boolean isTimeForUpdateCheck() {
		return this.activity.getConfig().isTimeForUpdateCheck();
	}
	
	public void checkForUpdates(String callBack) {
	
		if( callBack == null || callBack.equals("undefined") ) {
			return;
		}
		
		// TODO: checkForUpdates() needs to branch off into a separate thread, immediately returning control to the calling script.
		UpdateHeader updateHeader = null;
		WebServiceException err = null;
		try {
	    	updateHeader = updateHandler.checkForUpdate();
		} catch( WebServiceException e ) {
			err = e;
		}
		
		webView.setUpdateHeader(updateHeader,err,activity.getStorage().getBytesFree());
		webView.executeJavascriptFunction(callBack,"window.update");
	}

	/**
	 * 
	 * @param header JSON of the update header
	 * @param statusCallBack a status change callback
	 */
	public void performUpdate(final String header, final String statusCallBack) {
		
		// needs to immediately return control to the calling javascript,
		// and pass back download status information via the callback function.
		JsUpdateHeader jsUpdateHeader = null;
		try {
			jsUpdateHeader = new JsUpdateHeader(header);
		} catch(JSONException e) {
			throw new RuntimeException(e);
		}
		UpdateHeader reloadedHeader = jsUpdateHeader.getWrappedObject();
		if( reloadedHeader!=null ) {
			updateHandler.handleUpdate(reloadedHeader, new UpdateHandler.StatusChangeHandler() {
				public void onStatusChange(UpdateStatus update) {
					try {
						JSONObject js = new JSONObject("{update:"+header+"}");
						UpdateException error = update.getError();
						js.put("bytesDownloaded", update.getBytesDownloaded());
						js.put("complete", update.getComplete());
						js.put("error", 
								error!=null 
										? new JsError( error.getUpdateResult().toString(), error.getMessage() ).toJSONObject() 
										: null
								);
						webView.executeJavascriptFunction(statusCallBack,js.toString());
					} catch (JSONException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}	
	
	////////////
}
