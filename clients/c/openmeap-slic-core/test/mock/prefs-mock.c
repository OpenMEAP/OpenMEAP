/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2013 OpenMEAP, Inc.                                   #
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

#include <openmeap-slic-core.h>

void om_mock_prefs_dict_release_entry(void * key, void * value, void * release_data) {
	om_free(key);
	om_free(value);
}

void om_mock_prefs_clear(const om_prefs_ptr prefs) {
	om_dict_clear((om_dict_ptr)prefs->device_data);
}

om_bool om_mock_prefs_set(const om_prefs_ptr prefs, const char *key, const char *value) {
	// the device implementation will have to copy these
	return om_dict_put((om_prefs_ptr)prefs->device_data,om_string_copy(key),om_string_copy(value))!=OM_NULL ? OM_TRUE : OM_FALSE;
}

char * om_mock_prefs_get(const om_prefs_ptr prefs, const char *key) {
	char *val = (char*)om_dict_get((om_dict_ptr)prefs->device_data,key);
	if( val==OM_NULL )
		return OM_NULL;
	char *ret = om_string_copy(val);
	if( ret==OM_NULL ) {
		om_error_set(OM_ERR_MALLOC,"Could not malloc() for string in om_prefs_get()");
		return OM_NULL;
	}
	return ret;
}

void om_mock_prefs_remove(const om_prefs_ptr prefs, const char *key) {
	om_dict_remove((om_dict_ptr)prefs->device_data,key);
}

/////////////////

om_prefs_ptr om_prefs_acquire(const char *name) {
	om_prefs_ptr prefs = om_malloc(sizeof(om_prefs));
	strcpy(prefs->name,name);
	prefs->device_data = om_dict_new(15);
	((om_dict_ptr)prefs->device_data)->release_func = om_mock_prefs_dict_release_entry;
	prefs->get=om_mock_prefs_get;
	prefs->set=om_mock_prefs_set;
	prefs->clear=om_mock_prefs_clear;
	prefs->remove=om_mock_prefs_remove;
	return prefs;
}

void om_prefs_release(om_prefs_ptr prefs) {
	om_dict_release((om_dict_ptr)prefs->device_data);
	om_free(prefs);
}
