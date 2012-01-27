/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.util;

import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;

public class UrlListFactoryBean extends ArrayList<URL> {
	public UrlListFactoryBean() { super(); }
	public UrlListFactoryBean(String listValues) throws MalformedURLException {
		super();
		setListValues(listValues);
	}
	public void setListValues(String commaDelimitedUrls) throws MalformedURLException {
		String[] urls = commaDelimitedUrls.split(",");
		this.clear();
		for( String url : urls )
			this.add( new URL(url) );
	}
	public String getListValues() {
		StringBuilder sb = null;
		for( URL url : this ) {
			if( sb!=null )
				sb.append(',');
			else sb = new StringBuilder();
			sb.append(url.toString());
		}
		return sb.toString();
	}
}
