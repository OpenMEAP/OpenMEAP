package com.openmeap.thinclient;

public interface Preferences {
	public String get(String key);
	public Boolean put(String key, String value);
	public Boolean remove(String key);
	public Boolean clear();
}
