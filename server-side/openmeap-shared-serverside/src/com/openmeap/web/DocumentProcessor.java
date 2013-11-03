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

import java.io.Writer;
import java.util.*;

/**
 * Processor handling a document.
 * Single use, intended to be stateful during operation.
 * Life does not span multiple requests.
 * @author schang
 */
public interface DocumentProcessor {
	
	/**
	 * Variables that are available to each template within the tree
	 * @param variables
	 */
	public void setTemplateVariables(Map<Object,Object> variables);
	public Map<Object,Object> getTemplateVariables();
	
	/**
	 * A template tree.  The template file references child templates.
	 * @param templates
	 */
	public void setTemplateTree(TemplatedSection templates);
	public TemplatedSection getTemplateTree();
	
	public void handleProcessAndRender(Map<Object,Object> parameterMap, Writer writer);
	
	/**
	 * @param processesFormData defaults to true, for use by the servlet
	 */
	public void setProcessesFormData(Boolean processesFormData);
	public Boolean getProcessesFormData();
}
