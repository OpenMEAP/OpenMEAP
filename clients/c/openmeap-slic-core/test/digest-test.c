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

#include <stdio.h>
#include <string.h>

#include <openmeap-slic-core.h>
#include "unit-tests.h"
#include "digest-test.h"

START_TEST(test_digest_set) 
{
	om_props_ptr props = om_props_acquire("test properties");
	om_prefs_ptr prefs = om_prefs_acquire("test preferences");
	om_config_ptr cfg = om_config_obtain(prefs,props);
	om_storage_ptr stg = default_om_storage_alloc(cfg);
	
	om_file_input_stream *istr = om_storage_open_file_input_stream(stg,"test.zip");
	ASSERT(istr!=OM_NULL,"Whoops!");
	
	char *md5 = om_digest_hash_file(istr,OM_HASH_MD5);
	ASSERT(strcmp(md5,"df9c8c31053a0cc3f52d0a593ca330f3")==0,"Md5 message digest did not match expectations");
	om_free(md5);
	
	rewind((FILE*)istr->device_data);
	char *sha1 = om_digest_hash_file(istr,OM_HASH_SHA1);
	ASSERT(strcmp(sha1,"8a57f901db58425430021a8dfbfaaf543a42ed2f")==0,"Sha1 message digest did not match expectations.");
	om_free(sha1);
	
	om_storage_close_file_input_stream(istr);
	
	om_storage_release(stg);
	om_config_release(cfg);
	om_prefs_release(prefs);
	om_props_release(props);
	
	ASSERT_FREE
}
END_TEST()

void run_digest_tests() {
	UNIT_TEST(test_digest_set);
}