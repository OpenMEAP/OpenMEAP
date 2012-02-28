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

import com.openmeap.Event;
import com.openmeap.model.dto.ApplicationArchive;

public class ArchiveUploadEvent implements Event<ApplicationArchive> {

	final static public String NAME = "archiveUploadEvent";
	
	private static final long serialVersionUID = -4389253160307376753L;
	
	private ApplicationArchive archive = null;

	public ArchiveUploadEvent(ApplicationArchive archive) {
		this.archive=archive;
	}
	
	@Override
	public void setPayload(ApplicationArchive object) {
		archive = object;
	}

	@Override
	public ApplicationArchive getPayload() {
		return archive;
	}
}
