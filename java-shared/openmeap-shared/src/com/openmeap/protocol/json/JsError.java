package com.openmeap.protocol.json;

import com.openmeap.protocol.dto.Error;

import org.json.JSONException;
import org.json.JSONObject;

public class JsError {
	private String type;
	private String message;
	
	public JsError(Error error) {
		this.type = error.getCode().value();
		this.message = error.getMessage();
	}
	public JsError(String type, String message) {
		this.type = type;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return toJSON();
	}
	
	public String toJSON() {
		return toJSONObject().toString();
	}
	
	public JSONObject toJSONObject() {
		JSONObject err = new JSONObject();
		try {
			err.put("type", type);
			err.put("message", message);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return err;
	}
}

