package com.openmeap.thinclient;

import java.util.Properties;
import java.util.Date;

/**
 * The non-OS specific configuration universal to all Java based slic clients
 * @author schang
 */
public class SLICConfig {
	
	public static String PROPERTIES_FILE = "slic-config.properties";
	public static String PREFERENCES_FILE = "slic-config.preferences";
	public static String SLIC_VERSION = "0.0.1a";
	public static String DEVICE_TYPE = "Android";
	
	protected Properties properties = null;
	protected Preferences preferences = null;
	
	/**
	 * @param preferences
	 * @param propertiesStream
	 */
	public SLICConfig(Preferences preferences, Properties properties) {
		this.preferences = preferences;
		this.properties = properties;
	}
	
	public String getDeviceUuid() {
		String uuid = getProperty("com.openmeap.slic.deviceUuid");
		if( uuid == null ) {
			// TODO: this should be generated from device serials, application name, version, slic version, etc...so as to avoid collisions
			uuid = java.util.UUID.randomUUID().toString();
			setProperty("com.openmeap.slic.deviceUuid", uuid);
		}
		return uuid;
	}
	
	public void setLastAuthToken(String authToken) {
		setProperty("com.openmeap.slic.lastAuthToken",authToken);
	}
	public String getLastAuthToken() {
		return getProperty("com.openmeap.slic.lastAuthToken");
	}
	public String getAppMgmtServiceUrl() {
		return getProperty("com.openmeap.slic.appMgmtServiceUrl");
	}
	public String getApplicationName() {
		return getProperty("com.openmeap.slic.appName");
	}
	public String getApplicationTitle() {
		return getProperty("com.openmeap.slic.appTitle");
	}
	public String getApplicationVersion() {
		return getProperty("com.openmeap.slic.appVersion");
	}
	public void setApplicationVersion(String versionIdentifier) {
		setProperty("com.openmeap.slic.appVersion",versionIdentifier);
	}
	public String getArchiveHash() {
		return getProperty("com.openmeap.slic.archiveHash");
	}
	public void setArchiveHash(String hash) {
		setProperty("com.openmeap.slic.archiveHash",hash);
	}
	public Boolean getNotFirstRun() {
		String notFirstRun = getProperty("com.openmeap.slic.notFirstRun");
		if( notFirstRun!=null ) {
			return Boolean.valueOf(notFirstRun);
		}
		return null;
	}
	public void setNotFirstRun(Boolean notFirstRun) {
		setProperty("com.openmeap.slic.notFirstRun",notFirstRun.toString());
	}
	public void setNotFirstRun(String hash) {
		setProperty("com.openmeap.slic.archiveHash",hash);
	}
	public String getDeviceType() {
		return getProperty("com.openmeap.slic.deviceType");
	}
	public Boolean shouldPerformUpdateCheck() {
		return Boolean.valueOf(isTimeForUpdateCheck().booleanValue() && getProperty("com.openmeap.slic.pullUpdates").equals("true"));
	}
	public Boolean isDevelopmentMode() {
		String devMode = getProperty("com.openmeap.slic.developmentMode");
		if( devMode!=null ) {
			return Boolean.valueOf(devMode);
		}
		return Boolean.FALSE;
	}
	public Boolean isTimeForUpdateCheck() {
		// proceed with an update, if one is requested
    	Long lastUpdate = getLastUpdateAttempt();
    	boolean shouldUpdate = true;
    	if( lastUpdate!=null ) {
    		Date now = new Date();
    		shouldUpdate = getUpdateFrequency()!=null? ((now.getTime()-lastUpdate.longValue())/1000 > getUpdateFrequency().intValue()) : false;
    	}
		return Boolean.valueOf(shouldUpdate);
	}
	public Boolean isVersionOriginal(String version) {
		if( version.equals( properties.getProperty("com.openmeap.slic.appVersion") ) )
			return Boolean.TRUE;
		return Boolean.FALSE;
	}
	
	public Long getLastUpdateAttempt() {
		String lua = getProperty("com.openmeap.slic.lastUpdateAttempt");
		return lua!=null?Long.valueOf(lua):null;
	}
	public void setLastUpdateAttempt(Long time) {
		setProperty("com.openmeap.slic.lastUpdateAttempt",String.valueOf(time));
	}
	
	public String getLastUpdateResult() {
		return getProperty("com.openmeap.slic.lastUpdateResult");
	}
	public void setLastUpdateResult(String result) {
		setProperty("com.openmeap.slic.lastUpdateResult",result);
	}
	
	public Integer getUpdatePendingTimeout() {
		String updatePTO = getProperty("com.openmeap.slic.updatePendingTimeout");
		return updatePTO!=null?Integer.valueOf(updatePTO):null;
	}
	
	/**
	 * @return True if the application was just updated.  Flipped to false on the first reload.
	 */
	public Boolean getApplicationUpdated() {
		String updated = getProperty("com.openmeap.slic.appUpdated");
		return updated!=null?Boolean.valueOf(updated):null;
	}
	public void setApplicationUpdated(Boolean updated) {
		setProperty("com.openmeap.slic.appUpdated",updated.toString());
	}
	
	public Integer getUpdateFrequency() {
		String updateFreq = getProperty("com.openmeap.slic.updateFrequency");
		return updateFreq!=null?Integer.valueOf(updateFreq):null;
	}
	
	
	public String getStorageLocation() {
		return getProperty("com.openmeap.slic.storageLocation");
	}
	public void clearStorageLocation() {
		preferences.remove("com.openmeap.slic.storageLocation");
	}
	public void setStorageLocation(String locationPrefix) {
		setProperty("com.openmeap.slic.storageLocation",locationPrefix);
	}
	
	/*
	 * PRIVATE METHODS
	 */
	
	/**
	 * Pulls a property from preferences.  Failing to
	 * find it there, it will resort to the properties file.
	 * 
	 * @param name
	 * @return The value of the property, else null
	 */
	protected String getProperty(String name) {
		String value = preferences.get(name);
		if( value==null )
			value = properties.getProperty(name);
		return value;
	}
	
	protected void setProperty(String name, String value) {
		preferences.put(name, value);
	}
}
