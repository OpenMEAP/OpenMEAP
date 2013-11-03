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

package com.openmeap.model.event.notifier;

import java.util.Map;

import com.openmeap.constants.UrlParamConstants;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.dto.ApplicationArchive;

abstract public class AbstractArchiveFileEventNotifier<T extends ModelEntity> extends AbstractModelServiceClusterServiceMgmtNotifier<T> {
	
	protected void addRequestParameters(ModelEntity modelEntity, Map<String,Object> parms) {
		ApplicationArchive archive = (ApplicationArchive)modelEntity;
		String hash = archive.getHash();
		String hashType = archive.getHashAlgorithm();
		parms.put(UrlParamConstants.APPARCH_HASH, hash);
		parms.put(UrlParamConstants.APPARCH_HASH_ALG, hashType);
	}
}
