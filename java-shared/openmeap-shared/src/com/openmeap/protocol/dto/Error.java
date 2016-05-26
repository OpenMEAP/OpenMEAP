/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Rob Thacher                                 #
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

package com.openmeap.protocol.dto;

import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.JSONGetterSetter;
import com.openmeap.json.JSONProperty;

public class Error implements HasJSONProperties {

    protected ErrorCode code;
    protected String message;

    private static JSONProperty[] jsonProperties = new JSONProperty[] {
    	new JSONProperty("code",ErrorCode.class,
        	new JSONGetterSetter() {
	    		public Object getValue(Object src) {
					return ((Error)src).getCode();
				}
				public void setValue(Object dest, Object value) {
					((Error)dest).setCode((ErrorCode)ErrorCode.fromValue((String)value));
				}
			}),
    	new JSONProperty("message",String.class,
    		new JSONGetterSetter() {
	    		public Object getValue(Object src) {
	    				return ((Error)src).getMessage();
				}
				public void setValue(Object dest, Object value) {
					((Error)dest).setMessage((String)value);
				}
    		})
    };
    public JSONProperty[] getJSONProperties() {
		return jsonProperties;
	}
    
    public ErrorCode getCode() {
        return code;
    }
    public void setCode(ErrorCode value) {
        this.code = value;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String value) {
        this.message = value;
    }

}
