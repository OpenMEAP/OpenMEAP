/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

import com.openmeap.event.ProcessingEvent;

public class GenericProcessingEvent<T> implements ProcessingEvent<T> {

	protected T payload; 
	protected String[] targets;
	
	public GenericProcessingEvent() {
	}
	
	public GenericProcessingEvent(String target, T payload) {
		this.payload = payload;
		this.targets = new String[]{target};
	}
	
	public GenericProcessingEvent(String[] targets, T payload) {
		this.payload = payload;
		this.targets = targets;
	}
	
	public void setPayload(T object) {
		payload = object;
	}

	public T getPayload() {
		return payload;
	}

	public void setTargets(String[] processTargets) {
		targets = processTargets;
	}

	public String[] getTargets() {
		return targets;
	}
	
	public int hashCode() {
		return payload.hashCode() ^ targets.hashCode();
	}
	
}
