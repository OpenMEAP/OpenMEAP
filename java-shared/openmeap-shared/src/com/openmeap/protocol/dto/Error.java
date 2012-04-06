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

package com.openmeap.protocol.dto;

import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.JSONProperty;

public class Error implements HasJSONProperties {

    protected ErrorCode code;
    protected String message;

    private static JSONProperty[] jsonProperties = new JSONProperty[] {
    	new JSONProperty("getCode"),
    	new JSONProperty("getMessage")
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
