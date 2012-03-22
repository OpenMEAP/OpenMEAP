package com.openmeap.web.event;

import com.openmeap.web.GenericProcessingEvent;
import com.openmeap.web.html.LinkTag;

public class AddLinkTagEvent extends GenericProcessingEvent<LinkTag> {
	public final static String ADD_LINK_TAG_EVENT = "com.openmeap.web.event.AddLinkTagEvent";
	public AddLinkTagEvent(LinkTag payload) {
		super(AddLinkTagEvent.ADD_LINK_TAG_EVENT,payload);
	}
}
