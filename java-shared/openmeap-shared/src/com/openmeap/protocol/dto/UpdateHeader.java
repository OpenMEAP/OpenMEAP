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

public class UpdateHeader implements HasJSONProperties {

    protected Hash hash;
    protected String versionIdentifier;
    protected Long installNeeds;
    protected Long storageNeeds;
    protected String updateUrl;
    protected UpdateType type;
    protected Long spaceAvailable;

    private static JSONProperty[] jsonProperties = new JSONProperty[] {
    	new JSONProperty("getHash"),
    	new JSONProperty("getVersionIdentifier"),
    	new JSONProperty("getInstallNeeds"),
    	new JSONProperty("getStorageNeeds"),
    	new JSONProperty("getUpdateUrl"),
    	new JSONProperty("getType"),
    	new JSONProperty("getSpaceAvailable")
    };
    public JSONProperty[] getJSONProperties() {
		return jsonProperties;
	}
    
    public Hash getHash() {
        return hash;
    }
    public void setHash(Hash value) {
        this.hash = value;
    }

    public String getVersionIdentifier() {
        return versionIdentifier;
    }
    public void setVersionIdentifier(String value) {
        this.versionIdentifier = value;
    }

    public Long getInstallNeeds() {
        return installNeeds;
    }
    public void setInstallNeeds(Long value) {
        this.installNeeds = value;
    }

    public Long getStorageNeeds() {
        return storageNeeds;
    }
    public void setStorageNeeds(Long value) {
        this.storageNeeds = value;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }
    public void setUpdateUrl(String value) {
        this.updateUrl = value;
    }

    public UpdateType getType() {
        return type;
    }
    public void setType(UpdateType value) {
        this.type = value;
    }
    
	public Long getSpaceAvailable() {
		return spaceAvailable;
	}
	public void setSpaceAvailable(Long spaceAvailable) {
		this.spaceAvailable = spaceAvailable;
	}

}
