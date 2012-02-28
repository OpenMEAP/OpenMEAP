/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2012 OpenMEAP, Inc.                                   #
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

package com.openmeap.model;

import java.util.Map;

import com.openmeap.Event;

public class ArchiveUploadNotifiedEvent implements Event<Map> {
	
	// TODO: I'm not happy with this, from a purist pov...maybe the Event warrants a Message payload?
	
	final static public String NAME = "archiveUploadNotifiedEvent";
	
	private static final long serialVersionUID = -8871542806763550102L;
	private Map parms = null;

	public ArchiveUploadNotifiedEvent(Map parms) {
		this.parms=parms;
	}
	
	@Override
	public void setPayload(Map object) {
		parms = object;
	}

	@Override
	public Map getPayload() {
		return parms;
	}
}
