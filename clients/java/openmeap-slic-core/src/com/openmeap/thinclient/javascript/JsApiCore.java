package com.openmeap.thinclient.javascript;

import com.openmeap.thinclient.Preferences;

/**
 * The interface for the Javascript API.
 * 
 * Intended to be implemented in all Java-based SLIC implementations.
 * 
 * @author schang
 */
public interface JsApiCore {
	public enum Orientation { PORTRAIT, LANDSCAPE, SQUARE, UNDEFINED };

	public String getOrientation();
	
	public void doToast(String mesg);
	
	public void doToast(String mesg, Boolean isLong);
	
	/**
	 * Obtain the name of the device type
	 * @return
	 */
	public String getDeviceType();
	
	/**
	 * Sets the title of the window.
	 * Only effective if the application is configured to have a title.
	 * @param title
	 */
	public void setTitle(String title);
	
	/**
	 * Clear's the cache of the WebView
	 */
	public void clearCache();
	
	/**
	 * Creates or retrieves a preferences object
	 * @param name
	 * @return
	 */
	public Preferences getPreferences(String name);
	
	public Boolean isTimeForUpdateCheck();
	
	/**
	 * Connect to Application management and check for available updates.
	 * 
	 * callBack should be of the form: <code>function(updateHeader) { ... }</code>.
	 * If a new deployment is available, the <code>updateHeader</code> passed in will be of the form:
	 * <code>
	 * 	{
	 * 	};
	 * </code>
	 * 
	 * @param callBack Javascript to execute when done checking.  Should accept a single object parameter 
	 */
	public void checkForUpdates(String callBack);
	
	/**
	 * @param updateHeader The update to perform
	 * @param statusCallBack a javascript function to pass status information back to
	 */
	public void performUpdate(final String header, final String statusCallBack);
}

