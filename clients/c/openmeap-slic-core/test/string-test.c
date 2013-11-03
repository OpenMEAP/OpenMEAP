/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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
#include "string-test.h"

#include <stdio.h>
#include <string.h>

START_TEST(test_string_substr) {
	char * vals = "This is the string to take a substring of";
	om_substring_ptr ptr = om_substring_new((char*)(vals+12),6);
	char * copy = om_substring_copy(ptr);
	ASSERT(strcmp(copy,"string")==0 && strlen(copy)==6,"string should strcmp==0 with \"string\"");
	ASSERT(om_substring_equals(ptr,"string")==OM_TRUE,"om_substring_equals failed");
	om_free(copy);
	om_substring_release(ptr);
	ASSERT_FREE
} END_TEST();

START_TEST(test_string_substr_new) {
    char * vals = "This is the string to take a substring of";
	om_malloc_fail=OM_TRUE;
	char * ptr = om_substring_new((char*)(vals+12),6);
	ASSERT(ptr==OM_NULL && om_error_get_code()==OM_ERR_MALLOC,"error code should have been set");
	om_error_clear(); om_malloc_fail=OM_FALSE;
    ASSERT_FREE
} END_TEST();

START_TEST(test_string_append) {
	char * newstr = om_string_append("this is ","a string that should be");
	ASSERT(strcmp(newstr,"this is a string that should be")==0,"string contains incorrect content");
	om_free(newstr);
    ASSERT_FREE
} END_TEST();

START_TEST(test_string_substr_range) {
	char * newstr = om_string_substr("this is a string",5,4);
	ASSERT(strcmp(newstr,"is a")==0,"substr should return \"is a\"");
	om_free(newstr);
	ASSERT_FREE
} END_TEST();

START_TEST(test_string_format) {
	char *newstr = om_string_format("%u : %s",1001,"This is a string");
	ASSERT(strcmp("1001 : This is a string", newstr)==0,"String returned was incorrect");
	om_free(newstr);
	ASSERT_FREE
} END_TEST();

START_TEST(test_string_explode) {
	const char *testStr = "this/is/a/typical/path";
	om_list_ptr list = om_string_explode(testStr,'/');
	ASSERT(om_list_count(list)==5,"Should be 5 segments");
    om_list_release(list);
    ASSERT_FREE
} END_TEST();
    
START_TEST(test_string_implode) {    
    const char *testStr = "this/is/a/typical/path";
    om_list_ptr list = om_string_explode(testStr,'/');
	char *newstr = om_string_implode(list,'/');
	ASSERT(strcmp(newstr,testStr)==0,"should be this/is/a/typical/path");
	om_list_release(list);
	om_free(newstr);
	ASSERT_FREE
} END_TEST();	

START_TEST(test_string_encodeURI) {
	const char *toUrlEncode = "this_string should*be@url#encoded";
	char * encoded = om_string_encodeURI(toUrlEncode);
    ASSERT(strcmp(encoded,"this_string%20should*be%40url%23encoded")==0,"result is not correctly encoded");
	om_free(encoded);
	ASSERT_FREE
} END_TEST();

START_TEST(test_string_decodeURI) {
	const char *toUrlDecode = "this_string%20should*be%40url%23encoded";
	char * decoded = om_string_decodeURI(toUrlDecode);
    ASSERT(strcmp(decoded,"this_string should*be@url#encoded")==0,"result is not correctly decoded");
	om_free(decoded);
	ASSERT_FREE
} END_TEST();

void run_string_tests() {
	UNIT_TEST(test_string_substr);
    UNIT_TEST(test_string_substr_new);
    UNIT_TEST(test_string_append);
    UNIT_TEST(test_string_substr_range);
    UNIT_TEST(test_string_format);
    UNIT_TEST(test_string_explode);
    UNIT_TEST(test_string_implode);
    UNIT_TEST(test_string_encodeURI);
    UNIT_TEST(test_string_decodeURI);
}
