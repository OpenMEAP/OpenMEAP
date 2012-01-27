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

#include <openmeap-slic-core.h>

#include <stdio.h>
#include <stdlib.h>
#include <memory.h>

void * __om_config_get_convert(int om_config_enum, void *passed_in);

om_config_ptr om_config_obtain(om_prefs_ptr prefs, om_props_ptr props) {
	om_config_ptr conf = (om_config_ptr)om_malloc(sizeof(om_config));
	if( conf==NULL ) {
		om_error_set_code(OM_ERR_MALLOC);
		return OM_NULL;
	}
	conf->props = props;
	conf->prefs = prefs;
	return conf;
}

void om_config_release(om_config_ptr cfg) {
	om_free(cfg);
}

const char * om_config_map_to_str(int om_config_enum) {
	switch(om_config_enum) {
		case OM_CFG_APP_NAME:
			return "com.openmeap.slic.appName";
		case OM_CFG_APP_TITLE:
			return "com.openmeap.slic.appTitle";
		case OM_CFG_APP_VER:
			return "com.openmeap.slic.appVersion";
        case OM_CFG_APP_VER_HASH:
            return "com.openmeap.slic.appVersionHash";
		case OM_CFG_APP_MGMT_URL:
			return "com.openmeap.slic.appMgmtServiceUrl";
        case OM_CFG_DEV_MODE:
            return "com.openmeap.slic.developmentMode";
        case OM_CFG_NOT_FIRST_RUN:
            return "com.openmeap.slic.notFirstRun";
		case OM_CFG_APP_PKG_ROOT:
			return "com.openmeap.slic.packagedAppRoot";
        case OM_CFG_APP_UPDATED:
            return "com.openmeap.slic.appUpdated";
		case OM_CFG_SLIC_VER:
			return "com.openmeap.slic.version";
		case OM_CFG_DEV_TYPE:
			return "com.openmeap.slic.deviceType";
		case OM_CFG_DEV_UUID:
			return "com.openmeap.slic.deviceUuid";
		case OM_CFG_AUTH_LAST_TOKEN:
			return "com.openmeap.slic.lastAuthToken";
		case OM_CFG_UPDATE_LAST_ATTEMPT:
			return "com.openmeap.slic.lastUpdateAttempt";
		case OM_CFG_UPDATE_LAST_RESULT:
			return "com.openmeap.slic.lastUpdateResult";
		case OM_CFG_UPDATE_LAST_CHECK:
			return "com.openmeap.slic.lastUpdateCheck";
		case OM_CFG_UPDATE_FREQ:		
			return "com.openmeap.slic.updateFrequency";
		case OM_CFG_UPDATE_SHOULD_PULL:
			return "com.openmeap.slic.pullUpdates";
		case OM_CFG_CURRENT_STORAGE:
			return "com.openmeap.slic.storage.current";
        case OM_CFG_IMPORT_ARCHIVE_PATH:
            return "com.openmeap.slic.storage.importArchive";
		default:
			return "";
	}
}

void * om_config_get(om_config_ptr cfg, int om_config_enum) {
	
	const char * key = om_config_map_to_str(om_config_enum);
	void * toret = (void *)om_prefs_get(cfg->prefs,key);
	
	if( toret == OM_NULL )
		toret = (void *)om_props_get(cfg->props,key);
	
	if( toret == OM_NULL )
		return OM_NULL;
	
	toret = __om_config_get_convert(om_config_enum, toret);
	
	return toret;
}

void * om_config_get_original(om_config_ptr cfg, int om_config_enum) {
	
	const char * key = om_config_map_to_str(om_config_enum);
	void * toret = (void *)om_props_get(cfg->props,key);
	
	if( toret == OM_NULL )
		return OM_NULL;
	
	toret = __om_config_get_convert(om_config_enum, toret);
	
	return toret;	
}

om_bool om_config_set(om_config_ptr cfg, int om_config_enum, void *val) {
	const char * key = om_config_map_to_str(om_config_enum);
	void * toins = val;
	char * buffer = 0;
	uint32 a = 0;
	
	// transform from the type passed in
	switch(om_config_enum) {
		case OM_CFG_UPDATE_LAST_ATTEMPT:
		case OM_CFG_UPDATE_FREQ:
		case OM_CFG_UPDATE_SHOULD_PULL:
		case OM_CFG_UPDATE_LAST_CHECK:
        case OM_CFG_APP_UPDATED:
        case OM_CFG_DEV_MODE:
        case OM_CFG_NOT_FIRST_RUN:
            buffer = om_malloc(sizeof(char)*32);
			a = *(om_uint32*)val;
			sprintf( buffer, "%u", a );
			toins = buffer;
			break;
		default:
			toins = om_string_copy(val);
			break;
	}
	
	om_bool ret = om_prefs_set(cfg->prefs,key,toins);
    om_free(toins);
    return ret;
}

void * __om_config_get_convert(int om_config_enum, void *passed_in) {
	
	void * toret = passed_in;
	void * convert_from = toret;
	
	// transform to the type expected by the caller
	switch(om_config_enum) {
		case OM_CFG_UPDATE_LAST_ATTEMPT:
		case OM_CFG_UPDATE_FREQ:
		case OM_CFG_UPDATE_SHOULD_PULL:
		case OM_CFG_UPDATE_LAST_CHECK:
        case OM_CFG_APP_UPDATED:
        case OM_CFG_DEV_MODE:
        case OM_CFG_NOT_FIRST_RUN:
			toret = om_malloc(sizeof(uint32)*1);
			if( toret==OM_NULL ) {
				om_error_set(OM_ERR_MALLOC,"could not allocate uint32");
				return OM_NULL;
			}	
			*((om_uint32*)toret) = atoi( (char*)convert_from );
			om_free(convert_from);
			break;
	}
	
	return toret;
}


