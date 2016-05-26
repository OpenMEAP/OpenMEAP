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

package com.openmeap.web.html;

import com.openmeap.constants.FormConstants;

public class ScriptTag {
	private String source;
	private String language  = "Javascript";
	private String charset   = FormConstants.CHAR_ENC_DEFAULT;
	private String type      = "text/javascript";
	public ScriptTag() {}
	public ScriptTag(String source) {
		this.source = source;
	}
	@Override
	public String toString() {
		StringBuilder tag = new StringBuilder();
		tag.append("<script ");
		tag.append("src=\"").append(source).append("\" ");
		if( charset!=null )  tag.append("charset=\"").append(charset).append("\" ");
		if( language!=null ) tag.append("language=\"").append(language).append("\" ");
		if( type!=null )     tag.append("type=\"").append(type).append("\" ");
		tag.append("></script>");
		return tag.toString();
	}
	@Override
	public boolean equals(Object in) {
		ScriptTag tag = (ScriptTag)in;
		return source.equals(tag.getSource());
	}
	@Override
	public int hashCode() {
		return source.hashCode();
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
