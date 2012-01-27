/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

#include <stdio.h>
#include <string.h>

#include <openmeap-slic-core.h>
#include "unit-tests.h"
#include "error-test.h"

START_TEST(test_error_set) 
{
	const char *mesg = "testing om_set_error";
	om_error_set(OM_ERR_APP_SVC_CONN,mesg);
	ASSERT(
			strcmp(mesg,om_error_get_message())==0,
			"expecting om_error_get_message() to return a valid pointer"
		);
	
	om_error_set(OM_ERR_MALLOC,mesg);
	ASSERT(
		   strcmp(om_error_get_message_for_code(OM_ERR_MALLOC),om_error_get_message())==0,
		   "expecting om_error_get_message() to return the default message for malloc"
	   );
	
	ASSERT_FREE
}
END_TEST()

void run_error_tests() {
	UNIT_TEST(test_error_set);
}
