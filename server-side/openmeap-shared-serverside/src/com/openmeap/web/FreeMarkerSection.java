/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2013 OpenMEAP, Inc.                                   #
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

import java.util.*;

import freemarker.template.*;
import java.io.*;


/**
 * Encapsulates a templated section of HTML or whatever.
 *  
 * @author schang
 */
public class FreeMarkerSection implements TemplatedSection {
	private Configuration config = null;
	private String templatePath = null;
	private Map<Object,Object> templateVariables = null;
	private Map<String,TemplatedSection> childTemplates = null;
	private TemplatedSectionBacking sectionBacking = null;
	
	public FreeMarkerSection() {
		// intentionally blank
	}
	
	public FreeMarkerSection(Configuration config) {
		setConfiguration(config);
	}
	
	public FreeMarkerSection(Configuration config, String templatePath) {
		setConfiguration(config);
		setTemplatePath(templatePath);
	}
	
	public void setConfiguration(Configuration config) {
		this.config = config;
	}
	public Configuration getConfiguration() {
		return this.config;
	}
	
	public void setTemplatePath(String path) {
		templatePath = path;
	}
	public String getTemplatePath() {
		return templatePath;
	}
	
	public void setTemplateVariables(Map<Object,Object> variables) {
		templateVariables = variables;
	}
	public Map<Object,Object> getTemplateVariables() {
		return templateVariables;
	}
	
	public void render(Writer out) throws IOException, TemplateException {
		
		// TODO: not happy with this, really
		if( childTemplates!=null ) {
			
			if( templateVariables==null )
				templateVariables=new HashMap<Object,Object>();
			
			if( templateVariables.get("children")==null )
				templateVariables.put("children", new HashMap<String,String>());
			
			Map<String,String> ph = (Map<String,String>)templateVariables.get("children");
			for( Map.Entry<String,TemplatedSection> entry : childTemplates.entrySet() ) {
				ph.put(entry.getKey(), entry.getValue().render());
			}
		}
		
		Template temp = config.getTemplate(templatePath);
		temp.process(templateVariables, out);
		out.flush();
	}
	
	public String render() throws IOException, TemplateException {
		StringWriter writer = new StringWriter();
		render(writer);
		return writer.toString();
	}

	public void setChildren(Map<String, TemplatedSection> children) {
		childTemplates = children;
	}
	public Map<String, TemplatedSection> getChildren() {
		return childTemplates;
	}

	public void setSectionBacking(TemplatedSectionBacking backing) {
		this.sectionBacking = backing;
	}

	public TemplatedSectionBacking getSectionBacking() {
		return sectionBacking;
	}
}
