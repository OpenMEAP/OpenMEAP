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

package com.openmeap.thinclient.update;

import com.openmeap.protocol.dto.UpdateHeader;

/**
 * Represents a current update in-progress.
 */
public class UpdateStatus {
	private UpdateHeader updateHeader = null;
	private int bytesDownloaded = 0;
	private boolean complete = false;
	private UpdateException error = null;
	public UpdateStatus(UpdateHeader header, int i, boolean b) {
		this.updateHeader = header;
		this.bytesDownloaded = i;
		this.complete = b;
	}
	public UpdateStatus(UpdateHeader header, boolean complete, UpdateException error) {
		this.complete = complete;
		this.error = error;
		this.updateHeader = header;
	}
	public UpdateHeader getUpdateHeader() {
		return updateHeader;
	}
	public void setUpdateHeader(UpdateHeader header) {
		this.updateHeader = header;
	}
	public int getBytesDownloaded() {
		return bytesDownloaded;
	}
	public void setBytesDownloaded(int bytesDownloaded) {
		this.bytesDownloaded = bytesDownloaded;
	}
	public boolean getComplete() {
		return complete;
	}
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	public UpdateException getError() {
		return error;
	}
	public void setError(UpdateException error) {
		this.error = error;
	}
}
