package com.openmeap.thinclient;

public interface LoginFormCallback {
	void onCancel();
	void onProceed(String username, String password, Boolean remember);
	String getInfoText();
}
