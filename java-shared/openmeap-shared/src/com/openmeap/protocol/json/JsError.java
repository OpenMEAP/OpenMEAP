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

package com.openmeap.protocol.json;


import com.openmeap.protocol.dto.Error;
import com.openmeap.thirdparty.org.json.me.JSONException;
import com.openmeap.thirdparty.org.json.me.JSONObject;
import com.openmeap.util.GenericRuntimeException;


public class JsError {
	private String type;
	private String message;
	
	public JsError(Error error) {
		this.type = error.getCode().value();
		this.message = error.getMessage();
	}
	public JsError(String type, String message) {
		this.type = type;
		this.message = message;
	}
	
	public String toString() {
		return toJSON();
	}
	
	public String toJSON() {
		return toJSONObject().toString();
	}
	
	public JSONObject toJSONObject() {
		JSONObject err = new JSONObject();
		try {
			err.put("type", type);
			err.put("message", message);
		} catch (JSONException e) {
			throw new GenericRuntimeException(e);
		}
		return err;
	}
}

