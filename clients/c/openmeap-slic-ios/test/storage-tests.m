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

#import <SenTestingKit/SenTestingKit.h>
#import <UIKit/UIKit.h>

#import "unit-tests.h"

@implementation storage_tests

- (void) testOmStorageOutputStream {
	
	om_props_ptr props = om_props_acquire("props-test");
	om_prefs_ptr prefs = om_prefs_acquire("prefs-test");
	om_prefs_clear(prefs);
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_CURRENT_STORAGE),"a");
	om_config_ptr cfg = om_config_obtain(prefs,props);
	om_storage_ptr stg = om_storage_alloc(cfg);
	
	char * path = om_string_append( "", "testWriteOutputStream.txt" );
	om_file_output_stream_ptr os = om_storage_open_file_output_stream(stg,path,OM_TRUE);
	om_STAssert(os!=OM_NULL,@"unable to open file");
	char *text = "abcdefghijklmnopqrstuvwxyz";
	om_uint32 bytesWritten = om_storage_write(os,(void *)text,0,26);
	om_STAssert(bytesWritten==26,@"should have written 26 bytes");
	om_storage_close_file_output_stream(os);
    om_free(path);
	
	om_storage_release(stg);
	om_config_release(cfg);
	om_prefs_release(prefs);
	om_props_release(props);
}

- (void) testOmStorageInputStream {	
	
	om_props_ptr props = om_props_acquire("props-test");
	om_prefs_ptr prefs = om_prefs_acquire("prefs-test");
	om_prefs_clear(prefs);
	om_config_ptr cfg = om_config_obtain(prefs,props);
	om_storage_ptr stg = om_storage_alloc(cfg);
	
    char * prefix = om_storage_get_current_assets_prefix(stg);
	char * path = om_string_append( prefix, "/testReadInputStream.txt" );
    om_free(prefix);
	om_file_input_stream_ptr is = om_storage_open_file_input_stream(stg,path);
	om_STAssert(is==OM_NULL,@"unable to open file");
	char *text = om_malloc(4000);
	om_uint32 bytesRead = om_storage_read(is,(void *)text,4000);
	om_STAssert(bytesRead==26,@"should have read 26 bytes");
	om_STAssert(strcmp(text,"abcdefghijklmnopqrstuvwxyz")==0,@"Should have been the all-lower alphabet");
	om_free(text);
	om_storage_close_file_input_stream(is);
    om_free(path);
	
	om_storage_release(stg);
	om_config_release(cfg);
	om_prefs_release(prefs);
	om_props_release(props);
}

- (void) testOmStorageGetCurrentAssetsPrefix {
	
	om_props_ptr props = om_props_acquire("props-test");
	om_prefs_ptr prefs = om_prefs_acquire("prefs-test");
	om_prefs_clear(prefs);

	om_config_ptr cfg = om_config_obtain(prefs,props);
	om_storage_ptr stg = om_storage_alloc(cfg);
	
	om_config_set(stg->cfg,OM_CFG_APP_PKG_ROOT,"original/");
	char * prefix = om_storage_get_current_assets_prefix(stg);
    om_free(prefix);
    
	om_storage_release(stg);
	om_config_release(cfg);
	om_prefs_release(prefs);
	om_props_release(props);
}

- (void) testOmStorageGetBytesFree {
	
	om_props_ptr props = om_props_acquire("props-test");
	om_prefs_ptr prefs = om_prefs_acquire("prefs-test");
	om_prefs_clear(prefs);
	//om_prefs_set(prefs,om_config_map_to_str(OM_CFG_CURRENT_STORAGE),"a");
	om_config_ptr cfg = om_config_obtain(prefs,props);
	om_storage_ptr stg = om_storage_alloc(cfg);

	om_uint64 spaceLeft = om_storage_get_bytes_free(stg);
	om_STAssert(spaceLeft!=0,@"Seriously?  You have no space on the device?  There is prolly something broken here.");
	
	om_storage_release(stg);
	om_config_release(cfg);
	om_prefs_release(prefs);
	om_props_release(props);

}

@end
