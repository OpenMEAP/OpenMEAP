/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
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

package com.openmeap.cluster;

import java.net.URL;

import com.openmeap.event.EventNotificationException;

/**
 * @author schang
 */
public class ClusterNotificationException extends EventNotificationException {

	private URL url;
	
	public URL getUrl() {
		return url;
	}
	
	public ClusterNotificationException(URL url) {
		super();
		this.url=url;
	}
	
	public ClusterNotificationException(String arg0) {
		super(arg0);
	}
	
	public ClusterNotificationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.url=url;
	}

	public ClusterNotificationException(URL url, String arg0) {
		super(arg0);
		this.url=url;
	}

	public ClusterNotificationException(URL url, Throwable arg0) {
		super(arg0);
		this.url=url;
	}

	public ClusterNotificationException(URL url, String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.url=url;
	}

}
