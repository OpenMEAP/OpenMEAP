/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.JSONGetterSetter;
import com.openmeap.json.JSONProperty;

public class UpdateNotification implements HasJSONProperties {

	protected OperationResult result;
	protected String authToken;

	private static JSONProperty[] jsonProperties = new JSONProperty[] {
			new JSONProperty("result", OperationResult.class,
					new JSONGetterSetter() {

						public Object getValue(Object src) {
							return ((UpdateNotification) src).getResult();
						}

						public void setValue(Object dest, Object value) {
							((UpdateNotification) dest)
									.setResult((OperationResult) OperationResult.fromValue((String)value));
						}

					}),
			new JSONProperty("authToken", String.class, new JSONGetterSetter() {

				public Object getValue(Object src) {
					return ((UpdateNotification) src).getAuthToken();
				}

				public void setValue(Object dest, Object value) {
					((UpdateNotification) dest).setAuthToken((String) value);
				}

			}) };

	public JSONProperty[] getJSONProperties() {
		return jsonProperties;
	}

	public OperationResult getResult() {
		return result;
	}

	public void setResult(OperationResult value) {
		this.result = value;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String value) {
		this.authToken = value;
	}

}
