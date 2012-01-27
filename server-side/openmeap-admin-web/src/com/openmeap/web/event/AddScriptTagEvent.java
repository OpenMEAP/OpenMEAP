package com.openmeap.web.event;

import com.openmeap.web.GenericProcessingEvent;
import com.openmeap.web.html.ScriptTag;

public class AddScriptTagEvent extends GenericProcessingEvent<ScriptTag> {
	public final static String ADD_SCRIPT_TAG_EVENT = "com.openmeap.web.event.AddScriptTagEvent";
	public AddScriptTagEvent(ScriptTag payload) {
		super(AddScriptTagEvent.ADD_SCRIPT_TAG_EVENT,payload);
	}
}
