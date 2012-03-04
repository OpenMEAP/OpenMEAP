package com.openmeap.model.service;

import java.util.Map;

import com.openmeap.Event;
import com.openmeap.EventHandler;
import com.openmeap.EventHandlingException;

public class ArchiveDeleteHandler implements EventHandler<Map> {

	@Override
	public <E extends Event<Map>> void handle(E event)
			throws EventHandlingException {
		// TODO Auto-generated method stub
		
	}

}
