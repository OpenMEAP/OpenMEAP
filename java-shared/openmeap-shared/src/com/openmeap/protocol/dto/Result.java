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

public class Result implements HasJSONProperties {

    protected Error error;
    protected ConnectionOpenResponse connectionOpenResponse;

    private static JSONProperty[] jsonProperties = new JSONProperty[] {
    	new JSONProperty("error",Error.class,
    		new JSONGetterSetter(){
				public Object getValue(Object src) {
					return ((Result)src).getError();
				}
				public void setValue(Object dest, Object value) {
					((Result)dest).setError((Error)value);
				}
			}),
    	new JSONProperty("connectionOpenResponse",ConnectionOpenResponse.class,
    		new JSONGetterSetter(){
	    		public Object getValue(Object src) {
					return ((Result)src).getConnectionOpenResponse();
				}
				public void setValue(Object dest, Object value) {
					((Result)dest).setConnectionOpenResponse((ConnectionOpenResponse)value);
				}
    		})
    };
    public JSONProperty[] getJSONProperties() {
		return jsonProperties;
	}
    
    public Error getError() {
        return error;
    }
    public void setError(Error value) {
        this.error = value;
    }

    public ConnectionOpenResponse getConnectionOpenResponse() {
        return connectionOpenResponse;
    }
    public void setConnectionOpenResponse(ConnectionOpenResponse value) {
        this.connectionOpenResponse = value;
    }
}
