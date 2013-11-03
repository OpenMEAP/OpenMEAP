/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Robert Thacher                              #
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

package com.openmeap.web;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.openmeap.event.ProcessingEvent;
import com.openmeap.web.event.AddScriptTagEvent;
import com.openmeap.web.event.AddLinkTagEvent;
import com.openmeap.web.html.ScriptTag;
import com.openmeap.web.html.LinkTag;

public class LayoutBacking extends AbstractTemplatedSectionBacking {
	public LayoutBacking() {
		processingTargetIds.add(AddScriptTagEvent.ADD_SCRIPT_TAG_EVENT);
		processingTargetIds.add(AddLinkTagEvent.ADD_LINK_TAG_EVENT);
	}
	
	private List<ScriptTag> scriptTags = null;
	private List<LinkTag> linkTags = null;
	
	@Override 
	public Collection<ProcessingEvent> process( ProcessingContext context, 
			Map<Object,Object> templateVariables, 
			Map<Object,Object> parameterMap ) {
		for(ScriptTag tag : scriptTags) {
			addScriptTag(templateVariables,tag);
		}
		for(LinkTag tag : linkTags) {
			addLinkTag(templateVariables,tag);
		}
		return null;
	}
	
	@Override
	public Collection<ProcessingEvent> processEvents(ProcessingContext context,
			Collection<ProcessingEvent> events, Map<Object, Object> templateVariables,
			Map<Object, Object> parameterMap) {
		
		for( ProcessingEvent event : events ) {
			if( event instanceof AddScriptTagEvent ) {
				addScriptTag(templateVariables,(ScriptTag)event.getPayload());
			} else if( event instanceof AddLinkTagEvent ) {
				addLinkTag(templateVariables,(LinkTag)event.getPayload());
			}
		}
		
		return null;
	}
	
	private void addScriptTag(Map<Object, Object> templateVariables, ScriptTag tag) {
		@SuppressWarnings(value={"unchecked"})
		List<ScriptTag> tags = (List<ScriptTag>)templateVariables.get("scriptTags");
		if( tags==null ) {
			tags = new ArrayList<ScriptTag>();
			templateVariables.put("scriptTags",tags);
		}
		if( !tags.contains(tag) )
			tags.add(tag);
	}
	
	private void addLinkTag(Map<Object, Object> templateVariables, LinkTag tag) {
		@SuppressWarnings(value={"unchecked"})
		List<LinkTag> tags = (List<LinkTag>)templateVariables.get("linkTags");
		if( tags==null ) {
			tags = new ArrayList<LinkTag>();
			templateVariables.put("linkTags",tags);
		}
		if( !tags.contains(tag) )
			tags.add(tag);
	}
	
	public void setScriptTags(List<ScriptTag> tags) {
		scriptTags = tags;
	}
	public List<ScriptTag> getScriptTags() {
		return scriptTags;
	}
	
	public void setLinkTags(List<LinkTag> tags) {
		linkTags = tags;
	}
	public List<LinkTag> getLinkTags() {
		return linkTags;
	}
}
