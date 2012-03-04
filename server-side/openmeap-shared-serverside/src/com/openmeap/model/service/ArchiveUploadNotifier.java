/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2012 OpenMEAP, Inc.                                   #
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

package com.openmeap.model.service;

import java.util.Map;

import com.openmeap.constants.UrlParamConstants;
import com.openmeap.model.ArchiveUploadEvent;
import com.openmeap.model.dto.ApplicationArchive;

public class ArchiveUploadNotifier extends AbstractArchiveEventNotifier {	
	protected String getArchiveEventActionName() {
		return ArchiveUploadEvent.NAME;
	}
	protected void addRequestParameters(ApplicationArchive archive, Map<String,Object> parms) {
		parms.put(UrlParamConstants.APPARCH_FILE, archive.getFile(getModelManager().getGlobalSettings().getTemporaryStoragePath()));
	}
}
