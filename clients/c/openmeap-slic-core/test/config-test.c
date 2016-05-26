/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Rob Thacher                                 #
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

#include "unit-tests.h"
#include "config-test.h"

START_TEST(test_config) {
	
	om_props_ptr props = om_props_acquire("properties");
	om_prefs_ptr prefs = om_prefs_acquire("preferences");
	ASSERT(props!=OM_NULL && prefs!=OM_NULL,"prefs and props should be set");
	
	om_dict_put(props->device_data,"com.openmeap.slic.appName","app_name");
	
	// validate that failure to allocate functions as expected
	om_malloc_fail = OM_TRUE;	
	om_config_ptr cfg = om_config_obtain(prefs,props);
	ASSERT(cfg==OM_NULL && om_error_get_code()==OM_ERR_MALLOC,"should have set error and returned null");
	om_error_clear(); om_malloc_fail = OM_FALSE;
	
	// validate that a config object can be allocated
	cfg = om_config_obtain(prefs,props);
	ASSERT(cfg!=OM_NULL,"should be able to get a config object");
				
	// verify that error is set and null is returned if the string cannot be copied
	om_malloc_fail = OM_TRUE;
	char *ptr = om_config_get(cfg,OM_CFG_APP_NAME);
	ASSERT(ptr==OM_NULL && om_error_get_code()==OM_ERR_MALLOC,"should have set error and returned null");
	om_error_clear(); om_malloc_fail = OM_FALSE;
	
	// test that the initial props value will be returned,
	// if the prefs haven't been updated
	ptr = om_config_get(cfg,OM_CFG_APP_NAME);
	ASSERT(strcmp(ptr,"app_name")==0,"should be app_name");
	om_free(ptr);
	
	om_config_set(cfg,OM_CFG_APP_NAME,"app_name_2");
	
	ptr = om_config_get(cfg,OM_CFG_APP_NAME);
	ASSERT(strcmp(ptr,"app_name_2")==0,"should be app_name_2 at this point");
	om_free(ptr); // per the interface contract, i need to free this.
	
	ptr = om_config_get_original(cfg,OM_CFG_APP_NAME);
	ASSERT(strcmp(ptr,"app_name")==0,"the original should return app_name at this point.");
	om_free(ptr);
	
	ptr = om_dict_get(props->device_data, "com.openmeap.slic.appName");
	ASSERT(strcmp(ptr,"app_name")==0, "props data should not have changed");	
	
	// test the type conversion
	om_uint32 last_update=9827343;
	om_uint32 *last_update_ptr;
	om_config_set(cfg,OM_CFG_UPDATE_LAST_ATTEMPT,&last_update);
	last_update_ptr = om_config_get(cfg,OM_CFG_UPDATE_LAST_ATTEMPT);
	ASSERT( *last_update_ptr == last_update, "the last update uint32 should have survived the round trip" );
	om_free(last_update_ptr);
	
	
	
	last_update=9827343;
	*last_update_ptr;
	last_update_ptr = om_config_get(cfg,OM_CFG_UPDATE_FREQ);
	ASSERT( last_update_ptr == OM_NULL && om_error_get_code()==OM_ERR_NONE, "the last update uint32 should have survived the round trip" );
	om_free(last_update_ptr);
	
	om_props_release(props);
	om_prefs_release(prefs);
	om_config_release(cfg);
	
	ASSERT_FREE
} END_TEST()

void run_config_tests() {
	UNIT_TEST(test_config);
}
