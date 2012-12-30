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

package com.openmeap.thinclient.javascript;

import com.openmeap.json.Enum;
import com.openmeap.json.EnumUtils;

public class Orientation implements Enum { 
	static final public Orientation PORTRAIT = new Orientation("PORTRAIT");
	static final public Orientation LANDSCAPE = new Orientation("LANDSCAPE"); 
	static final public Orientation SQUARE = new Orientation("SQUARE"); 
	static final public Orientation UNDEFINED = new Orientation("UNDEFINED"); 
	static final private Orientation[] constants = new Orientation[] {
		PORTRAIT, LANDSCAPE, SQUARE, UNDEFINED
	};
    private final String value;
    private Orientation(String v) {
        value = v;
    }
    public String value() {
        return value;
    }
    public boolean equals(Object o) {
    	return o.hashCode()==hashCode();
    }
    public int hashCode() {
    	return value.hashCode();
    }
    static public Orientation[] values() {
    	return constants;
    }
    static public Orientation fromValue(String v) {
    	return (Orientation)EnumUtils.fromValue(UNDEFINED, v);
    }
	public Enum[] getStaticConstants() {
		return constants;
	}
};
