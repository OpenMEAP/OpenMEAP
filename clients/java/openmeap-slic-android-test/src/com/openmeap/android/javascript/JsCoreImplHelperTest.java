/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Rob Thacher                                 #
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
