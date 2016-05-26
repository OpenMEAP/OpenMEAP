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

package com.openmeap.model.event.notifier;

import java.util.List;

import javax.persistence.PersistenceException;

import com.openmeap.event.Event;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.ModelServiceOperation;
import com.openmeap.model.dto.GlobalSettings;

public class GlobalSettingsAddModifyNotifier extends AbstractModelServiceEventNotifier<GlobalSettings>{

	@Override
	public Boolean notifiesFor(ModelServiceOperation operation,
			ModelEntity payload) {
		return operation==ModelServiceOperation.SAVE_OR_UPDATE && payload instanceof GlobalSettings;
	}

	@Override
	public <E extends Event<GlobalSettings>> void onBeforeOperation(E event,
			List<ProcessingEvent> events) throws EventNotificationException {

		GlobalSettings settings = (GlobalSettings)event.getPayload();
		if( settings==null || settings.getId()==null || !settings.getId().equals(Long.valueOf(1)) ) {
			throw new PersistenceException("There can be only 1 instance of GlobalSettings.  "
					+ "Please first acquire with modelManager.getGlobalSettings(), make modifications, then update.");
		}
	}
}
