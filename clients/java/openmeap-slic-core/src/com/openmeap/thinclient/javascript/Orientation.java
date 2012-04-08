package com.openmeap.thinclient.javascript;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.openmeap.json.Enum;

public class Orientation implements Enum { 
	static final public Orientation PORTRAIT = new Orientation("PORTRAIT");
	static final public Orientation LANDSCAPE = new Orientation("LANDSCAPE"); 
	static final public Orientation SQUARE = new Orientation("SQUARE"); 
	static final public Orientation UNDEFINED = new Orientation("UNDEFINED"); 
	private String value;
	private Orientation(String value) {
		this.value=value;
	}
	public String value() {
		return value;
	}
	public String toString() {
		return value();
	}
	static public Orientation[] values() {
    	List list = new ArrayList();
    	Field[] fields = Orientation.class.getDeclaredFields();
    	for( int fieldIdx=0; fieldIdx<fields.length; fieldIdx++ ) {
    		try {
				list.add(fields[fieldIdx].get(null));
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
    	}
    	return (Orientation[])list.toArray();
    }
    public static Orientation fromValue(String v) {
    	Field[] fields = Orientation.class.getDeclaredFields();
    	for( int fieldIdx=0; fieldIdx<fields.length; fieldIdx++ ) {
    		Field field = fields[fieldIdx];
    		try {
	    		if( ((Orientation)field.get(null)).value().equals(v) ) {
	    			return (Orientation)field.get(null);
					
	    		}
    		} catch(Exception e) {
    			throw new IllegalArgumentException(v);
    		}
    	}
    	throw new IllegalArgumentException(v);
    }
};
