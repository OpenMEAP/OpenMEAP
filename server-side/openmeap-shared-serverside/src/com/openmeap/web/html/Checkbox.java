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

package com.openmeap.web.html;

public class Checkbox {
	private Boolean checked;
	private String value;
	private String name;
	
	/**
	 * @param chk Whether the checkbox should be checked or not
	 */
	public void setIsChecked(Boolean chk) {
		checked = chk;
	}
	public Boolean getIsChecked() {
		return checked;
	}
	public String getChecked() {
		return checked?"checked":"";
	}
	
	/**
	 * @param val The value attribute of the checkbox input
	 */
	public void setValue(String val) {
		value = val;
	}
	public String getValue() {
		return value;
	}
	
	/**
	 * @param name The name attribute of the checkbox input
	 */
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}
