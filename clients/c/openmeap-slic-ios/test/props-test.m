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

#include <openmeap-slic-core.h>

#import <SenTestingKit/SenTestingKit.h>
#import <UIKit/UIKit.h>

#include <stdlib.h>

#import "unit-tests.h"

@implementation props_test

- (void) testProps {
	om_props_ptr props = om_props_acquire("props-test");
	om_STAssert(props!=OM_NULL,@"Should return a valid properties object.");
	
	char * v = om_props_get(props, "item_one");
	om_STAssert( strcmp(v,"test_value")==0, @"Must be test_value" );
	om_free(v);
	
	v = om_props_get(props, "item_two");
	om_STAssert( strcmp(v,"another_test")==0, @"Must be test_value" );
	om_free(v);
	
	om_STAssert(om_props_get(props,"non existent")==OM_NULL,@"non-existent keys should return OM_NULL");
	
	om_props_release(props);
}

@end
