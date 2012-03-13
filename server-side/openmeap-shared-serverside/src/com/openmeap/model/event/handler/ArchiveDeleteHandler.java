package com.openmeap.model.event.handler;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.Event;
import com.openmeap.EventHandler;
import com.openmeap.EventHandlingException;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ClusterNode;

public class ArchiveDeleteHandler implements EventHandler<Map> {

	private Logger logger = LoggerFactory.getLogger(ArchiveDeleteHandler.class);
	
	private ModelManager modelManager;
	
	@Override
	public <E extends Event<Map>> void handle(E event)
			throws EventHandlingException {

		if( logger.isTraceEnabled() ) {
			logger.trace("entering handle()");
		}
		
		ApplicationArchive archive = (ApplicationArchive)event.getPayload().get("archive");
		
		ClusterNode node = modelManager.getClusterNode();
		File file = archive.getFile(node.getFileSystemStoragePathPrefix());
		
		if( file.exists() ) {
			file.delete();
		}
		
		if( logger.isTraceEnabled() ) {
			logger.trace("exiting handle()");
		}
	}
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}

}
