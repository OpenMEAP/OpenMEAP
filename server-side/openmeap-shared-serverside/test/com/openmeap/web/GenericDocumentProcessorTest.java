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

import org.junit.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import com.openmeap.event.ProcessingEvent;
import com.openmeap.web.*;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

public class GenericDocumentProcessorTest {
	
	@Test public void testEventPassing() {
		GenericDocumentProcessor gtd = new GenericDocumentProcessor();
		Configuration cfg = FreeMarkerTestUtils.createConfiguration(this.getClass());
		
		FreeMarkerSection fms = new FreeMarkerSection(cfg,"templates/1.ftl");
		fms.setChildren(new HashMap<String,TemplatedSection>());
		
		FreeMarkerSection f11 = new FreeMarkerSection(cfg,"templates/1.1.ftl");
		f11.setSectionBacking(new Backing(Arrays.asList(new String[]{"1.1"})));
		
		FreeMarkerSection f12 = new FreeMarkerSection(cfg,"templates/1.2.ftl");
		f12.setSectionBacking(new Backing(Arrays.asList(new String[]{"1.2"})));
		
		fms.getChildren().put("m1", f11);
		fms.getChildren().put("m2", f12);
		
		gtd.setTemplateTree(fms);
		StringWriter writer = new StringWriter();
		gtd.handleProcessAndRender(new HashMap<Object,Object>(), writer);
		Assert.assertTrue("HERPPAYLOAD_FOR_1.1DERPPAYLOAD_FOR_1.2HAPPINESS".compareTo(writer.getBuffer().toString())==0);
	}
	
	private class Backing implements TemplatedSectionBacking {
		private List<String> processTarget = null;
		public Backing(List<String> processTarget) {
			this.processTarget=processTarget;
		}
		public Collection<ProcessingEvent> process(ProcessingContext context,
				Map<Object, Object> templateVariables,
				Map<Object, Object> parameterMap) {
			String target = processTarget.get(0).compareTo("1.2")==0?"1.1":"1.2";
			return ProcessingUtils.newList(new GenericProcessingEvent<String>(target,"PAYLOAD_FOR_"+target));
		}
		public List<String> getProcessingTargetIds() {
			return processTarget;
		}
		public Collection<ProcessingEvent> processEvents(ProcessingContext context,
				Collection<ProcessingEvent> events,
				Map<Object, Object> templateVariables,
				Map<Object, Object> parameterMap) {
			if(events!=null && events.size()>0 ) {
				int i=0;
				for( ProcessingEvent event : events ) {
					if( i==0 && ((String)event.getPayload()).startsWith("PAYLOAD") )
						templateVariables.put("targetedText", (String)event.getPayload());
					else templateVariables.put("tail", (String)event.getPayload());
					i++;
				}
			}
			if( processTarget.get(0).compareTo("1.1")==0 )
				return ProcessingUtils.newList(new GenericProcessingEvent<String>("1.2","HAPPINESS"));
			return null;
		}
	}
}
