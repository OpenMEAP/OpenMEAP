package com.openmeap.thinclient;

import java.util.*;

import com.openmeap.thinclient.Preferences;

public class PreferencesTestImpl implements Preferences {
	private Map<String,String> map = null;
	public PreferencesTestImpl() {
		this.map = new HashMap<String,String>();
	}
	public String get(String key) {
		return map.get(key);
	}
	public Boolean put(String key, String value) {
		return map.put(key,value)!=null;
	}
	public Boolean remove(String key) {
		return map.remove(key)!=null;
	}
	public Boolean clear() {
		map.clear();
		return true;
	}
}
