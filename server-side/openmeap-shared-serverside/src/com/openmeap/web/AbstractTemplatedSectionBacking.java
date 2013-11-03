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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;

import com.openmeap.Authorizer;
import com.openmeap.event.ProcessingEvent;

abstract public class AbstractTemplatedSectionBacking implements TemplatedSectionBacking {

	protected List<String> processingTargetIds = new ArrayList<String>();
	
	public List<String> getProcessingTargetIds() {
		return processingTargetIds;
	}
	public void setProcessingTargetIds(List<String> targetIds) {
		processingTargetIds = targetIds;
	}

	public Collection<ProcessingEvent> processEvents(ProcessingContext context,
			Collection<ProcessingEvent> events, Map<Object, Object> templateVariables,
			Map<Object, Object> parameterMap) {
		return null;
	}
	
	public Collection<ProcessingEvent> process(ProcessingContext context, 
			Map<Object,Object> templateVariables, 
			Map<Object, Object> parameterMap) {
		return null;
	}
}
