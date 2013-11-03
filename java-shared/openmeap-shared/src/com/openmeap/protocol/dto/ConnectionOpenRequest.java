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

package com.openmeap.protocol.dto;

import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.JSONGetterSetter;
import com.openmeap.json.JSONProperty;

public class ConnectionOpenRequest implements HasJSONProperties {

    protected Application application;
    protected SLIC slic;

    private static JSONProperty[] jsonProperties = new JSONProperty[] {
    	new JSONProperty("application",Application.class,
			new JSONGetterSetter(){
				public Object getValue(Object src) {
					return ((ConnectionOpenRequest)src).getApplication();
				}
				public void setValue(Object dest, Object value) {
					((ConnectionOpenRequest)dest).setApplication((Application)value);
				}
			}),
    	new JSONProperty("slic",SLIC.class,
    		new JSONGetterSetter(){
				public Object getValue(Object src) {
					return ((ConnectionOpenRequest)src).getSlic();
				}
				public void setValue(Object dest, Object value) {
					((ConnectionOpenRequest)dest).setSlic((SLIC)value);
				}
			}),
    };
    public JSONProperty[] getJSONProperties() {
		return jsonProperties;
	}
    
    public Application getApplication() {
        return application;
    }
    public void setApplication(Application value) {
        this.application = value;
    }

    public SLIC getSlic() {
        return slic;
    }
    public void setSlic(SLIC value) {
        this.slic = value;
    }

}
