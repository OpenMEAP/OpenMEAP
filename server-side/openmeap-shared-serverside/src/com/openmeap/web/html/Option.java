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

package com.openmeap.web.html;

public class Option {
	private Boolean selected;
	private String innerText;
	private String value;
	public Option(String value,String innerText,Boolean selected) {
		this.selected = selected;
		this.innerText = innerText;
		this.value = value;
	}
	public Option() {}	
	public void setInnerText(String innerText) {
		this.innerText = innerText;
	}
	public String getInnerText() {
		return innerText;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	public void setIsSelected(Boolean selected) {
		this.selected = selected;
	}
	public Boolean getIsSelected() {
		return this.selected;
	}
	public String getSelected() {
		return selected?"selected":"";
	}
}
