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

package com.openmeap.model.event.notifier;

import com.openmeap.model.ModelEntity;
import com.openmeap.model.ModelServiceOperation;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.event.ModelEntityEventAction;

/**
 * Notifies the cluster of the need to delete an application archive file.
 * @author schang
 */
public class ArchiveDeleteNotifier extends AbstractArchiveEventNotifier {
	
	@Override 
	protected String getEventActionName() {
		return ModelEntityEventAction.ARCHIVE_DELETE.getActionName();
	}

	@Override
	public Boolean notifiesFor(ModelServiceOperation operation, ModelEntity payload) {
		if(operation==ModelServiceOperation.DELETE && ApplicationArchive.class.isAssignableFrom(payload.getClass()) ) {
			return true;
		}
		return false;
	}
}
