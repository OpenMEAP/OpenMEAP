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

package com.openmeap.services.dto;

import com.openmeap.json.EnumUtils;
import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.JSONProperty;

/**
 * Generic result to return from the service-management servlet
 * @author schang
 */
public class Result implements HasJSONProperties {
	
	static public class Status implements com.openmeap.json.Enum {
		static final public Status SUCCESS=new Status("SUCCESS");
		static final public Status FAILURE=new Status("FAILURE");
	    private final String value;
	    private Status(String v) {
	        value = v;
	    }
	    public String value() {
	        return value;
	    }
	    static public Status[] values() {
	    	return (Status[])EnumUtils.values(Status.class);
	    }
	    static public Status fromValue(String v) {
	    	return (Status)EnumUtils.fromValue(Status.class, v);
	    }
	};
	
	private Status resultStatus;
	private String message;
	
	static final private JSONProperty[] jsonProperties = new JSONProperty[] {
		new JSONProperty("getStatus"),
		new JSONProperty("getMessage")
	};
	@Override
	public JSONProperty[] getJSONProperties() {
		return jsonProperties;
	}
	
	public Result() {
	}
	public Result(Status status, String message) {
		setStatus(status);
		setMessage(message);
	}
	public Result(Status status) {
		setStatus(status);
		setMessage(status.toString());
	}
	
	public Status getStatus() {
		return resultStatus;
	}
	public void setStatus(Status result) {
		this.resultStatus = result;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}