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

package com.openmeap.services.dto;

import com.openmeap.json.Enum;
import com.openmeap.json.EnumUtils;
import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.JSONGetterSetter;
import com.openmeap.json.JSONProperty;

/**
 * Generic result to return from the service-management servlet
 * @author schang
 */
public class Result implements HasJSONProperties {
	
	static public class Status implements com.openmeap.json.Enum {
		static final public Status SUCCESS=new Status("SUCCESS");
		static final public Status FAILURE=new Status("FAILURE");
		static final private Status[] constants = new Status[]{SUCCESS,FAILURE};
	    private final String value;
	    private Status(String v) {
	        value = v;
	    }
	    public String value() {
	        return value;
	    }
	    static public Status[] values() {
	    	return constants;
	    }
	    static public Status fromValue(String v) {
	    	return (Status)EnumUtils.fromValue(Status.SUCCESS, v);
	    }
		@Override
		public Enum[] getStaticConstants() {
			return constants;
		}
	};
	
	private Status resultStatus;
	private String message;
	
	static final private JSONProperty[] jsonProperties = new JSONProperty[] {
		new JSONProperty("status",Status.class,
			new JSONGetterSetter(){
				public Object getValue(Object src) {
					return ((Result)src).getStatus();
				}
				@Override
				public void setValue(Object dest, Object value) {
					((Result)dest).setStatus(Status.fromValue((String)value));
				}
		}),
		new JSONProperty("message",String.class,
			new JSONGetterSetter(){
				public Object getValue(Object src) {
					return ((Result)src).getMessage();
				}
				@Override
				public void setValue(Object dest, Object value) {
					((Result)dest).setMessage((String)value);
				}
		})
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
