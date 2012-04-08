package com.openmeap.thinclient.javascript;

import com.openmeap.json.Enum;
import com.openmeap.json.EnumUtils;

public class Orientation implements Enum { 
	static final public Orientation PORTRAIT = new Orientation("PORTRAIT");
	static final public Orientation LANDSCAPE = new Orientation("LANDSCAPE"); 
	static final public Orientation SQUARE = new Orientation("SQUARE"); 
	static final public Orientation UNDEFINED = new Orientation("UNDEFINED"); 
    private final String value;
    private Orientation(String v) {
        value = v;
    }
    public String value() {
        return value;
    }
    static public Orientation[] values() {
    	return (Orientation[])EnumUtils.values(Orientation.class);
    }
    static public Orientation fromValue(String v) {
    	return (Orientation)EnumUtils.fromValue(Orientation.class, v);
    }
};
