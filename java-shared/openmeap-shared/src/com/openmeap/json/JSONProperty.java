/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

package com.openmeap.json;

public class JSONProperty {
	private String propertyName;
	private Class returnType;
	private Class containedType;
	private JSONGetterSetter getterSetter;
	public JSONProperty(String propertyName, Class returnType, JSONGetterSetter jsonGetterSetter) {
		this.propertyName=propertyName;
		this.returnType=returnType;
		this.getterSetter=jsonGetterSetter;
	}
	public JSONProperty(String propertyName, Class returnType, Class containedType, JSONGetterSetter getterSetter) {
		this(propertyName, returnType, getterSetter);
		this.containedType=containedType;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public Class getReturnType() {
		return returnType;
	}
	public Class getContainedType() {
		return containedType;
	}
	public JSONGetterSetter getGetterSetter() {
		return getterSetter;
	}
}
