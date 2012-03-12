package com.openmeap.model.service;

import java.util.Map;

import com.openmeap.Event;
import com.openmeap.EventHandler;
import com.openmeap.EventHandlingException;
import com.openmeap.model.ModelManager;

public class ArchiveDeleteHandler implements EventHandler<Map> {

	private ModelManager modelManager;
	
	@Override
	public <E extends Event<Map>> void handle(E event)
			throws EventHandlingException {
		// TODO Auto-generated method stub
		
	}
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}

}
