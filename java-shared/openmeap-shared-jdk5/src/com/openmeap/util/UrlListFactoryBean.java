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

package com.openmeap.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;

public class UrlListFactoryBean extends ArrayList {

	private static final long serialVersionUID = 6143243343274185728L;
	
	public UrlListFactoryBean() { super(); }
	public UrlListFactoryBean(String listValues) throws MalformedURLException {
		super();
		setListValues(listValues);
	}
	public void setListValues(String commaDelimitedUrls) throws MalformedURLException {
		this.clear();
		List urls = Arrays.asList(commaDelimitedUrls.split(","));
		Iterator urlIter = urls.iterator();
		while(urlIter.hasNext()) {
			String url = (String) urlIter.next(); 
			this.add( new URL(url) );
		}
	}
	public String getListValues() {
		StringBuilder sb = null;
		Iterator urlItr = this.iterator();
		while( urlItr.hasNext() ) {
			URL url = (URL) urlItr.next();
			if( sb!=null )
				sb.append(',');
			else sb = new StringBuilder();
			sb.append(url.toString());
		}
		return sb.toString();
	}
}
