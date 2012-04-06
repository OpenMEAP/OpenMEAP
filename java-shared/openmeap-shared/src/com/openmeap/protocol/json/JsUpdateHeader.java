package com.openmeap.protocol.json;

import org.json.JSONException;
import org.json.JSONObject;

import com.openmeap.json.JSONObjectBuilder;
import com.openmeap.protocol.dto.Hash;
import com.openmeap.protocol.dto.HashAlgorithm;
import com.openmeap.protocol.dto.UpdateHeader;
import com.openmeap.protocol.dto.UpdateType;

public class JsUpdateHeader {
	
	private UpdateHeader updateHeader;
	private Long spaceAvailable;
	
	public JsUpdateHeader(UpdateHeader updateHeader, Long spaceAvailable) {
		this.updateHeader=updateHeader;
		this.spaceAvailable = spaceAvailable;
		if( updateHeader!=null ) {
			this.updateHeader.setSpaceAvailable(spaceAvailable);
		}
	}
	
	public JsUpdateHeader(String json) throws JSONException {
		JSONObject obj = new JSONObject(json);
		JSONObjectBuilder builder = new JSONObjectBuilder();
		UpdateHeader update = (UpdateHeader)builder.fromJSON(obj, new UpdateHeader());
		updateHeader = update;
	}
	
	public UpdateHeader getWrappedObject() {
		return updateHeader;
	}
	
	public String toString() {
		return toJSON().toString();
	}
	
	public String toJSON() {
		if( updateHeader!=null ) {
			try {
				JSONObjectBuilder builder = new JSONObjectBuilder();
				return builder.toJSON(updateHeader).toString();
			} catch( JSONException e ) {
				throw new RuntimeException(e);
			}
		}
		return "null";
	}
}
