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

package com.openmeap.http;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

abstract public class CredentialsProviderFactory {
	
	static private CredentialsProviderFactory credentialsProviderFactory = new CredentialsProviderFactory() {
		public CredentialsProvider newCredentialsProvider() {
			return new BasicCredentialsProvider();
		}
	};
	
	abstract public CredentialsProvider newCredentialsProvider();
	
	static public void setDefaultCredentialsProviderFactory(CredentialsProviderFactory factory) {
		credentialsProviderFactory = factory;
	}
	static public CredentialsProviderFactory getDefaultCredentialsProviderFactory() {
		return credentialsProviderFactory;
	}
	static public CredentialsProvider newDefaultCredentialsProvider() {
		return credentialsProviderFactory.newCredentialsProvider();
	}
}
