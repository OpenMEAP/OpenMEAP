/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
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
	static final public UpdateResult UNDEFINED = new UpdateResult("UNDEFINED");
	static final private UpdateResult[] constants = new UpdateResult[] {
		SUCCESS, PENDING, OUT_OF_SPACE, IO_EXCEPTION, HASH_MISMATCH, INTERRUPTED, PLATFORM, RESPONSE_STATUS_CODE, IMPORT_UNZIP, UNDEFINED
	};
	public Enum[] getStaticConstants() {
		return (Enum[])this.constants;
	}
	public String toString() {
		return value();
	}
    private final String value;
    private UpdateResult(String v) {
        value = v;
    }
    public String value() {
        return value;
    }
    static public UpdateResult[] values() {
    	return constants;
    }
    static public UpdateResult fromValue(String v) {
    	return (UpdateResult)EnumUtils.fromValue(PENDING, v);
    }
}
