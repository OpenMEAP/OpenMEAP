package com.openmeap.thinclient.update;

import com.openmeap.protocol.dto.UpdateHeader;

/**
 * Represents a current update in-progress.
 */
public class UpdateStatus {
	private UpdateHeader updateHeader = null;
	private Integer bytesDownloaded = 0;
	private Boolean complete = false;
	private UpdateException error = null;
	public UpdateStatus(UpdateHeader header, Integer bytesDownloaded, Boolean complete) {
		this.updateHeader = header;
		this.bytesDownloaded = bytesDownloaded;
		this.complete = complete;
	}
	public UpdateStatus(UpdateHeader header, Boolean complete, UpdateException error) {
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
	public Integer getBytesDownloaded() {
		return bytesDownloaded;
	}
	public void setBytesDownloaded(Integer bytesDownloaded) {
		this.bytesDownloaded = bytesDownloaded;
	}
	public Boolean getComplete() {
		return complete;
	}
	public void setComplete(Boolean complete) {
		this.complete = complete;
	}
	public UpdateException getError() {
		return error;
	}
	public void setError(UpdateException error) {
		this.error = error;
	}
}
