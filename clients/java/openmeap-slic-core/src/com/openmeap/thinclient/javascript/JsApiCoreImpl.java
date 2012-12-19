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

package com.openmeap.thinclient.javascript;



import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.UpdateHeader;
import com.openmeap.protocol.json.JsError;
import com.openmeap.protocol.json.JsUpdateHeader;
import com.openmeap.thinclient.LocalStorageException;
import com.openmeap.thinclient.OmMainActivity;
import com.openmeap.thinclient.OmWebView;
import com.openmeap.thinclient.Preferences;
import com.openmeap.thinclient.update.UpdateException;
import com.openmeap.thinclient.update.UpdateHandler;
import com.openmeap.thinclient.update.UpdateStatus;
import com.openmeap.thirdparty.org.json.me.JSONException;
import com.openmeap.thirdparty.org.json.me.JSONObject;
import com.openmeap.util.GenericRuntimeException;

public class JsApiCoreImpl implements JsApiCore {
	
	private OmWebView webView = null;
	private OmMainActivity activity = null;
	private UpdateHandler updateHandler = null;
	
	public JsApiCoreImpl(OmMainActivity activity, OmWebView webView, UpdateHandler updateHandler) {
		this.webView = webView;
		this.activity = activity;
		this.updateHandler = updateHandler;
	}
	
	public Preferences getPreferences(String name) {
		return activity.getPreferences(name);
	}
	
	public void setTitle(String title) {
		activity.setTitle(title);
	}
	
	public void clearCache() {
		webView.clearCache(true);
	}

	public String getOrientation() {
		return activity.getOrientation().toString();
	}
	
	public void doToast(String mesg) { 
		doToast(mesg,Boolean.TRUE); 
	}
 
	public void doToast(final String mesg, final Boolean isLong) {
		boolean longOne = isLong==null?true:isLong.booleanValue();
		activity.doToast(mesg,longOne);
	}
	
	public String getDeviceType() {
		return this.activity.getConfig().getDeviceType();
	}
	
	public void reload() {
		activity.restart();
	}

	public Boolean isTimeForUpdateCheck() {
		return this.activity.getConfig().isTimeForUpdateCheck();
	}
	
	public void checkForUpdates() {
	
		UpdateHeader updateHeader = null;
		WebServiceException err = null;
		try {
			try {
		    	updateHeader = updateHandler.checkForUpdate();
		    	webView.setUpdateHeader(updateHeader,err,activity.getStorage().getBytesFree());
			} catch( WebServiceException e ) {
				webView.setUpdateHeader(null,e,activity.getStorage().getBytesFree());
			}
		} catch (LocalStorageException e) {
			;
		}
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
			throw new GenericRuntimeException(e);
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
						webView.executeJavascriptFunction(statusCallBack,new String[]{js.toString()});
					} catch (JSONException e) {
						throw new GenericRuntimeException(e);
					}
				}
			});
		}
	}	
	
	public void notifyReadyForUpdateCheck() {
		activity.setReadyForUpdateCheck(true);
	}
	
	////////////
}
