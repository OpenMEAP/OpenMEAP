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

package com.openmeap.protocol.dto;

import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.JSONGetterSetter;
import com.openmeap.json.JSONProperty;

public class Application implements HasJSONProperties {

    protected ApplicationInstallation installation;
    protected String name;
    protected String versionId;
    protected String hashValue;

    private static JSONProperty[] jsonProperties = new JSONProperty[] {
    	new JSONProperty("installation",ApplicationInstallation.class,
			new JSONGetterSetter(){
				public Object getValue(Object src) {
					return ((Application)src).getInstallation();
				}
				public void setValue(Object dest, Object value) {
					((Application)dest).setInstallation((ApplicationInstallation)value);
				}
    		}),
    	new JSONProperty("name",String.class,
    		new JSONGetterSetter(){
				public Object getValue(Object src) {
					return ((Application)src).getName();
				}
				public void setValue(Object dest, Object value) {
					((Application)dest).setName((String)value);
				}
			}),
    	new JSONProperty("versionId",String.class,
    		new JSONGetterSetter(){
				public Object getValue(Object src) {
					return ((Application)src).getVersionId();
				}
				public void setValue(Object dest, Object value) {
					((Application)dest).setVersionId((String)value);
				}
			}),
    	new JSONProperty("hashValue",String.class,
    		new JSONGetterSetter(){
				public Object getValue(Object src) {
					return ((Application)src).getHashValue();
				}
				public void setValue(Object dest, Object value) {
					((Application)dest).setHashValue((String)value);
				}
			})
    };
    public JSONProperty[] getJSONProperties() {
		return jsonProperties;
	}
    
    public ApplicationInstallation getInstallation() {
        return installation;
    }
    public void setInstallation(ApplicationInstallation value) {
        this.installation = value;
    }

    public String getName() {
        return name;
    }
    public void setName(String value) {
        this.name = value;
    }

    public String getVersionId() {
        return versionId;
    }
    public void setVersionId(String value) {
        this.versionId = value;
    }

    public String getHashValue() {
        return hashValue;
    }
    public void setHashValue(String value) {
        this.hashValue = value;
    }
}
