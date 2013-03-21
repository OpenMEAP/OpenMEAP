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

package com.openmeap.android;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.GeolocationPermissions.Callback;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.openmeap.constants.FormConstants;
import com.openmeap.digest.DigestInputStreamFactory;
import com.openmeap.digest.Md5DigestInputStream;
import com.openmeap.digest.Sha1DigestInputStream;
import com.openmeap.http.CredentialsProviderFactory;
import com.openmeap.http.HttpRequestExecuter;
import com.openmeap.http.HttpRequestExecuterFactory;
import com.openmeap.http.HttpRequestExecuterImpl;
import com.openmeap.thinclient.FirstRunCheck;
import com.openmeap.thinclient.LoginFormCallback;
import com.openmeap.thinclient.LoginFormLauncher;
import com.openmeap.thinclient.OmMainActivity;
import com.openmeap.thinclient.OmWebView;
import com.openmeap.thinclient.Preferences;
import com.openmeap.thinclient.SLICConfig;
import com.openmeap.thinclient.javascript.JsApiCoreImpl;
import com.openmeap.thinclient.javascript.Orientation;
import com.openmeap.thinclient.update.UpdateHandler;
import com.openmeap.util.Utils;

public class MainActivity extends Activity implements OmMainActivity,
LoginFormLauncher {
    
	private static String SOURCE_ENCODING = FormConstants.CHAR_ENC_DEFAULT;
	private static String DIRECTORY_INDEX = "index.html";
    
	private static final int LOGIN_DIALOG = 1;
    
	private AndroidSLICConfig config = null;
	private UpdateHandler updateHandler = null;
	private WebView webView = null;
	private LocalStorageImpl storage = null;
	private LoginFormCallback loginFormCallback = null;
	private boolean readyForUpdateCheck = false;
    
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		// Get rid of the android title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
        
		CredentialsProviderFactory
        .setDefaultCredentialsProviderFactory(new OmSlicCredentialsProvider.Factory(
                                                                                    this));
		HttpRequestExecuterFactory
        .setDefaultType(HttpRequestExecuterImpl.class);
		DigestInputStreamFactory.setDigestInputStreamForName("md5",
                                                             Md5DigestInputStream.class);
		DigestInputStreamFactory.setDigestInputStreamForName("sha1",
                                                             Sha1DigestInputStream.class);
        
		// setup the SLICConfig instance
		Preferences prefs = new SharedPreferencesImpl(getSharedPreferences(
                                                                           SLICConfig.PREFERENCES_FILE, MODE_PRIVATE));
		try {
			Properties props = new Properties();
			props.load(getAssets().open(SLICConfig.PROPERTIES_FILE));
			config = new AndroidSLICConfig(this, prefs, props);
		} catch (IOException ioe) {
			// this is a deal breaker...if we cannot read the properties file,
			// then the application is fail
			throw new RuntimeException("The primary configuration file ("
                                       + SLICConfig.PROPERTIES_FILE + ") could not be opened.");
		}
        
		// perform our first-run-check
		Object o = getSystemService(Context.WIFI_SERVICE);
		if (o instanceof WifiManager) {
			WifiManager wifiManager = (WifiManager) o;
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo != null && wifiInfo.getMacAddress() != null) {
				Runnable firstRunCheck = new FirstRunCheck(config,
                                                           wifiInfo.getMacAddress(),
                                                           HttpRequestExecuterFactory.newDefault());
				new Thread(firstRunCheck).start();
			}
		}
        
		if (config.isDevelopmentMode()) {
			System.setProperty(HttpRequestExecuter.SSL_PEER_NOVERIFY_PROPERTY,
                               "true");
		} else {
			System.setProperty(HttpRequestExecuter.SSL_PEER_NOVERIFY_PROPERTY,
                               "false");
		}
        
		storage = new LocalStorageImpl(this);
		updateHandler = new UpdateHandler(this, config, storage);
        
		// Calls the title from client.properties
		// setupWindowTitle();
	}
    
	public void launchLoginForm(LoginFormCallback credProv) {
		this.loginFormCallback = credProv;
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
    
	public void restart() {
		Intent i = getBaseContext().getPackageManager()
        .getLaunchIntentForPackage(getBaseContext().getPackageName());
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
    
	public Preferences getPreferences(String name) {
		return new SharedPreferencesImpl(this.getSharedPreferences(name, 0));
	}
    
	public Orientation getOrientation() {
		Configuration config = getResources().getConfiguration();
		return config.orientation == Configuration.ORIENTATION_LANDSCAPE ? Orientation.LANDSCAPE
        : config.orientation == Configuration.ORIENTATION_PORTRAIT ? Orientation.PORTRAIT
        : config.orientation == Configuration.ORIENTATION_SQUARE ? Orientation.SQUARE
        : Orientation.UNDEFINED;
	}
    
	public void doToast(String mesg, boolean isLong) {
		Context context = getApplicationContext();
		CharSequence text = mesg;
		int duration = isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
    
	public void setTitle(String title) {
		super.setTitle(title);
	}
    
	/*
	 * PROTECTED METHODS HERE
	 */
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (webView != null) {
			webView.saveState(outState);
		}
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		if (webView != null) {
			webView.performOnResume();
		}
		this.initialize();
	}
    
	@Override
	protected void onPause() {
		if (webView != null) {
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
        
		updateHandler.initialize(webView);
	}
    
	/**
	 * Loads in the content of the index document page to load into the WebView
	 *
	 * @return The content of the index document
	 * @throws IOException
	 */
	public String getRootWebPageContent() throws IOException {
		InputStream inputStream = null;
		// storage location will be null until the first successful update
		if (config.shouldUseAssetsOrSdCard()) {
			if (config.assetsOnExternalStorage()) {
				String path = config.getAssetsBaseUrl() + DIRECTORY_INDEX;
				inputStream = getContentResolver().openInputStream(
                                                                   Uri.parse(path));
			} else {
				String rootPath = config.getAssetsRootPath() + DIRECTORY_INDEX;
				inputStream = getAssets().open(rootPath);
			}
		} else
			inputStream = openFileInput(FileContentProvider
                                        .getInternalStorageFileName(DIRECTORY_INDEX));
		return Utils.readInputStream(inputStream, SOURCE_ENCODING);
	}
    
	/**
	 * Sets up the window title, per the properties
	 *
	 * private void setupWindowTitle() { if( config.getApplicationTitle()!=null
	 * ) { if( config.getApplicationTitle().equals("off") ) {
	 * requestWindowFeature(Window.FEATURE_NO_TITLE); } else {
	 * setTitle(config.getApplicationTitle()); } } else
	 * setTitle(config.getApplicationName()); }
	 */
    
	/**
	 * Creates the default WebView where we'll run javascript and render content
	 */
	public WebView createDefaultWebView() {
        
		WebView webView = new com.openmeap.android.WebView(this, this);
        
		// make sure javascript and our api is available to the webview
        
		JsApiCoreImpl jsApi = new JsApiCoreImpl(this, webView, updateHandler);
		webView.addJavascriptInterface(jsApi, JS_API_NAME);
        
		webView.getSettings().setJavaScriptEnabled(true);
        
		// enable navigator.geolocation
		webView.getSettings().setGeolocationEnabled(true);
		webView.getSettings().setGeolocationDatabasePath(
                                                         "/data/data/com.openmeap.android/databases/");
        
		webView.getSettings().setDomStorageEnabled(true);
		// removes vertical and horizontal scroll bars
		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
        
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onGeolocationPermissionsShowPrompt(String origin,
                                                           Callback callback) {
				// TODO Auto-generated method stub
				//super.onGeolocationPermissionsShowPrompt(origin, callback);
				callback.invoke(origin, true, false);
			}
            //
            //			@Override
            //			public boolean onJsAlert(android.webkit.WebView view, String url,
            //					String message, JsResult result) {
            //				// TODO Auto-generated method stub
            ////				return super.onJsAlert(view, url, message, result);
            //				return false;
            //			}
		});
		
		
        
		// make sure the web view fills the viewable area
		webView.setLayoutParams(new LinearLayout.LayoutParams(
                                                              android.view.ViewGroup.LayoutParams.FILL_PARENT,
                                                              android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        
		return webView;
	}
    
	private Dialog createLoginFormDialog() {
        
		Context mContext = this;
		final Dialog dialog = new Dialog(mContext);
        
		LayoutInflater inflater = (LayoutInflater) mContext
        .getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.login_form,
                                             (ViewGroup) findViewById(R.id.login_form_container));
        
		dialog.setContentView(layout);
		dialog.setTitle("Authentication Prompt");
        
		String infoText = loginFormCallback.getInfoText();
		TextView infoTextView = (TextView) layout.findViewById(R.id.info);
		infoTextView.setText(infoText);
        
		Button proceedButton = (Button) layout.findViewById(R.id.proceed);
		proceedButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				EditText passwordText = (EditText) layout
                .findViewById(R.id.password);
				EditText usernameText = (EditText) layout
                .findViewById(R.id.username);
				CheckBox rememberBox = (CheckBox) layout
                .findViewById(R.id.remember);
				loginFormCallback.onProceed(usernameText.getEditableText()
                                            .toString(), passwordText.getEditableText().toString(),
                                            rememberBox.isChecked());
				loginFormCallback = null;
				if (!rememberBox.isChecked()) {
					usernameText.setText("");
					passwordText.setText("");
				}
				dialog.dismiss();
			}
		});
        
		Button cancelButton = (Button) layout.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				EditText passwordText = (EditText) layout
                .findViewById(R.id.password);
				EditText usernameText = (EditText) layout
                .findViewById(R.id.username);
				CheckBox rememberBox = (CheckBox) layout
                .findViewById(R.id.remember);
				loginFormCallback.onCancel();
				loginFormCallback = null;
				if (!rememberBox.isChecked()) {
					usernameText.setText("");
					passwordText.setText("");
				}
				dialog.dismiss();
			}
		});
        
		EditText usernameText = (EditText) layout.findViewById(R.id.username);
		usernameText.requestFocus();
        
		return dialog;
	}
    
	public void setContentView(OmWebView webView) {
		runOnUiThread(new Runnable() {
			OmWebView webView;
            
			public void run() {
				setContentView((View) webView);
			}
            
			public Runnable construct(OmWebView webView) {
				this.webView = webView;
				return this;
			}
		}.construct(webView));
	}
    
	public void setWebView(OmWebView webView) {
		this.webView = (WebView) webView;
	}
    
	public void setReadyForUpdateCheck(boolean ready) {
		this.readyForUpdateCheck = ready;
	}
    
	public boolean getReadyForUpdateCheck() {
		return readyForUpdateCheck;
	}
}
