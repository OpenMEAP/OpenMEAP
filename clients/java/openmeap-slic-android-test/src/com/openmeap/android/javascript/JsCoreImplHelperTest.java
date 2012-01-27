package com.openmeap.android.javascript;

import org.json.JSONObject;

import com.openmeap.protocol.dto.ConnectionOpenResponse;
import com.openmeap.protocol.dto.Hash;
import com.openmeap.protocol.dto.HashAlgorithm;
import com.openmeap.protocol.dto.UpdateHeader;

import junit.framework.TestCase;

public class JsCoreImplHelperTest extends TestCase {
	public void testToJSON() {
		ConnectionOpenResponse cor = new ConnectionOpenResponse();
		cor.setAuthToken("auth-token");
		cor.setUpdate(new UpdateHeader());
		UpdateHeader update = cor.getUpdate();
		update.setHash(new Hash());
		update.getHash().setValue("value");
		update.getHash().setAlgorithm(HashAlgorithm.MD5);
		update.setInstallNeeds(1000L);
		update.setStorageNeeds(2000L);
		update.setUpdateUrl("update-url");
		update.setVersionIdentifier("version-identifier");
	}
}
