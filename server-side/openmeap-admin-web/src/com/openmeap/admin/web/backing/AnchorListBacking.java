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

package com.openmeap.admin.web.backing;

import java.util.*;

import com.openmeap.event.ProcessingEvent;
import com.openmeap.web.html.*;
import com.openmeap.web.*;

public class AnchorListBacking extends AbstractTemplatedSectionBacking {

	@SuppressWarnings(value={"rawtypes","unchecked"})
	public Collection<ProcessingEvent> processEvents(ProcessingContext context,
			Collection<ProcessingEvent> events, Map<Object, Object> templateVariables,
			Map<Object, Object> parameterMap) {
		if( templateVariables.get("links")==null ) {
			templateVariables.put("links", new ArrayList<Anchor>());
		}
		for( ProcessingEvent event : events ) {
			if( event.getPayload() instanceof Anchor ) {
				((List<Anchor>)templateVariables.get("links")).add((Anchor)event.getPayload());
			}
		}
		return null;
	}

}
