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

#include <stdlib.h>

#import <SenTestingKit/SenTestingKit.h>
#import <UIKit/UIKit.h>

#import "unit-tests.h"

@implementation prefs_test

- (void) testPrefs {
	
	NSString *testPrefs = @"prefs-test";
	NSString *testKey = @"test key";
	NSString *testVal = @"test data";
	
	// validate that a preferences object is created
	om_prefs_ptr prefs = om_prefs_acquire([testPrefs UTF8String]);
	om_STAssert( prefs!=NULL, @"The om_prefs struct was not allocated" );
	
	// make certain that we can set a preference into the dictionary we're establishing
	om_prefs_set(prefs,(const char *)[testKey UTF8String],(const char *)[testVal UTF8String]);
	NSDictionary *dict = prefs->device_data;
	NSString * test = [dict objectForKey:testKey];
	NSString *str = [NSString stringWithFormat:@"The data for key \"%@\" should have been \"%@\"", testKey, testVal];
	om_STAssert( [test compare:testVal]==NSOrderedSame, str );
	
	// make certain our "get" method will allocate a string and return the correct data
	char *val = om_prefs_get(prefs,(const char *)[testKey UTF8String]);
	om_STAssert( val!=OM_NULL, @"The test key should return a value" );
	NSString *star = [NSString stringWithUTF8String:val];
	om_STAssert( [star compare:@"test data"]==NSOrderedSame, @"\"test data\" should have been the value returned" );
	om_prefs_remove(prefs,(const char *)"test");
	om_STAssert( om_prefs_get(prefs,(const char *)"test")==OM_NULL, @"Get should return OM_NULL after we remove the key" );
	free(val);
	
	// assert that we can remove a key
	om_prefs_remove(prefs,(const char *)[testKey UTF8String]);
	NSObject * obj = [[NSUserDefaults standardUserDefaults] objectForKey:testKey];
	om_STAssert( obj==nil, @"The value for key should be nil" );
	
	// make certain we can clear a preferences store
	om_prefs_clear(prefs);
	char * v = om_prefs_get(prefs,[testKey UTF8String]);
	om_STAssert( v==nil, @"The value for key should be nil" );
	
	// make sure we can release a preferences store
	om_prefs_release(prefs);
	
}

@end
