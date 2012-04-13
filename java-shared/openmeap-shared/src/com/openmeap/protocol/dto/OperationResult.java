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

public class OperationResult implements Enum {

	static final public OperationResult SUCCESS = new OperationResult("success");
	static final public OperationResult FAILURE = new OperationResult("failure");
	static final private OperationResult[] constants = new OperationResult[] {SUCCESS,FAILURE};    
    private final String value;
    
    OperationResult(String v) {
        value = v;
    }

    public boolean equals(Object o) {
    	return o.hashCode()==this.hashCode();
    }
    
    public int hashCode() {
    	return value.hashCode();
    }
    
    public String value() {
        return value;
    }
    
    public Enum[] getStaticConstants() {
		return constants;
	}

    public static OperationResult fromValue(String v) {
    	return (OperationResult)EnumUtils.fromValue(SUCCESS,v);
    }
}
