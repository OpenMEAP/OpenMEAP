package com.openmeap.android;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import com.openmeap.thinclient.LoginFormCallback;
import com.openmeap.thinclient.LoginFormLauncher;
import com.openmeap.util.CredentialsProviderFactory;

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
