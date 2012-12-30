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

package com.openmeap.util;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.exception.ExceptionUtils;

public class ThrowableList extends ArrayList<Throwable> {
	
	private static String SEPARATOR = ", ";
	private Logger logger = LoggerFactory.getLogger(ThrowableList.class);
	
	public ThrowableList() {}
	public ThrowableList(int arg0) {super(arg0);}
	public ThrowableList(Collection arg0) {super(arg0);}
	public ThrowableList(Logger logger) {
		this.logger = logger;
	}
	
	public void log() {
		logger.error(this.toString());
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		Integer i = 0;
		for( Throwable t : this ) {
			// get root cause
			Exception rootCause = (Exception)ExceptionUtils.getRootCause(t);
			String stackTrace = ExceptionUtils.getStackTrace(rootCause);
			result.append("+= Throwable "+i+" ==================\n");
			result.append(rootCause.getMessage());
			result.append("\n");
			result.append(stackTrace);
			result.append("\n");
			i++;
		}
		return result.toString();
	}
	
	public String getMessages() {
		StringBuilder result = new StringBuilder();
		Integer i = 0;
		for( Throwable t : this ) {
			// get root cause
			Exception rootCause = (Exception)ExceptionUtils.getRootCause(t);
			String stackTrace = ExceptionUtils.getStackTrace(rootCause);
			if(i!=0) {
				result.append(SEPARATOR);
			}
			result.append("\""+rootCause.getMessage()+"\"");
			i++;
		}
		return result.toString();
	}
}
