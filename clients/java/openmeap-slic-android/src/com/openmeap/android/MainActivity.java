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

package com.openmeap.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.util.Properties;

import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.openmeap.android.javascript.JsApiCoreImpl;
import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.UpdateHeader;
import com.openmeap.protocol.dto.UpdateType;
import com.openmeap.thinclient.FirstRunCheck;
import com.openmeap.thinclient.LoginFormLauncher;
import com.openmeap.thinclient.Preferences;
import com.openmeap.thinclient.SLICConfig;
import com.openmeap.thinclient.update.UpdateHandler;
import com.openmeap.util.HttpRequestExecuter;
import com.openmeap.util.HttpRequestExecuterFactory;
import com.openmeap.util.Utils;

public class MainActivity extends Activity implements LoginFormLauncher<OmSlicCredentialsProvider> {
	
	private static String SOURCE_ENCODING = "utf-8";
	private static String DIRECTORY_INDEX = "index.html";
	
	private static final int LOGIN_DIALOG = 1;
	
	private AndroidSLICConfig config = null;
	private UpdateHandler updateHandler = null;
	private WebView webView = null;
	private LocalStorageImpl storage = null;
	private OmSlicCredentialsProvider credentialsProvider = null;
	
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        HttpRequestExecuterFactory.setDefaultCredentialsProviderFactory(new OmSlicCredentialsProvider.Factory(this));
        
        // setup the SLICConfig instance 
        Preferences prefs = new SharedPreferencesImpl(getSharedPreferences(SLICConfig.PREFERENCES_FILE,MODE_PRIVATE));
        try {
        	Properties props = new Properties();
        	props.load( getAssets().open(SLICConfig.PROPERTIES_FILE) );
        	config = new AndroidSLICConfig(this,prefs,props);
        } catch( IOException ioe ) {
        	// this is a deal breaker...if we cannot read the properties file, then the application is fail
        	throw new RuntimeException("The primary configuration file ("+SLICConfig.PROPERTIES_FILE+") could not be opened.");
        }
        
        // perform our first-run-check
        Object o = getSystemService(Context.WIFI_SERVICE);
        if( o instanceof WifiManager ) {
	        WifiManager wifiManager = (WifiManager)o;
	        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	        if( wifiInfo!=null && wifiInfo.getMacAddress()!=null ) {
	        	Runnable firstRunCheck = new FirstRunCheck(config,wifiInfo.getMacAddress());
	        	new Thread(firstRunCheck).start();
	        }
        }
        
        if( config.isDevelopmentMode() ) {
        	System.setProperty(HttpRequestExecuter.SSL_PEER_NOVERIFY_PROPERTY,"true");
        } else {
        	System.setProperty(HttpRequestExecuter.SSL_PEER_NOVERIFY_PROPERTY,"false");
        }
        
        storage = new LocalStorageImpl(this);
        updateHandler = new UpdateHandler(config,storage);

        setupWindowTitle();
    }
    
    public void launchLoginForm(OmSlicCredentialsProvider credProv) {
    	this.credentialsProvider = credProv;
    	this.runOnUiThread(new Runnable() {
    		public void run() {
		    	showDialog(LOGIN_DIALOG);
    		}
    	});
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      // We do nothing here. We're only handling this to keep orientation
      // or keyboard hiding from causing the WebView activity to restart.
    }
	
	public void restart() throws NameNotFoundException {
		Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}
	
	public AndroidSLICConfig getConfig() {
    	return config;
    }
    
    public LocalStorageImpl getStorage() {
		return storage;
	}
    
    public UpdateHandler getUpdateHandler() {
		return updateHandler;
	}
    
    /*
     * PROTECTED METHODS HERE
     */
    
    @Override
	protected void onSaveInstanceState(Bundle outState) {
		if( webView!=null ) {
			webView.saveState(outState);
		}
	}
	
    @Override
	protected void onResume() {
		super.onResume();
		if( webView!=null ) {
			webView.performOnResume();
		}
		this.initialize();
	}
	
    @Override
	protected void onPause () {
    	if( webView!=null ) {
    		webView.performOnPause();
    	}
		super.onPause();
	}
    
    @Override
    protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case LOGIN_DIALOG:
			dialog = createLoginFormDialog();
			break;
		}
		return dialog;
	}
	
    protected void initialize() {
    
        FileContentProvider.setProviderAuthority(config.getProviderAuthority());        
        storage.setupSystemProperties();
        
        final MainActivity thisActivity = this;
        
        // if this application is configured to fetch updates,
        // then check for them now 
        if( config.shouldPerformUpdateCheck() ) {
        	
        	runOnUiThread(new Runnable() {
        		public void run() {
        			if( webView!=null ) {
        				webView.clearView();
        			}
        		}
        	});
        	new Thread(new Runnable(){
        		public void run() {
        			UpdateHeader update = null;
        			WebServiceException err = null;
		        	try {
		        		update = updateHandler.checkForUpdate();
		        	} catch( WebServiceException wse ) {
		        		err = wse;
		        	}
		        	if( update!=null && update.getType()==UpdateType.IMMEDIATE ) {
		        		try {
		        			updateHandler.handleUpdate(update);
		        			storage.setupSystemProperties();
		        			update=null;
		        		} catch( Exception e ) {
		            		err = new WebServiceException(WebServiceException.TypeEnum.CLIENT_UPDATE,e);
		            	}
		        	}
		        	runOnUiThread(new InitializeWebView(update, err));
        		}
        	}).start();
        } else {
        	runOnUiThread(new InitializeWebView(null, null));
        }
	}
    
    private class InitializeWebView implements Runnable {
    	UpdateHeader update=null;
    	WebServiceException err=null;
    	public InitializeWebView(UpdateHeader update, WebServiceException err) {
    		this.update=update;
    		this.err=err;
    	}
    	public void run() {
	    	// here after, everything is handled by the html and javascript
	        try {
	        	Boolean justUpdated = config.getApplicationUpdated();
	        	if( justUpdated!=null && justUpdated==true ) {
	        		config.setApplicationUpdated(false);
	        	}
	        	String baseUrl = config.getAssetsBaseUrl();
	        	String pageContent = getRootWebPageContent();
	        	webView = createDefaultWebView();
	        	if( justUpdated!=null && justUpdated ) {
	        		webView.clearCache(true);
	        		webView = createDefaultWebView();
	        	}
	        	webView.setUpdateHeader(update, err, storage.getBytesFree());
	        	webView.loadDataWithBaseURL(baseUrl, pageContent, "text/html", SOURCE_ENCODING, null);
	            setContentView(webView);
	        } catch( Exception e ) {
	        	throw new RuntimeException(e);
	        }
	    }
    }
    
	/**
     * Loads in the content of the index document page to load into the WebView
     * 
     * @return The content of the index document
     * @throws IOException
     */
    protected String getRootWebPageContent() throws IOException {
    	InputStream inputStream = null;
    	// storage location will be null until the first successful update
    	if( config.shouldUseAssetsOrSdCard() ) {
    		if( config.assetsOnExternalStorage() ) {
    			String path = config.getAssetsBaseUrl()+DIRECTORY_INDEX;
    			inputStream = getContentResolver().openInputStream(Uri.parse(path));
    		} else {
    			inputStream = getAssets().open(config.getAssetsRootPath()+DIRECTORY_INDEX);
    		}
    	} else inputStream = openFileInput(FileContentProvider.getInternalStorageFileName(DIRECTORY_INDEX));
    	return Utils.readInputStream(inputStream,SOURCE_ENCODING);
    }
    
    /**
     * Sets up the window title, per the properties
     */
    private void setupWindowTitle() {
    	if( config.getApplicationTitle()!=null ) {
        	if( config.getApplicationTitle().equals("off") ) {
        		requestWindowFeature(Window.FEATURE_NO_TITLE);
        	} else {
        		setTitle(config.getApplicationTitle());
        	}
		} else setTitle(config.getApplicationName());
    }
      
    /**
     * Creates the default WebView where we'll run javascript and render content
     */
    private WebView createDefaultWebView() {
    	
    	WebView webView = new com.openmeap.android.WebView(this,this);
    	
    	// make sure javascript and our api is available to the webview
        webView.getSettings().setJavaScriptEnabled(true);
        JsApiCoreImpl jsApi = new JsApiCoreImpl(this,webView,updateHandler);
        webView.addJavascriptInterface(jsApi, "OpenMEAP_Core");
        
        // make sure the web view fills the viewable area
        webView.setLayoutParams( new LinearLayout.LayoutParams(	
        		android.view.ViewGroup.LayoutParams.FILL_PARENT, 
        		android.view.ViewGroup.LayoutParams.WRAP_CONTENT ) );
        
        return webView;
    }
    
    private Dialog createLoginFormDialog() {
    	
    	Context mContext = this;
    	final Dialog dialog = new Dialog(mContext);

    	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
    	final View layout = inflater.inflate(R.layout.login_form,(ViewGroup) findViewById(R.id.login_form_container));
    	
    	dialog.setContentView(layout);
    	dialog.setTitle("Authentication Prompt");
    	
    	AuthScope authScope = credentialsProvider.getAuthScope();
    	String infoText = authScope.getHost() + ":" +
    			authScope.getPort() + "\n" + 
    			authScope.getRealm();
    	TextView infoTextView = (TextView)layout.findViewById(R.id.info);
    	infoTextView.setText(infoText);
    	
    	Button proceedButton = (Button)layout.findViewById(R.id.proceed);
    	proceedButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				EditText passwordText = (EditText)layout.findViewById(R.id.password);
		    	EditText usernameText = (EditText)layout.findViewById(R.id.username);
		    	CheckBox rememberBox = (CheckBox)layout.findViewById(R.id.remember);
				credentialsProvider.onProceed(
						usernameText.getEditableText().toString(), 
						passwordText.getEditableText().toString(), 
						rememberBox.isChecked());
				credentialsProvider=null;
				if( ! rememberBox.isChecked() ) {
					usernameText.setText("");
					passwordText.setText("");
				}
				dialog.dismiss();
			}
    	});
    	
    	Button cancelButton = (Button)layout.findViewById(R.id.cancel);
    	cancelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View view) {
    			EditText passwordText = (EditText)layout.findViewById(R.id.password);
		    	EditText usernameText = (EditText)layout.findViewById(R.id.username);
		    	CheckBox rememberBox = (CheckBox)layout.findViewById(R.id.remember);
    			credentialsProvider.onCancel();
    			credentialsProvider=null;
    			if( ! rememberBox.isChecked() ) {
					usernameText.setText("");
					passwordText.setText("");
				}
				dialog.dismiss();
			}
    	});
    	
    	EditText usernameText = (EditText)layout.findViewById(R.id.username);
    	usernameText.requestFocus();
    	
    	return dialog;
    }
}
