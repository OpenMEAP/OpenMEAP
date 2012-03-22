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

package com.openmeap.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

abstract public class ProcessingUtils {
	
	private ProcessingUtils() {}
	
	@SuppressWarnings("rawtypes")
	static public ProcessingEvent[] toArray(Collection<ProcessingEvent> events) {
		ProcessingEvent[] ev = new ProcessingEvent[events.size()];
		int i=0;
		for( ProcessingEvent event : events )
			ev[i++] = event;
		return ev;
	}
	
	@SuppressWarnings("rawtypes")
	static public Boolean containsTarget(Collection<ProcessingEvent> events, String processingTarget) {
		for( ProcessingEvent event : events ) {
			for( String target : event.getTargets() )
				if( target.compareTo(processingTarget)==0 )
					return true;
		}
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	static public List<ProcessingEvent> newList(ProcessingEvent... events) {
		List<ProcessingEvent> toret = new ArrayList<ProcessingEvent>();
		for( ProcessingEvent event : events ) {
			toret.add(event);
		}
		return toret;
	}
}
