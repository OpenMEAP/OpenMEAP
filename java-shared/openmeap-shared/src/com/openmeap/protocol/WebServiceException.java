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

package com.openmeap.protocol;

import com.openmeap.json.Enum;
import com.openmeap.json.EnumUtils;
import com.openmeap.protocol.dto.ErrorCode;
import com.openmeap.util.GenericException;

/**
 * Provides a bridge to the xml generated type errorcode enum
 * 
 * @author schang
 */
public class WebServiceException extends GenericException {

	public TypeEnum type = null;
	
	static public class TypeEnum implements Enum {
		static final public TypeEnum CLIENT = new TypeEnum("CLIENT");
		static final public TypeEnum CLIENT_CONNECTION = new TypeEnum("CLIENT_CONNECTION");
		static final public TypeEnum CLIENT_UPDATE = new TypeEnum("CLIENT_UPDATE");
		static final public TypeEnum APPLICATION_NOTFOUND = new TypeEnum("APPLICATION_NOTFOUND");
		static final public TypeEnum APPLICATION_VERSION_NOTFOUND = new TypeEnum("APPLICATION_VERSION_NOTFOUND");
		static final public TypeEnum MISSING_PARAMETER = new TypeEnum("MISSING_PARAMETER");
		static final public TypeEnum DATABASE_ERROR = new TypeEnum("DATABASE_ERROR");
		static final public TypeEnum UNDEFINED = new TypeEnum("UNDEFINED");
		static final private TypeEnum[] constants = new TypeEnum[] {
			CLIENT,
			CLIENT_CONNECTION,
			CLIENT_UPDATE,
			APPLICATION_NOTFOUND,
			APPLICATION_VERSION_NOTFOUND,
			MISSING_PARAMETER,
			DATABASE_ERROR,
			UNDEFINED
		};
		public String toString() {
			return value();
		}
		public Enum[] getStaticConstants() {
			return constants;
		}
		private String value;
		private TypeEnum(String name) {
			this.value = name;
		};
		public boolean equals(Object o) {
			return this.hashCode()==o.hashCode();
		}
		public int hashCode() {
			return this.value.hashCode();
		}
		public String value() {
			return value;
		}
		public static TypeEnum fromValue(String v) {
	    	return (TypeEnum)EnumUtils.fromValue((Enum)UNDEFINED, v);
	    }
		public ErrorCode asErrorCode() {
			if( APPLICATION_NOTFOUND.equals(this) ) {
				return ErrorCode.APPLICATION_NOTFOUND;
			} else if( APPLICATION_VERSION_NOTFOUND.equals(this) ) {
				return ErrorCode.APPLICATION_VERSION_NOTFOUND;
			} else if( MISSING_PARAMETER.equals(this) ) {
				return ErrorCode.MISSING_PARAMETER;
			} else if( DATABASE_ERROR.equals(this) ) {
				return ErrorCode.DATABASE_ERROR;
			}
			return ErrorCode.UNDEFINED;
		}
	}
	
	public TypeEnum getType() {
		return type;
	}

	public WebServiceException(TypeEnum type) {
		super();
		this.type=type;
	}

	public WebServiceException(TypeEnum type, String message) {
		super(message);
		this.type=type;
	}

	public WebServiceException(TypeEnum type, Throwable cause) {
		super(cause);
		this.type=type;
	}

	public WebServiceException(TypeEnum type, String message, Throwable cause) {
		super(message, cause);
		this.type=type;
	}
	
	static public String toJSON(WebServiceException wse) {
		if(wse==null) {
			return "null";
		}
		return "{type:\""+wse.getType().toString()+"\",message:\""+wse.toString().replace('"','\'')+"\"}";
	}
}
