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

import java.lang.reflect.Field;

import com.openmeap.protocol.dto.ErrorCode;
import com.openmeap.protocol.dto.HashAlgorithm;

/**
 * Provides a bridge to the xml generated type errorcode enum
 * 
 * @author schang
 */
public class WebServiceException extends Exception {

	public TypeEnum type = null;
	
	static public class TypeEnum {
		static final public TypeEnum CLIENT = new TypeEnum("CLIENT");
		static final public TypeEnum CLIENT_CONNECTION = new TypeEnum("CLIENT_CONNECTION");
		static final public TypeEnum CLIENT_UPDATE = new TypeEnum("CLIENT_UPDATE");
		static final public TypeEnum APPLICATION_NOTFOUND = new TypeEnum("APPLICATION_NOTFOUND");
		static final public TypeEnum APPLICATION_VERSION_NOTFOUND = new TypeEnum("APPLICATION_VERSION_NOTFOUND");
		static final public TypeEnum MISSING_PARAMETER = new TypeEnum("MISSING_PARAMETER");
		static final public TypeEnum DATABASE_ERROR = new TypeEnum("DATABASE_ERROR");
		static final public TypeEnum UNDEFINED = new TypeEnum("UNDEFINED");
		private String name;
		private TypeEnum(String name) {
			this.name = name;
		};
		public boolean equals(Object o) {
			return this.hashCode()==o.hashCode();
		}
		public int hashCode() {
			return this.name.hashCode();
		}
		public String value() {
			return name;
		}
		public static TypeEnum fromValue(String v) {
	    	Field[] fields = TypeEnum.class.getDeclaredFields();
	    	for( int fieldIdx=0; fieldIdx<fields.length; fieldIdx++ ) {
	    		Field field = fields[fieldIdx];
	    		try {
		    		if( ((TypeEnum)field.get(null)).value().equals(v) ) {
		    			return (TypeEnum)field.get(null);
						
		    		}
	    		} catch(Exception e) {
	    			throw new IllegalArgumentException(v);
	    		}
	    	}
	    	throw new IllegalArgumentException(v);
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
		return "{type:\""+wse.getType().toString()+"\",message:\""+wse.toString().replace("\"","\\\"")+"\"}";
	}
}
