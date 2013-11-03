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

package com.openmeap.model.event.notifier;

import java.util.List;

import com.openmeap.event.Event;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.ModelEntity;

abstract public class AbstractModelServiceEventNotifier<T extends ModelEntity> 
		implements ModelServiceEventNotifier<T> {

	@Override
	public <E extends Event<T>> void onInCommitAfterCommit(E event,
			List<ProcessingEvent> events) throws EventNotificationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <E extends Event<T>> void onInCommitBeforeCommit(E event,
			List<ProcessingEvent> events) throws EventNotificationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <E extends Event<T>> void onBeforeOperation(E event,
			List<ProcessingEvent> events) throws EventNotificationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <E extends Event<T>> void onAfterOperation(E event,
			List<ProcessingEvent> events) throws EventNotificationException {
		// TODO Auto-generated method stub
		
	}
}
