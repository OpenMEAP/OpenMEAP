package com.openmeap.thinclient;

import java.util.*;

import com.openmeap.thinclient.Preferences;

public class PreferencesTestImpl implements Preferences {
	private Map map = null;
	public PreferencesTestImpl() {
		this.map = new HashMap();
	}
	public String get(String key) {
		return (String)map.get(key);
	}
	public Boolean put(String key, String value) {
		return Boolean.valueOf(map.put(key,value)!=null);
	}
	public Boolean remove(String key) {
		return Boolean.valueOf(map.remove(key)!=null);
	}
	public Boolean clear() {
		map.clear();
		return Boolean.TRUE;
	}
}
