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

import com.openmeap.json.Enum;
import com.openmeap.json.EnumUtils;

public class UpdateType implements Enum {

    final static public UpdateType REQUIRED = new UpdateType("REQUIRED");
    final static public UpdateType OPTIONAL = new UpdateType("OPTIONAL");
    final static public UpdateType IMMEDIATE = new UpdateType("IMMEDIATE");
    
    private final String value;
    private UpdateType(String v) {
        value = v;
    }
    public String value() {
        return value;
    }
    static public UpdateType[] values() {
    	return (UpdateType[])EnumUtils.values(UpdateType.class);
    }
    static public UpdateType fromValue(String v) {
    	return (UpdateType)EnumUtils.fromValue(UpdateType.class, v);
    }

}
