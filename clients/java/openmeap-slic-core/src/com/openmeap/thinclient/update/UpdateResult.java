package com.openmeap.thinclient.update;

public enum UpdateResult {
	SUCCESS,
	PENDING,
	OUT_OF_SPACE,
	IO_EXCEPTION,
	HASH_MISMATCH,
	INTERRUPTED,
	PLATFORM,
	RESPONSE_STATUS_CODE,
	IMPORT_UNZIP
}
