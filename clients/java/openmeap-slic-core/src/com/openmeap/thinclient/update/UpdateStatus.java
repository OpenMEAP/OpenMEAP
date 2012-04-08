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
