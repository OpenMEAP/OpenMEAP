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

public class LinkTag {
	private String href;
	private String rel   = "stylesheet";
	private String media = "screen";
	private String type  = "text/css";
	public LinkTag() {}
	public LinkTag(String href) {
		this.href = href;
	}
	@Override
	public String toString() {
		StringBuilder tag = new StringBuilder();
		tag.append("<link ");
		tag.append("href=\"").append(href).append("\" ");
		if( rel!=null )   tag.append("rel=\"").append(rel).append("\" ");
		if( media!=null ) tag.append("media=\"").append(media).append("\" ");
		if( type!=null )  tag.append("type=\"").append(type).append("\" ");
		tag.append("></link>");
		return tag.toString();
	}
	@Override
	public boolean equals(Object in) {
		LinkTag tag = (LinkTag)in;
		return href.equals(tag.getHref());
	}
	@Override
	public int hashCode() {
		return href.hashCode();
	}
	public String getRel() {
		return rel;
	}
	public void setRel(String rel) {
		this.rel = rel;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public String getMedia() {
		return media;
	}
	public void setMedia(String media) {
		this.media = media;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
