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

#include <openmeap-slic-core.h>
#include "unit-tests.h"
#include "storage-test.h"

#include <memory.h>

START_TEST(test_storage) {
	
	char *ptr = OM_NULL;
	om_bool result = OM_FALSE;
	
	om_storage_ptr stg = om_storage_alloc(NULL);
	
	// just make sure i can use my mock vars object
	om_storage_mock_vars vars;
	memset( (void*)&vars, 0, sizeof(om_storage_mock_vars) );
	om_storage_set_mock_vars(&vars);
	vars.get_bytes_free_result=666;
	ASSERT((void*)om_storage_get_bytes_free(stg)==666,"should have been correct");	
	
	om_props_ptr props = om_props_acquire("test properties");
	ASSERT(props!=OM_NULL,"props should not be null");
	om_prefs_ptr prefs = om_prefs_acquire("test preferences");
	ASSERT(prefs!=OM_NULL,"prefs should not be null");
	om_config_ptr cfg = om_config_obtain(prefs,props);
	ASSERT(cfg!=OM_NULL,"cfg should not be null");
	
	om_storage_release(stg);
	
	// verify that we can flip flop the storage location
	// must be a str so strlen will work in supporting code
	/*om_config_set(cfg,OM_CFG_CURRENT_STORAGE,"a");
	
	stg = om_storage_alloc(cfg);
	om_storage_do_flip_flop(stg);
	ptr = om_config_get(cfg,OM_CFG_CURRENT_STORAGE);
	ASSERT(ptr[0]=='b',"we should be in the 'b' storage location now");
	om_free(ptr);
	//om_storage_free_mock_vars(&vars);
	om_storage_do_flip_flop(stg);
	ptr = om_config_get(cfg,OM_CFG_CURRENT_STORAGE);
	ASSERT(ptr[0]=='a',"we should be in the 'a' storage location now");
	om_free(ptr);*/
	
	// validate the delete import archive function
    stg = om_storage_alloc(cfg);
	om_config_set(cfg,OM_CFG_IMPORT_ARCHIVE_PATH,"path");
	vars.delete_file_result = OM_TRUE;
	result = om_storage_delete_import_archive(stg);
	ASSERT(result==OM_TRUE,"the result from delete_file_result should have made it here");
	ASSERT(strcmp(vars.delete_file_arg,"path")==0,"om_storage_delete_import_archive called om_storage_delete_file with the wrong arg");
	om_storage_free_mock_vars(&vars);
	
	om_storage_release(stg);
	om_config_release(cfg);
	om_prefs_release(prefs);
	om_props_release(props);
	om_storage_free_mock_vars(vars);
	
	ASSERT_FREE	
}
END_TEST()

void run_storage_tests() {
	UNIT_TEST(test_storage);
}

