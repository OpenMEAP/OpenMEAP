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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.openmeap.constants.FormConstants;
import com.openmeap.thinclient.Preferences;
import com.openmeap.util.GenericRuntimeException;
import com.openmeap.util.Utils;

public class SharedPreferencesImpl implements Preferences {

	private final static String CONNECTION = OpenMEAPApp.STORAGE_ROOT+"/";
	private final static String EXTENSION = ".prefs";
	
	private JSONObject values;
	private String name;
	
	public SharedPreferencesImpl(String name) throws IOException, JSONException {
		this.name = name;
		load();
	}
	
	public String get(String key) {
		try {
			return values.getString(key);
		} catch (JSONException e) {
			return null;
		}
	}

	public Boolean put(String key, String value) {
		try {
			values.put(key, value);
		} catch (JSONException e) {
			return Boolean.FALSE;
		}
		return write();
	}

	public Boolean remove(String key) {
		values.remove(key);
		return write();
	}

	public Boolean clear() {
		try {
			FileConnection fc = (FileConnection)Connector.open(CONNECTION+name+EXTENSION);
			try {
				if( fc.exists() ) {
					try {
						fc.delete();
					} catch (IOException e) {
						return Boolean.FALSE;
					}
				}
			} finally {
				if(fc!=null) {
					fc.close();
				}
			}
		} catch(IOException e) {
			throw new GenericRuntimeException(e);
		}
		return Boolean.TRUE;
	}

	private void load() throws IOException, JSONException {
		FileConnection fc = (FileConnection)Connector.open(CONNECTION+name+EXTENSION);
		try {
			if(fc.exists()) {
				InputStream is = fc.openInputStream();
				String json = "";
				try {
					json = Utils.readInputStream(is,FormConstants.CHAR_ENC_DEFAULT);
				} finally {
					is.close();
				}
				values = new JSONObject(json);
			} else {
				values = new JSONObject();
			}
		} finally {
			if(fc!=null) {
				fc.close();
			}
		}
	}
	
	private Boolean write() {
		try {
			String path = CONNECTION+name+EXTENSION;
			FileConnection fc = (FileConnection)Connector.open(path);
			try {
				if(!fc.exists()) {
					fc.create();
				}
				OutputStream os = fc.openOutputStream();
				try {
					Utils.pipeInputStreamIntoOutputStream(new ByteArrayInputStream(values.toString().getBytes()), os);
				} finally {
					os.close();
				}
			} finally {
				if(fc!=null) {
					fc.close();
				}
			}
		} catch(IOException e) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}	
}
