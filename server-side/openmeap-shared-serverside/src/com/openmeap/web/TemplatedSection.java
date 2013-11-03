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

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import freemarker.template.TemplateException;

/**
 * The interface for the display side of a section of the page
 * 
 * @author schang
 */
public interface TemplatedSection {
	
	/**
	 * Children that may be in placeholders of the template.
	 * 
	 * If a child is added with <code>getChildren().put("ph1",new FreeMarkerSection(...</code>
	 * then the placeholder will be available within the template as <code>${children.ph1}</code>
	 * 
	 * @param children
	 */
	public void setChildren(Map<String,TemplatedSection> children);
	public Map<String,TemplatedSection> getChildren();
	
	public void setTemplatePath(String path);
	public String getTemplatePath();
	
	public void setTemplateVariables(Map<Object,Object> variables);
	public Map<Object,Object> getTemplateVariables();
	
	public void render(Writer out) throws IOException, TemplateException;
	public String render() throws IOException, TemplateException;
	
	public void setSectionBacking(TemplatedSectionBacking backing);
	public TemplatedSectionBacking getSectionBacking();
}
