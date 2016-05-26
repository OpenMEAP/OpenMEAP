/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Rob Thacher                                 #
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

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import com.openmeap.event.ProcessingEvent;

import freemarker.template.TemplateException;

public class GenericDocumentProcessor implements DocumentProcessor {

	private Map<Object,Object> templateVariables = null;
	private TemplatedSection templateTree = null;
	private Boolean processesFormData = Boolean.TRUE;
	
	private Map<String,Set<TemplatedSection>> sectionsWithBackings;
	private Map<String,List<ProcessingEvent>> processingEvents;

	public void handleProcessAndRender(Map<Object,Object> parameterMap, Writer writer) {
		
		sectionsWithBackings = new HashMap<String,Set<TemplatedSection>>();
		processingEvents = new HashMap<String,List<ProcessingEvent>>();
		int lastHashCode = 0;
		
		// pass one may generate events for other backings to process
		recurseProcessSectionBackings(templateTree,parameterMap);
		
		// iteratively process events till none are thrown...
		while( processingEvents.size()>0 && processingEvents.hashCode()!=lastHashCode ) {
			
			Map<String,List<ProcessingEvent>> originalEvents = processingEvents;
			lastHashCode = processingEvents.hashCode();
			processingEvents = new HashMap<String,List<ProcessingEvent>>();
			
			for( Map.Entry<String,List<ProcessingEvent>> entry : originalEvents.entrySet() ) {
				if( sectionsWithBackings.get(entry.getKey())!=null ) {
					Set<TemplatedSection> sections = sectionsWithBackings.get(entry.getKey());
					for(TemplatedSection section : sections ) {
						
						Map<Object,Object> vars = new HashMap<Object,Object>();
						if( this.getTemplateVariables()!=null ) {
							vars.putAll(this.getTemplateVariables());
						}
						if( section.getTemplateVariables()!=null ) {
							vars.putAll(section.getTemplateVariables());
						}
						section.setTemplateVariables(vars);
						
						ProcessingContext context = new ProcessingContextImpl();
						Collection<ProcessingEvent> newEvents = section.getSectionBacking().processEvents(context,entry.getValue(),vars,parameterMap);
						
						// put the events in their appropriate target location
						if( newEvents!=null )
							for( ProcessingEvent event : newEvents ) {
								for( String targetName : event.getTargets() ) {
									if( processingEvents.get(targetName)==null )
										processingEvents.put(targetName, new ArrayList<ProcessingEvent>());
									processingEvents.get(targetName).add(event);
								}
							}
					}
				}
			}
		}
		
		try {
			templateTree.render(writer);
		} catch( TemplateException te ) {
			throw new RuntimeException(te);
		} catch( IOException ioe ) {
			throw new RuntimeException(ioe);
		}
	}
	
	/**
	 * Initial processing pass.  
	 * Collections events and sections with backings as it goes.  
	 * Events are processed in the next pass.
	 * 
	 * @param section
	 * @param parameterMap
	 */
	private void recurseProcessSectionBackings(TemplatedSection section, Map<Object,Object> parameterMap) {
		
		Map<Object,Object> vars = new HashMap<Object,Object>();
		if( this.getTemplateVariables()!=null ) {
			vars.putAll(this.getTemplateVariables());
		}
		if( section.getTemplateVariables()!=null ) {
			vars.putAll(section.getTemplateVariables());
		}
		section.setTemplateVariables(vars);
		
		if( section.getSectionBacking()!=null ) {
			
			ProcessingContext context = new ProcessingContextImpl();
			TemplatedSectionBacking backing = section.getSectionBacking();
			
			// as we process sections, we accumulate the sections that have backings
			// so that they may target each other in the events processing pass
			// the map is Map<Section,List<TemplatedSection>>
			List<String> targets = backing.getProcessingTargetIds();
			if( targets!=null ) 
				for( String target : targets ) {
					
					if( sectionsWithBackings.get(target)==null )
						sectionsWithBackings.put(target,new HashSet<TemplatedSection>());
					sectionsWithBackings.get(target).add(section);
				}
			
			Collection<ProcessingEvent> events = backing.process(context, vars, parameterMap);
			
			// put the events in their appropriate target location
			if( events!=null && events.size()>0 )
				for( ProcessingEvent event : events ) {
					for( String targetName : event.getTargets() ) {
						if( processingEvents.get(targetName)==null )
							processingEvents.put(targetName, new ArrayList<ProcessingEvent>());
						processingEvents.get(targetName).add(event);
					}
				}
		}
		
		// recurse up the template tree toward the leaf levels
		if( section.getChildren()!=null )
			for( Object thisSection : section.getChildren().values().toArray() )
				recurseProcessSectionBackings((TemplatedSection)thisSection, parameterMap);
	}

	public void setTemplateVariables(Map<Object, Object> variables) {
		templateVariables = variables;
	}
	public Map<Object, Object> getTemplateVariables() {
		return templateVariables;
	}

	public void setTemplateTree(TemplatedSection templates) {
		templateTree = templates;
	}
	public TemplatedSection getTemplateTree() {
		return templateTree;
	}
	
	/**
	 * @param processesFormData a hint to the servlet on how to prepare for rendering.
	 */
	public void setProcessesFormData(Boolean processesFormData) {
		this.processesFormData = processesFormData;
	}
	public Boolean getProcessesFormData() {
		return processesFormData;
	}
}
