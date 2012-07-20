/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2012 OpenMEAP, Inc.                                   #
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

package com.openmeap.android;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import com.openmeap.http.CredentialsProviderFactory;
import com.openmeap.thinclient.LoginFormCallback;
import com.openmeap.thinclient.LoginFormLauncher;

@SuppressWarnings("rawtypes")
public class OmSlicCredentialsProvider extends BasicCredentialsProvider implements LoginFormCallback {

	static public class Factory extends CredentialsProviderFactory {

		private LoginFormLauncher launcher;
		public Factory(LoginFormLauncher launcher) {
			this.launcher = launcher;
		}
		public CredentialsProvider newCredentialsProvider() {
			return new OmSlicCredentialsProvider(launcher);
		}
	}
	
	private LoginFormLauncher launcher;
	private String password=null;
	private String username=null;
	private Boolean remember=null;
	private AuthScope authScope=null;
	static private Map<AuthScope,Credentials> memory = new HashMap<AuthScope,Credentials>();
	
	OmSlicCredentialsProvider(LoginFormLauncher launcher) {
		this.launcher = launcher;
	}
	
	public String getInfoText() {
		if( authScope==null ) {
			return null;
		}
		return authScope.getHost() + ":" + authScope.getPort() + "\n" + authScope.getRealm();
	}
	
	@Override
	public void clear() {
		super.clear();
	}
	
	@Override
	public Credentials getCredentials(final AuthScope authScope) {
		
		this.authScope = authScope;
		
		if( memory.containsKey(authScope) ) {
			return memory.get(authScope);
		}
		
		synchronized(this) {
			try {
				launcher.launchLoginForm(this);
				wait();
			} catch( InterruptedException ie ) {
				throw new RuntimeException(ie);
			}
		}
		
		if( username==null || password==null ) {
			return super.getCredentials(authScope);
		}
		
		Credentials creds = new UsernamePasswordCredentials(username,password);
		
		if( remember==null || remember==false ) {
			password = null;
			username = null;
		} else {
			memory.put(authScope, creds);
		}
		
		return creds;
	}	
	
	public AuthScope getAuthScope() {
		return authScope;
	}
	
	public synchronized void onCancel() {
		this.password = null;
		this.username = null;
		this.remember = null;
		notify();
	}
	
	public synchronized void onProceed(String username, String password, Boolean remember) {
		this.password = password;
		this.username = username;
		this.remember = remember;
		notify();
	}
}
