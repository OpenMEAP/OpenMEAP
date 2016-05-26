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

package com.openmeap.util;

import org.apache.commons.transaction.util.LoggerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4JLoggerFacade implements LoggerFacade {

	private Logger logger;
	
	public SLF4JLoggerFacade(Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public LoggerFacade createLogger(String arg0) {
		return new SLF4JLoggerFacade(LoggerFactory.getLogger(arg0));
	}

	@Override
	public boolean isFineEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public boolean isFinerEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public boolean isFinestEnabled() {
		return logger.isTraceEnabled();
	}

	@Override
	public void logFine(String arg0) {
		logger.debug(arg0);
	}

	@Override
	public void logFiner(String arg0) {
		logger.debug(arg0);
	}

	@Override
	public void logFinest(String arg0) {
		logger.trace(arg0);
	}

	@Override
	public void logInfo(String arg0) {
		logger.info(arg0);
	}

	@Override
	public void logSevere(String arg0) {
		logger.error(arg0);
	}

	@Override
	public void logSevere(String arg0, Throwable arg1) {
		logger.error(arg0,arg1);
	}

	@Override
	public void logWarning(String arg0) {
		logger.warn(arg0);
	}

	@Override
	public void logWarning(String arg0, Throwable arg1) {
		logger.warn(arg0,arg1);
	}

}
