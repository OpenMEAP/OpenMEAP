/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.model;

import com.openmeap.Event;

public class ModelEntityModifyEvent implements Event<ModelEntity> {
	
	// TODO: there should be a NAME constant here which is used everywhere else
	final static public String NAME = "refresh";
	
	private static final long serialVersionUID = 5825309477564008214L;
	
	private ModelEntity payload = null;
	
	public ModelEntityModifyEvent(ModelEntity payload) {
		setPayload(payload);
	}
	
	@Override
	public void setPayload(ModelEntity object) {
		payload = object;
	}

	@Override
	public ModelEntity getPayload() {
		return payload;
	}

}
