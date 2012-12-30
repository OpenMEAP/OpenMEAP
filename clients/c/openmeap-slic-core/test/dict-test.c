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
#include "unit-tests.h"
#include "dict-test.h"

void * __keyPassed = OM_NULL;
void * __valuePassed = OM_NULL;
void * __releaseDataPassed = OM_NULL;

OM_PRIVATE_FUNC void __mock_release_dict_func(void *key, void *value, void *release_data) {
	__keyPassed = key;
	__valuePassed = value;
	__releaseDataPassed = release_data;
}

START_TEST(test_dict) {
	int hashVal = om_dict_hash_string("jon");
	ASSERT(hashVal==107,"should be 107");
	hashVal = om_dict_hash_string("jons");
	ASSERT(hashVal==24,"should be 24");
    ASSERT_FREE
}END_TEST()

/**
 * Validate that the count function is correct
 */
START_TEST(test_dict_put_get_count) {
	om_dict_ptr dict = om_dict_new(20);
	om_dict_put(dict,"test","value");
	om_dict_put(dict,"test1","value1");
	om_dict_put(dict,"test2","value2");
	char *val = om_dict_get(dict,"test");
	ASSERT(val!=OM_NULL && strcmp(val,"value")==0,"should be 'value'");
	ASSERT(om_dict_count(dict)==3,"the dict should have 3 values at this point");
	ASSERT(strcmp(om_dict_remove(dict,"test1"),"value1")==0 
		&& om_dict_count(dict)==2
		&& strcmp(om_dict_get(dict,"test"),"value")==0,
		"value should be returned and removed");
	om_dict_release(dict);
	ASSERT_FREE
}END_TEST()

/**
 * Validate that the release function is called on overwriting a value.
 */
START_TEST(test_dict_release_func_on_overwrite) {
	om_dict_ptr dict = om_dict_new(15);
	
	dict->release_func=&__mock_release_dict_func;
	om_dict_put(dict,"test4","value4");
	om_dict_put(dict,"test5","value5");
	om_dict_put(dict,"test6","value6");
	ASSERT(om_dict_count(dict)==3,"count should, again, be 3");
    
	om_dict_put(dict,"test6","another value");
	ASSERT(strcmp(om_dict_get(dict,"test6"),"another value")==0,
	   "should be 'another value'");
	ASSERT( __keyPassed!=OM_NULL
		   && __valuePassed!=OM_NULL
		   && __releaseDataPassed==OM_NULL,
		"release_func should have been called");
    om_dict_release(dict);
    ASSERT_FREE
}END_TEST()

/**
 * Validate that put returns false when an alloc failure occurs.
 */
START_TEST(test_dict_alloc_failures) {
    om_dict_ptr dict = om_dict_new(15);
	om_malloc_fail=OM_TRUE;
	om_bool ret = om_dict_put(dict,"test7","test7");
	ASSERT(ret==OM_FALSE && om_error_get_code()==OM_ERR_MALLOC,"a failure should have been triggered");
	om_error_clear(); om_malloc_fail=OM_FALSE;
    om_dict_release(dict);
    ASSERT_FREE
}END_TEST()

/**
 * Verify that we can successfully clear the dict.
 */
START_TEST(test_dict_clear) {
    om_dict_ptr dict = om_dict_new(15);
    om_dict_put(dict,"test","value4");
	om_dict_put(dict,"test5","value5");
	om_dict_put(dict,"test6","value6");
	om_dict_clear(dict);
	ASSERT(om_dict_get(dict,"test")==OM_NULL && om_dict_count(dict)==0, "dict should be clear now");
	for( int i=0; i<dict->bucket_count; i++ ) {
		ASSERT(dict->buckets[i]==OM_NULL,"buckets should be all null");		
	}
	om_dict_release(dict);
	ASSERT_FREE
} END_TEST()
		   
/**
 * Verify the om_dict_from_query_string method.
 */
START_TEST(test_dict_from_query_string) { 
    char * query = "key1=value1&key2=value2&key3=value3";
    om_dict_ptr dict = om_dict_from_query_string(query);
    char * r=OM_NULL;
    ASSERT(dict!=OM_NULL && om_dict_count(dict)==3,"dict should have 3 entries");
    r=om_dict_get(dict,"key1");
    ASSERT(strcmp(r,"value1")==0,"key/value pair is incorrect, should be 1");
    r=om_dict_get(dict,"key2");
    ASSERT(strcmp(r,"value2")==0,"key/value pair is incorrect, should be 2");
    r=om_dict_get(dict,"key3");
    ASSERT(strcmp(r,"value3")==0,"key/value pair is incorrect, should be 3");
    om_dict_release(dict);
    ASSERT_FREE    
}END_TEST()

void run_dict_tests() {
	UNIT_TEST(test_dict);
    UNIT_TEST(test_dict_put_get_count);
    UNIT_TEST(test_dict_release_func_on_overwrite);
    UNIT_TEST(test_dict_alloc_failures);
    UNIT_TEST(test_dict_clear);
    UNIT_TEST(test_dict_from_query_string);
}
