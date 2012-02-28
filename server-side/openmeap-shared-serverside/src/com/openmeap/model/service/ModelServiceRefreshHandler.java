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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.Event;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelServiceEventHandler;

public class ModelServiceRefreshHandler implements ModelServiceEventHandler {
	
	private Logger logger = LoggerFactory.getLogger(ModelServiceRefreshHandler.class);
	
	private ModelManager modelManager = null;
	
	public void setModelManager(ModelManager manager) {
		modelManager = manager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}
	
	@Override
	public <E extends Event<ModelEntity>> void handle(E event) {
		if( event.getPayload()!=null ) {
			ModelEntity payload = event.getPayload(); 
			try {
				handleRefresh(event.getPayload().getClass().getSimpleName(), event.getPayload().getPk().toString());
			} catch (ClassNotFoundException e) {
				logger.error("{}",e);
			}
		}
	}
	
	public Boolean handleRefresh(String refreshType, String objectId) throws ClassNotFoundException {
			
		Object id=null;
		if( refreshType.equals("ApplicationInstallation") )
			id = objectId;
		else id = Long.valueOf(objectId);

		@SuppressWarnings("unchecked")
		Class<ModelEntity> clazz = (Class<ModelEntity>)Class.forName("com.openmeap.model.dto."+refreshType);
		ModelEntity app = (ModelEntity)modelManager.getModelService().findByPrimaryKey(clazz, id);
		
		modelManager.getModelService().refresh(app);
		return true;	
	}

}
