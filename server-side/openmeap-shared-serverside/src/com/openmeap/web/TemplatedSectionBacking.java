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

import com.openmeap.event.ProcessingEvent;

/**
 * Handles backend processing for one little section of a page.
 * It's a controllette, essentially.
 * 
 * Classes implementing this interface are intended to be stateless.
 * 
 * @author schang
 */
public interface TemplatedSectionBacking {
	
	/**
	 * The initial processing of the templated section.  Should handle the parameterMap
	 * and populate the template variables
	 * 
	 * @param context the processing context
	 * @param templateVariables outputs for the template
	 * @param parameterMap inputs from the request or wherever
	 * @return any events triggered by the processing of the template
	 */
	public Collection<ProcessingEvent> process( ProcessingContext context, Map<Object,Object> templateVariables, Map<Object,Object> parameterMap );
	
	/**
	 * Each backing should return an id unique to the class
	 * so that events may be passed intelligently
	 */
	public List<String> getProcessingTargetIds();
	
	/**
	 * Offers backings the option to respond to events
	 * after the initial processing pass.
	 * 
	 * @param context the processing context
	 * @param events events triggered by other backings
	 * @param templateVariables outputs to the template
	 * @param parameterMap inputs from the request or wherever
	 * @return any events triggered by the processing of events
	 */
	public Collection<ProcessingEvent> processEvents( ProcessingContext context, Collection<ProcessingEvent> events, Map<Object,Object> templateVariables, Map<Object,Object> parameterMap );	
}
