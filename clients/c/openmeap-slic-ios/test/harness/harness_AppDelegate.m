//
//  openmeap_slic_ios_devAppDelegate.m
//  openmeap-slic-ios-dev
//
//  Created by Jonathan Schang on 5/20/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import "harness_AppDelegate.h"
#import "OmSlicViewController.h"

#import "unit-tests.h"


@implementation harness_AppDelegate

#pragma mark -
#pragma mark Application lifecycle

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
	
	// run all our non-javascript, non-integration-type tests first
	[[[network_test alloc] init] testOmNetDoHttpPost];
	[[[network_test alloc] init] testOmNetDoHttpGetToFileOutputStream];
	
	[[[prefs_test alloc] init] testPrefs];
	
	[[[props_test alloc] init] testProps];
	
	[[[update_test alloc] init] testOmUpdateParseConnresp];
	
	[[[storage_tests alloc] init] testOmStorageGetCurrentAssetsPrefix];
	[[[storage_tests alloc] init] testOmStorageInputStream];
	[[[storage_tests alloc] init] testOmStorageOutputStream];
	[[[storage_tests alloc] init] testOmStorageGetBytesFree];
	
	// give our integration tests a clean slate
	om_prefs_ptr prefs = om_prefs_acquire("slic-prefs");
	om_prefs_clear(prefs);
	om_prefs_release(prefs);
	
	return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

@end
