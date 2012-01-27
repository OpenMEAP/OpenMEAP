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

#ifndef __OPENMEAP_SLIC_CORE_CONFIG__
#define __OPENMEAP_SLIC_CORE_CONFIG__

#include <openmeap/prefs.h>
#include <openmeap/props.h>

typedef struct om_config_type {
	om_prefs_ptr prefs;
	om_props_ptr props;
} om_config, *om_config_ptr;

enum om_config_enum_type {
    
	OM_CFG_APP_NAME,
	OM_CFG_APP_TITLE,
	OM_CFG_APP_VER,
    OM_CFG_APP_VER_HASH,
	OM_CFG_APP_MGMT_URL,
    
    /**
	 * (uint32) get only.  The development 1 or 0.
	 */
	OM_CFG_DEV_MODE,
    
    /**
	 * (uint32) get only.  Null if the first run...1 from there on after
	 */
	OM_CFG_NOT_FIRST_RUN,
	
	/**
	 * (char *) Where within the packaged application (.apk, .app, .zip, etc) assets may be found.
	 * This is a relative path and ends with a trailing file separator.
	 */
	OM_CFG_APP_PKG_ROOT,
	OM_CFG_SLIC_VER,
	
	/**
	 * (boolean) get/set.  A temporary flag indicating that the app has just been updated and cache should be cleared.
	 */
	OM_CFG_APP_UPDATED,
    
	/**
	 * (char *) get only.  The string device-type of the device we're running on.
	 */
	OM_CFG_DEV_TYPE,
	
	/**
	 * (char *) get only.  Generated the first time the application is run.
	 */
	OM_CFG_DEV_UUID,
	
	/**
	 * (char *) The last auth token given by application service management
	 */
	OM_CFG_AUTH_LAST_TOKEN,

	
	///////////
	// UPDATE RELATED CONFIGURATION
	
	/**
	 * (uint32) The last time, in seconds since unix epoch, an update check was made.
	 */
	OM_CFG_UPDATE_LAST_CHECK,
	
	/**
	 * (uint32) The last time, in seconds since unix epoch, an update attempt was made.
	 */
	OM_CFG_UPDATE_LAST_ATTEMPT,
	
	/**
	 * (uint32) The result code of the last update attempt.
	 */
	OM_CFG_UPDATE_LAST_RESULT,
	
	/**
	 * (boolean) True(1) if we should attempt to pull updates, else False(0)
	 */
	OM_CFG_UPDATE_SHOULD_PULL,
	
	/**
	 * (uint32) get/set.  The number of seconds that must pass between checking for application updates.
	 */
	OM_CFG_UPDATE_FREQ,
	
	
	//////////
	// STORAGE RELATED CONFIGURATION
	
	/**
	 * (char) current storage location 'a' or 'b'
	 */
	OM_CFG_CURRENT_STORAGE,
	
	/**
	 * (char *) The full path to the import archive.  Should be created on the first app startup.
	 */
	OM_CFG_IMPORT_ARCHIVE_PATH
	
};

/**
 * Initialize a configuration using the prefs and props passed in
 */
OM_EXPORT om_config_ptr om_config_obtain(om_prefs_ptr prefs, om_props_ptr props);

/**
 * Release an resources required by the configuratin
 */
OM_EXPORT void om_config_release(om_config_ptr cfg);

/**
 * Obtain the configuration value from the passed in configuration struct.
 * as it could be from prefs or props, it must be freed by the caller
 *
 * @param cfg
 * @param om_config_enum
 * @param val A pointer to the pointer of where to put the value discovered
 */
OM_EXPORT void * om_config_get(om_config_ptr cfg, int om_config_enum);

/**
 * Pulls the value originally packaged with the application.
 */
OM_EXPORT void * om_config_get_original(om_config_ptr cfg, int om_config_enum);

/**
 * Sets a configuration value to the value passed in
 */
OM_EXPORT om_bool om_config_set(om_config_ptr cfg, int om_config_enum, void *val);

#endif

