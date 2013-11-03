/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

#include "unzip.h"

START_TEST(test_list_zip) {
	
	om_unzip_archive_ptr ptr = om_unzip_open_archive("test.zip");
	ASSERT(ptr!=OM_NULL,"Should have been able to open a file");
	om_bool err = om_unzip_archive_into_path(ptr,"/tmp");
	if( err==OM_FALSE ) {
		int n = om_list_count(ptr->error_log);
		for( int i=0; i<n; i++ ) {
			printf("%s\n",om_list_get(ptr->error_log,i));
		}
	}
	om_unzip_close_archive(ptr);
	ASSERT_FREE
	
	om_storage_ptr stg = om_malloc(sizeof(om_storage));
	stg->delete_directory=default_om_storage_delete_directory;
	om_storage_delete_directory( (om_storage_ptr)stg, "/tmp/version-0.0.3a", OM_FALSE );
	om_free(stg);
	
	ASSERT_FREE 
	
} END_TEST()

void run_zip_tests() {
	UNIT_TEST(test_list_zip);
}
