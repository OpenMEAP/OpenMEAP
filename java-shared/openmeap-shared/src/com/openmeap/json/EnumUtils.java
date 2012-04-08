package com.openmeap.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.openmeap.protocol.dto.HashAlgorithm;

abstract public class EnumUtils {
	
   final static public Object[] values(Class enumClass) {
    	List list = new ArrayList();
    	Field[] fields = HashAlgorithm.class.getDeclaredFields();
    	for( int fieldIdx=0; fieldIdx<fields.length; fieldIdx++ ) {
    		try {
    			Field field = fields[fieldIdx];
    			if(!Modifier.isPublic(field.getModifiers())) {
    				continue;
    			}
    			Object value;
    			try {
    				value = field.get(null);
    			} catch (NullPointerException e) {
    				continue;
    			}
    			if( ! (Modifier.isStatic(field.getModifiers()) && enumClass.isAssignableFrom(value.getClass()) ) ) {
        			continue;
        		}
    			list.add(value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
    	}
    	return (Object[])list.toArray((Object[]) Array.newInstance(enumClass, list.size()));
    }

    final static public Enum fromValue(Class enumClass, String v) {
    	Field[] fields = enumClass.getDeclaredFields();
    	for( int fieldIdx=0; fieldIdx<fields.length; fieldIdx++ ) {
    		Field field = fields[fieldIdx];
    		try {
    			if( ! (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) ) {
    				continue;
    			}
    			Object value;
    			try {
    				value = field.get(null);
    			} catch(NullPointerException e) {
    				continue;
    			}
    			if( ! enumClass.isAssignableFrom(value.getClass()) ) {
    				continue;
    			}
	    		if( ((Enum)value).value().equals(v) ) {
	    			return (Enum)value;
	    		}
    		} catch(Exception e) {
    			throw new IllegalArgumentException(v);
    		}
    	}
    	throw new IllegalArgumentException(v);
    }
}
