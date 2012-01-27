package com.openmeap.thinclient;

public interface LoginFormCallback {
	public void onCancel();
	public void onProceed(String username, String password, Boolean remember);
}
