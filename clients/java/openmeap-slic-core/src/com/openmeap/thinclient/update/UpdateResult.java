package com.openmeap.thinclient.update;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.openmeap.json.Enum;

public class UpdateResult implements Enum {
	static final public UpdateResult SUCCESS = new UpdateResult("SUCCESS");
	static final public UpdateResult PENDING = new UpdateResult("PENDING");
	static final public UpdateResult OUT_OF_SPACE = new UpdateResult("OUT_OF_SPACE");
	static final public UpdateResult IO_EXCEPTION = new UpdateResult("IO_EXCEPTION");
	static final public UpdateResult HASH_MISMATCH = new UpdateResult("HASH_MISMATCH");
	static final public UpdateResult INTERRUPTED = new UpdateResult("INTERRUPTED");
	static final public UpdateResult PLATFORM = new UpdateResult("PLATFORM");
	static final public UpdateResult RESPONSE_STATUS_CODE = new UpdateResult("RESPONSE_STATUS_CODE");
	static final public UpdateResult IMPORT_UNZIP = new UpdateResult("IMPORT_UNZIP");
	private String value;
	private UpdateResult(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
	static public UpdateResult[] values() {
    	List list = new ArrayList();
    	Field[] fields = UpdateResult.class.getDeclaredFields();
    	for( int fieldIdx=0; fieldIdx<fields.length; fieldIdx++ ) {
    		try {
				list.add(fields[fieldIdx].get(null));
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
    	}
    	return (UpdateResult[])list.toArray();
    }

    public static UpdateResult fromValue(String v) {
    	Field[] fields = UpdateResult.class.getDeclaredFields();
    	for( int fieldIdx=0; fieldIdx<fields.length; fieldIdx++ ) {
    		Field field = fields[fieldIdx];
    		try {
	    		if( ((UpdateResult)field.get(null)).value().equals(v) ) {
	    			return (UpdateResult)field.get(null);
					
	    		}
    		} catch(Exception e) {
    			throw new IllegalArgumentException(v);
    		}
    	}
    	throw new IllegalArgumentException(v);
    }
}
