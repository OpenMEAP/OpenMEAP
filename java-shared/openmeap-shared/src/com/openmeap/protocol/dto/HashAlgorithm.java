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

import com.openmeap.json.Enum;
import com.openmeap.json.EnumUtils;

public class HashAlgorithm implements Enum {

	static final public HashAlgorithm MD5 = new HashAlgorithm("MD5");
	static final public HashAlgorithm SHA1 = new HashAlgorithm("SHA1");
	static final private HashAlgorithm[] constants = new HashAlgorithm[] {
				MD5,
				SHA1
			};
    private final String value;
    private HashAlgorithm(String v) {
        value = v;
    }
    public String value() {
        return value;
    }
    public Enum[] getStaticConstants() {
    	return constants;
    }
    static public HashAlgorithm[] values() {
    	return (HashAlgorithm[])MD5.getStaticConstants();
    }
    static public HashAlgorithm fromValue(String v) {
    	return (HashAlgorithm)EnumUtils.fromValue(MD5, v);
    }
}
