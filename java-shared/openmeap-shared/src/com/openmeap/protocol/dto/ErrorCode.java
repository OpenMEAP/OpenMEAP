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

public class ErrorCode implements Enum {

	static final public ErrorCode UNDEFINED = new ErrorCode("UNDEFINED");
	static final public ErrorCode MISSING_PARAMETER = new ErrorCode("MISSING_PARAMETER");
	static final public ErrorCode DATABASE_ERROR = new ErrorCode("DATABASE_ERROR");
	static final public ErrorCode AUTHENTICATION_FAILURE = new ErrorCode("AUTHENTICATION_FAILURE");
	static final public ErrorCode APPLICATION_NOTFOUND = new ErrorCode("APPLICATION_NOTFOUND");
	static final public ErrorCode APPLICATION_VERSION_NOTFOUND = new ErrorCode("APPLICATION_VERSION_NOTFOUND");
	static final private ErrorCode[] constants = new ErrorCode[]{
		UNDEFINED,
		MISSING_PARAMETER,
		DATABASE_ERROR,
		AUTHENTICATION_FAILURE,
		APPLICATION_NOTFOUND,
		APPLICATION_VERSION_NOTFOUND
	};
	public Enum[] getStaticConstants() {
		return constants;
	}
	
	private String value;
	private ErrorCode(String name) {
		this.value = name;
	}
    
	public boolean equals(Object o) {
		return this.hashCode()==o.hashCode();
	}
	
	public int hashCode() {
		return this.value.hashCode();
	}
	
    public String value() {
        return value;
    }

    public static ErrorCode valueOf(String v) {
        return fromValue(v);
    }
    
    public static ErrorCode fromValue(String v) {
    	return (ErrorCode)EnumUtils.fromValue((Enum)UNDEFINED, v);
    }
}
