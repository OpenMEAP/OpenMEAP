package com.openmeap.thinclient;

import org.apache.http.client.CredentialsProvider;

public interface LoginFormLauncher<T extends CredentialsProvider> {
	public void launchLoginForm(T credentialsProvider);
}
