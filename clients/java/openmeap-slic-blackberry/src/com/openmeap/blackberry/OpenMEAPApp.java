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

package com.openmeap.blackberry;

import java.io.IOException;
import java.io.InputStream;

import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Status;

import org.json.me.JSONException;

import com.openmeap.blackberry.digest.Md5DigestInputStream;
import com.openmeap.blackberry.digest.Sha1DigestInputStream;
import com.openmeap.constants.FormConstants;
import com.openmeap.digest.DigestInputStreamFactory;
import com.openmeap.http.HttpRequestExecuterFactory;
import com.openmeap.thinclient.LocalStorage;
import com.openmeap.thinclient.OmMainActivity;
import com.openmeap.thinclient.OmWebView;
import com.openmeap.thinclient.Preferences;
import com.openmeap.thinclient.SLICConfig;
import com.openmeap.thinclient.javascript.Orientation;
import com.openmeap.thinclient.update.UpdateHandler;
import com.openmeap.util.GenericRuntimeException;
import com.openmeap.util.Utils;

import fr.free.ichir.mahieddine.Properties;

/**
 * This class extends the UiApplication class, providing a
 * graphical user interface.
 */
public class OpenMEAPApp extends UiApplication implements OmMainActivity
{
	public static String STORAGE_ROOT = "file:///store/home/user";
	
	private SLICConfig config;
	private LocalStorage localStorage;
	private UpdateHandler updateHandler;
	private OpenMEAPScreen webView;
	
	public static void main(String[] args)
    {
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
		OpenMEAPApp theApp = new OpenMEAPApp();
        theApp.enterEventDispatcher();
    }
	
    /**
     * Creates a new MyApp object
     * @throws JSONException 
     * @throws IOException 
     */
    public OpenMEAPApp() {
    	
    	DigestInputStreamFactory.setDigestInputStreamForName("MD5", Md5DigestInputStream.class);
    	DigestInputStreamFactory.setDigestInputStreamForName("SHA1", Sha1DigestInputStream.class);
    	HttpRequestExecuterFactory.setDefaultType(HttpRequestExecuterImpl.class);
    	
    	try {
    		InputStream stream = System.class.getResourceAsStream('/'+SLICConfig.PROPERTIES_FILE);
    		Properties properties = new Properties();
    		properties.load(stream);
    		
    		// we want everything for this installation instance
        	// to go under it's own directory in /store/home/user
        	// so that it's easy to wipe later.
    		String storageRoot = (String)properties.getProperties().get("com.openmeap.slic.blackberry.localStorageRoot");
    		if(storageRoot==null) {
    			throw new GenericRuntimeException("com.openmeap.slic.blackberry.localStorageRoot is a required property and should be unique to your application");
    		}
    		STORAGE_ROOT = STORAGE_ROOT+'/'+storageRoot;
    		
	    	config = new BlackberrySLICConfig(
	    			new SharedPreferencesImpl("slic-config"),
	    			properties.getProperties()
	    		);
	    	
	    } catch(JSONException jse) {
	    	throw new GenericRuntimeException(jse);
	    } catch(IOException ioe) {
	    	throw new GenericRuntimeException(ioe);
	    }  
    	
    	localStorage = new LocalStorageImpl(config);
    	
    	CodeModuleManager.addListener(this, new ApplicationDeleteCleanup((BlackberrySLICConfig)config,(LocalStorageImpl)localStorage));
    	
    	updateHandler = new UpdateHandler(this,config,localStorage);
    	
		updateHandler.initialize(webView);
    }

	public SLICConfig getConfig() {
		return config;
	}

	public Preferences getPreferences(String name) {
		try {
			return new SharedPreferencesImpl(name);
		} catch (IOException e) {
			throw new GenericRuntimeException(e);
		} catch (JSONException e) {
			throw new GenericRuntimeException(e);
		}
	}

	/**
     * Sets up the window title, per the properties
     */
    private void setupWindowTitle() {
    	if( config.getApplicationTitle()!=null ) {
        	if( config.getApplicationTitle().equals("off") ) {
        		setTitle(null);
        	} else {
        		setTitle(config.getApplicationTitle());
        	}
		} else setTitle(config.getApplicationName());
    }
	
	public void setTitle(String title) {
		this.setTitle(title);
	}

	public Orientation getOrientation() {
		return Orientation.PORTRAIT;
	}

	public LocalStorage getStorage() {
		return localStorage;
	}

	public UpdateHandler getUpdateHandler() {
		return updateHandler;
	}

	public OmWebView createDefaultWebView() {
		return new OpenMEAPScreen(this,config,localStorage);
	}

	public void setContentView(OmWebView webView) {
		runOnUiThread(new Runnable(){
			OmWebView webView;
			public void run() {
				pushScreen((Screen)webView);
				((Screen)webView).setFocus();
			}
			public Runnable construct(OmWebView webView) {
				this.webView = webView;
				return this;
			}
		}.construct(webView));
	}
	
	public void runOnUiThread(Runnable runnable) {
		invokeLater(runnable);
	}

	public void setWebView(OmWebView webView) {
		this.webView = (OpenMEAPScreen) webView;
	}  
	
	/**
     * Loads in the content of the index document page to load into the WebView
     * 
     * @return The url of the index document
     * @throws IOException
     */
    public String getBaseUrl() {
    	return config.getAssetsBaseUrl();
    }
    
    public void doToast(final String mesg, boolean isLong) {
    	runOnUiThread(new Runnable(){
			public void run() {
				Status.show(mesg);
			}   		
    	});
	}
	
	public void restart() {
		runOnUiThread(new Runnable(){
			public void run() {
				popScreen(webView);
				webView=null;
				updateHandler.initialize(webView);
			}
		});
	}
	
	public String getRootWebPageContent() throws IOException {
		InputStream is = null;
		try {
			is = webView.getContent(getBaseUrl()+"/index.html");
			return Utils.readInputStream(is,FormConstants.CHAR_ENC_DEFAULT);
		} catch(Exception e) {
			throw new IOException(e.getMessage());
		} finally {
			if(is!=null) {
				is.close();
			}
		}
	}
}
