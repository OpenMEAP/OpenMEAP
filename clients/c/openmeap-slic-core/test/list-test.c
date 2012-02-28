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
#include <stdio.h>
#include <string.h>
#include "unit-tests.h"

START_TEST(test_list) 
{
	char *str[] = {
		"static string\0",
		"static string 2\0"
	};
	
	om_list_ptr myList = om_list_new();
	myList->release_func=OM_NULL;
	ASSERT(myList!=OM_NULL,"allocate a new list failed");
	
	ASSERT(om_list_prepend(myList,str[0]),"prepend a static string failed");
	ASSERT(strcmp(om_list_get(myList,0),str[0])==0,"access an item failed");
	
	ASSERT(om_list_append(myList,str[1]),"appending static string failed");
	ASSERT(strcmp(om_list_get(myList,1),str[1])==0,"get appended string failed");
	
	ASSERT(om_list_append(myList,"test"),"appending static string failed");
	ASSERT(strcmp(om_list_get(myList,2),"test")==0,"get appended string failed");
	
	ASSERT(om_list_count(myList)==3,"count at this point should be 3");
	ASSERT(om_list_remove(myList,str[1])==OM_TRUE,"removing a middle element failed"); 
	ASSERT(om_list_append(myList,str[1]) && om_list_get_index(myList,str[1])==2,
		"appending static string failed");
	
	ASSERT(om_list_get(myList,0)==str[0],"pointer at index 0 should be different");
	
	ASSERT(om_list_count(myList)==3,"count should be 3");
	ASSERT(om_list_remove(myList,str[0])==OM_TRUE && om_list_get_index(myList,str[0])==(-1),
		"removing (1) by pointer failed");
	ASSERT(om_list_remove_index(myList,0)==OM_TRUE,"removing (2) by index failed");
	ASSERT(om_list_remove_index(myList,0)==OM_TRUE,"removing (3) by index failed");
	ASSERT(om_list_remove_index(myList,0)==OM_FALSE && om_list_count(myList)==0,
		"removing (4) by index succeeded but should have failed with an empty list");
	
	om_malloc_fail=OM_TRUE;
	om_bool ret = om_list_append(myList,str[0]);
	ASSERT(ret==OM_FALSE && om_error_get_code()==OM_ERR_MALLOC,"append should have triggered malloc() error");
	om_malloc_fail=OM_FALSE; om_error_clear();
	
	om_malloc_fail=OM_TRUE;
	ret = om_list_prepend(myList,str[0]);
	ASSERT(ret==OM_FALSE && om_error_get_code()==OM_ERR_MALLOC,"prepend should have triggered malloc() error");
	om_malloc_fail=OM_FALSE; om_error_clear();	
	
	om_list_release(myList);
	
	ASSERT_FREE
}
END_TEST()

void run_list_tests(void) {
	UNIT_TEST(test_list);
}