/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2012 OpenMEAP, Inc.                                   #
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

package com.openmeap.admin.web.backing;

import java.util.*;

import com.openmeap.event.ProcessingEvent;
import com.openmeap.event.ProcessingTargets;
import com.openmeap.web.AbstractTemplatedSectionBacking;
import com.openmeap.web.ProcessingContext;

public class MessagesBacking extends AbstractTemplatedSectionBacking {

	private static String PROCESS_TARGET = ProcessingTargets.MESSAGES;

	public List<String> getProcessingTargetIds() {
		return Arrays.asList(new String[]{PROCESS_TARGET});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<ProcessingEvent> processEvents(ProcessingContext context, Collection<ProcessingEvent> events, Map<Object, Object> templateVariables, Map<Object, Object> parameterMap ) {
		if( templateVariables.get("messages") == null )
			templateVariables.put("messages", new ArrayList<String>());
		for(ProcessingEvent event : events) {
			if( event.getPayload() instanceof String )
				((List<String>)templateVariables.get("messages")).add((String)event.getPayload());
		}
		return null;
	}
}
