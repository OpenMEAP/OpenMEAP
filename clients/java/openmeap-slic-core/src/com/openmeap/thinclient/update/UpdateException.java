package com.openmeap.thinclient.update;

public class UpdateException extends Exception {

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
