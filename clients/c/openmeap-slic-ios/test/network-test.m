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
#include <stdlib.h>

#import <SenTestingKit/SenTestingKit.h>
#import <UIKit/UIKit.h>

#import "unit-tests.h"

@implementation network_test

- (void) testOmNetDoHttpPost {
	
	/*om_http_response_ptr resp = om_net_do_http_post("http://www.google.com/","nothing=nothing");
	om_STAssert(resp!=OM_NULL,@"whatever");
	om_STAssert(resp->status_code==200,"@should be 200");
	NSLog([NSString stringWithUTF8String:resp->result]);
	//STAssertNil(om_storage_open_file_output_stream(NULL), @"");
	om_net_release_response(resp);*/
	
}

- (void) testOmNetDoHttpGetToFileOutputStream {
	
	om_props_ptr props = om_props_acquire("props-test");
	om_prefs_ptr prefs = om_prefs_acquire("prefs-test");
	om_prefs_clear(prefs);
	om_config_ptr cfg = om_config_obtain(prefs,props);
	om_storage_ptr stg = om_storage_alloc(cfg);

	char * localStorage = ios_om_storage_get_localstorage_path(nil);
	char * path = om_string_append(localStorage,"/download.html");
	
	om_file_output_stream_ptr ostream = om_storage_open_file_output_stream(stg,path,OM_TRUE);
	om_http_response_ptr resp = 
		om_net_do_http_get_to_file_output_stream("http://localhost:8080" \
												 "/openmeap-admin-web/interface/",ostream,OM_NULL,OM_NULL);
	om_STAssert(resp->status_code==200,@"should be 200 response status code");
	om_storage_close_file_output_stream(ostream);

	om_storage_release(stg);
	om_config_release(cfg);
	om_props_release(props);
	om_prefs_release(prefs);
}
	

@end
