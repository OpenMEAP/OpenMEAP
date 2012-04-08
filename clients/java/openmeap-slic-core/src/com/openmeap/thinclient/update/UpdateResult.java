package com.openmeap.thinclient.update;

import com.openmeap.json.Enum;
import com.openmeap.json.EnumUtils;

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
	
    private final String value;
    private UpdateResult(String v) {
        value = v;
    }
    public String value() {
        return value;
    }
    static public UpdateResult[] values() {
    	return (UpdateResult[])EnumUtils.values(UpdateResult.class);
    }
    static public UpdateResult fromValue(String v) {
    	return (UpdateResult)EnumUtils.fromValue(UpdateResult.class, v);
    }
}
