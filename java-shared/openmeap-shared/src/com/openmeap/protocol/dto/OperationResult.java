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

import java.lang.reflect.Field;

public class OperationResult {

	static final public OperationResult SUCCESS = new OperationResult("success");
	static final public OperationResult FAILURE = new OperationResult("failure");
    private final String value;

    OperationResult(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OperationResult fromValue(String v) {
    	Field[] fields = OperationResult.class.getDeclaredFields();
    	for( int fieldIdx=0; fieldIdx<fields.length; fieldIdx++ ) {
    		Field field = fields[fieldIdx];
    		try {
	    		if( ((OperationResult)field.get(null)).value().equals(v) ) {
	    			return (OperationResult)field.get(null);
					
	    		}
    		} catch(Exception e) {
    			throw new IllegalArgumentException(v);
    		}
    	}
    	throw new IllegalArgumentException(v);
    }


}
