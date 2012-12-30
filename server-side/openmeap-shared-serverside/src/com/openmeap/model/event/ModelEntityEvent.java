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

package com.openmeap.model.event;

import com.openmeap.event.AbstractEvent;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.ModelServiceOperation;

public class ModelEntityEvent<T extends ModelEntity> extends AbstractEvent<T> {
	
	private static final long serialVersionUID = 7401170880417711172L;
	private ModelServiceOperation operation;
	
	public ModelEntityEvent(ModelServiceOperation operation, T payload) {
		super(payload);
		this.operation = operation;
	}
	
	public ModelServiceOperation getOperation() {
		return this.operation;
	}
	
	public int hashCode(ModelEntityEvent o) {
		return operation.hashCode() + getPayload().hashCode();
	}
	
	public boolean equals(ModelEntityEvent o) {
		return o.getOperation()==operation && getPayload().equals(o.getPayload());
	}
}
