/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
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

#include <stdio.h>
#include <string.h>

#include <openmeap-slic-core.h>

#include "unit-tests.h"
#include "error-test.h"
#include "list-test.h"
#include "storage-test.h"
#include "dict-test.h"
#include "config-test.h"
#include "update-test.h"
#include "zip-test.h"
#include "digest-test.h"

int main (void)
{
	run_dict_tests();
	run_string_tests();
	run_zip_tests();
	run_core_tests();
	run_error_tests();
	run_list_tests();
	run_update_tests();
	run_config_tests();
	run_storage_tests();
	run_digest_tests();
	return 0;
}
