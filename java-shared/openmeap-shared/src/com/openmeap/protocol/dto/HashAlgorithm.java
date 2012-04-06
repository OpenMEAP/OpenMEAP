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
import java.util.ArrayList;
import java.util.List;

import com.openmeap.json.Enum;

public class HashAlgorithm implements Enum {

	static final public HashAlgorithm MD5 = new HashAlgorithm("MD5");
	static final public HashAlgorithm SHA1 = new HashAlgorithm("SHA1");

    private final String value;

    private HashAlgorithm(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
    
    static public HashAlgorithm[] values() {
    	List list = new ArrayList();
    	Field[] fields = HashAlgorithm.class.getDeclaredFields();
    	for( int fieldIdx=0; fieldIdx<fields.length; fieldIdx++ ) {
    		try {
				list.add(fields[fieldIdx].get(null));
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
    	}
    	return (HashAlgorithm[])list.toArray();
    }

    public static HashAlgorithm fromValue(String v) {
    	Field[] fields = HashAlgorithm.class.getDeclaredFields();
    	for( int fieldIdx=0; fieldIdx<fields.length; fieldIdx++ ) {
    		Field field = fields[fieldIdx];
    		try {
	    		if( ((HashAlgorithm)field.get(null)).value().equals(v) ) {
	    			return (HashAlgorithm)field.get(null);
					
	    		}
    		} catch(Exception e) {
    			throw new IllegalArgumentException(v);
    		}
    	}
    	throw new IllegalArgumentException(v);
    }

}
