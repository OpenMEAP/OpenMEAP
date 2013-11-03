/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

package com.openmeap.thinclient;

import java.util.Date;
import java.util.Hashtable;

import com.openmeap.util.StringUtils;
import com.openmeap.util.UUID;
import com.openmeap.util.Utils;

/**
 * The non-OS specific configuration universal to all Java based slic clients
 * @author schang
 */
abstract public class SLICConfig {
	
	public static String PROPERTIES_FILE = "slic-config.properties";
	public static String PREFERENCES_FILE = "slic-config.preferences";
	public static String SLIC_VERSION = "0.0.1a";
	
	protected Hashtable properties = null;
	protected Preferences preferences = null;
	
	/**
	 * @param preferences
	 * @param propertiesStream
	 */
	public SLICConfig(Preferences preferences, Hashtable properties) {
		this.preferences = preferences;
		this.properties = properties;
	}
	
	abstract public String getAssetsBaseUrl();
	
	/**
	 * TODO: rename to applicationInstallationUuid
	 * @return
	 */
	public String getDeviceUuid() {
		String uuid = getProperty("com.openmeap.slic.deviceUuid");
		if( StringUtils.isEmpty(uuid) ) {
			// TODO: this should be generated from device serials, application name, version, slic version, etc...so as to avoid collisions
			uuid = UUID.randomUUID();
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
			return new Boolean(Utils.parseBoolean(notFirstRun));
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
		return new Boolean(isTimeForUpdateCheck().booleanValue() && getProperty("com.openmeap.slic.pullUpdates").equals("true"));
	}
	public Boolean isDevelopmentMode() {
		String devMode = getProperty("com.openmeap.slic.developmentMode");
		if( devMode!=null ) {
			return new Boolean(Utils.parseBoolean(devMode));
		}
		return Boolean.FALSE;
	}
	public Boolean isTimeForUpdateCheck() {
		// proceed with an update, if one is requested
    	Long lastUpdate = getLastUpdateAttempt();
    	boolean shouldUpdate = true;
    	if( lastUpdate!=null ) {
    		Date now = new Date();
    		long secondsSince = (now.getTime()-lastUpdate.longValue())/(long)1000;
    		long secondsInterval = getUpdateFrequency()!=null ? getUpdateFrequency().intValue() : 0;
    		shouldUpdate = secondsSince > secondsInterval;
    	}
		return new Boolean(shouldUpdate);
	}
	public Boolean isVersionOriginal(String version) {
		if( version.equals( properties.get("com.openmeap.slic.appVersion") ) )
			return Boolean.TRUE;
		return Boolean.FALSE;
	}
	
	public Long getLastUpdateAttempt() {
		String lua = getProperty("com.openmeap.slic.lastUpdateAttempt");
		return lua!=null?new Long(Long.parseLong(lua)):null;
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
		return updated!=null?new Boolean(Utils.parseBoolean(updated)):null;
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
	
	public Boolean shouldUseAssetsOrSdCard() {
    	return new Boolean( getStorageLocation()==null || isVersionOriginal(getApplicationVersion()).booleanValue() );
    }
	
	public String getPackagedAppRoot() {
		return /*"assets:///"+*/this.getProperty("com.openmeap.slic.packagedAppRoot");
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
		if( value==null ) {
			value = (String)properties.get(name);
		}
		return value;
	}
	
	protected void setProperty(String name, String value) {
		preferences.put(name, value);
	}
	
	
}
