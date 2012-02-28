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

import com.openmeap.protocol.dto.ErrorCode;

/**
 * Provides a bridge to the xml generated type errorcode enum
 * 
 * @author schang
 */
public class WebServiceException extends Exception {

	public TypeEnum type = null;
	
	public enum TypeEnum {
		CLIENT,
		CLIENT_CONNECTION,
		CLIENT_UPDATE,
		APPLICATION_NOTFOUND,
		APPLICATION_VERSION_NOTFOUND,
		MISSING_PARAMETER,
		DATABASE_ERROR,
		UNDEFINED;
		public ErrorCode asErrorCode() {
			switch(this) {
				case APPLICATION_NOTFOUND:         return ErrorCode.APPLICATION_NOTFOUND; 
				case APPLICATION_VERSION_NOTFOUND: return ErrorCode.APPLICATION_VERSION_NOTFOUND; 
				case MISSING_PARAMETER:            return ErrorCode.MISSING_PARAMETER; 
				case DATABASE_ERROR:               return ErrorCode.DATABASE_ERROR; 
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
