package com.openmeap.blackberry;

import net.rim.device.api.system.CodeModuleListener;

import com.openmeap.thinclient.LocalStorageException;
import com.openmeap.util.GenericRuntimeException;

public class ApplicationDeleteCleanup implements CodeModuleListener {

	LocalStorageImpl storage;
	BlackberrySLICConfig config;
	
	public void moduleDeletionsPending(String[] moduleNames) {
		storage = (LocalStorageImpl) OpenMEAPApp.getInstance().getStorage();
		config = (BlackberrySLICConfig) OpenMEAPApp.getInstance().getConfig();
		try {
			if(!config.isVersionOriginal(config.getApplicationVersion()).booleanValue()) {
				
				// delete config prefs (in blackberry, clear removes the prefs file)
				config.getPreferences().clear();
				
				// delete everything for the app
				storage.resetStorage(OpenMEAPApp.STORAGE_ROOT);
			}
		} catch(LocalStorageException lse) {
			throw new GenericRuntimeException(lse);
		}
	}

	public void modulesAdded(int[] handles) {
	}

	public void modulesDeleted(String[] moduleNames) {
	}

}
