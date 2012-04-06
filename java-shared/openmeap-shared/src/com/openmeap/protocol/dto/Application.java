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

public class Application implements HasJSONProperties {

    protected ApplicationInstallation installation;
    protected String name;
    protected String versionId;
    protected String hashValue;

    private static JSONProperty[] jsonProperties = new JSONProperty[] {
    	new JSONProperty("getInstallation"),
    	new JSONProperty("getName"),
    	new JSONProperty("getVersionId"),
    	new JSONProperty("getHashValue")
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
