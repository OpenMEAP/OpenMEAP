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

package com.openmeap.thinclient.update;

import com.openmeap.util.GenericException;

public class UpdateException extends GenericException {

	private static final long serialVersionUID = 1940779386542633906L;
	
	private UpdateResult updateResult;
	
	public UpdateException(UpdateResult result) {
		setUpdateResult(result);
	}

	public UpdateException(UpdateResult result, String arg0) {
		super(arg0);
		setUpdateResult(result);
	}

	public UpdateException(UpdateResult result, String arg0, Throwable arg1) {
		super(arg0, arg1);
		setUpdateResult(result);
	}

	public UpdateResult getUpdateResult() {
		return updateResult;
	}
	public void setUpdateResult(UpdateResult updateResult) {
		this.updateResult = updateResult;
	}

}
