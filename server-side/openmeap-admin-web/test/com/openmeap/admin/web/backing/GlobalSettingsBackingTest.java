/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.admin.web.backing;

import java.util.Map;
import java.util.HashMap;

import com.openmeap.admin.web.ProcessingTargets;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelTestUtils;
import com.openmeap.util.ParameterMapUtils;

import org.junit.Test;

public class GlobalSettingsBackingTest {

	@Test public void testSettingsProcess() {
		
		ModelTestUtils.resetTestDb();
		ModelTestUtils.createModel(null);
		ModelManager mm = ModelTestUtils.createModelManager();
		
		GlobalSettingsBacking backing = new GlobalSettingsBacking();
		backing.setModelManager(mm);
		
		Map<Object,Object> templateVariables = new HashMap<Object,Object>();
		Map<Object,Object> parameterMap = new HashMap<Object,Object>();
		ParameterMapUtils.setValue("processTarget", ProcessingTargets.GLOBAL_SETTINGS, parameterMap);
		backing.process(null, templateVariables, parameterMap);
		ModelTestUtils.resetTestDb();
	}
	
}
