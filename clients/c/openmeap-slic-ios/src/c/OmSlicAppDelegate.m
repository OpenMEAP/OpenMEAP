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

#import "OmSlicAppDelegate.h"
#import "OmSlicViewController.h"
#import "OmSlicJsApiProtocol.h"
#import "OmSlicConnectionHandler.h"

#import <sys/stat.h>

@implementation OmSlicAppDelegate

@synthesize window;
@synthesize viewController;
@synthesize loginViewController;

@synthesize updateHeader;

@synthesize config;
@synthesize storage;

static OmSlicAppDelegate *__globalOmSlicAppDelegateInstance;

+ (OmSlicAppDelegate*) globalInstance {
	return __globalOmSlicAppDelegateInstance;
}

- (OmSlicAppDelegate*) init {
    self = [super init];
	__globalOmSlicAppDelegateInstance = self;
    self->updateHeader = OM_NULL;
    return self;
}

#pragma mark -
#pragma mark Application lifecycle

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
	
	NSLog(@"in OmSlicAppDelegate::didFinishLaunchingWithOptions");
	
	// load in current configuration
	om_props_ptr props = om_props_acquire("slic-config");
	om_prefs_ptr prefs = om_prefs_acquire("slic-prefs");
	self->config = om_config_obtain(prefs,props);
	self->storage = om_storage_alloc(self->config);
	if( props==OM_NULL 
			|| prefs==OM_NULL 
			|| self->config==OM_NULL
			|| self->storage==OM_NULL ) {
		return NO;
	}
	NSLog(@"-- configuration loaded");
    
    // perform our first run logic
    dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
	dispatch_async(queue, ^{ 
        NSLog(@"--about to check for first run");
        @synchronized([OmSlicAppDelegate class]) {
            om_first_run_check(self->config);
        } 
    });
	
	// make sure all the storage configuration items are correctly setup
	if( [self initializeStorage] == NO )
		return NO;
	NSLog(@"-- storage initialized");
	
	// check for and create a guid
	char * uuid = om_config_get(self->config,OM_CFG_DEV_UUID);
	
	if( uuid == OM_NULL ) {
		
		// TODO: do not rely on the device to generate this, not this way
		char * str = om_string_format("%ul",(unsigned long)random());
		
		// TODO: error handle memory
		om_config_set(self->config,OM_CFG_DEV_UUID,str);
		om_free(str);
	} else om_free(uuid);
	NSLog(@"-- dev uuid checked");
	
	NSLog(@"in OmSlicAppDelegate::applicationDidBecomeActive");
	[self reload];
	
    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application {
    /*
     Sent when the application is about to move from active to inactive state. 
	 This can occur for certain types of temporary interruptions 
	 (such as an incoming phone call or SMS message) or when the user quits 
	 the application and it begins the transition to the background state.
     Use this method to pause ongoing tasks, disable timers, and throttle down 
	 OpenGL ES frame rates. Games should use this method to pause the game.
     */
	NSLog(@"in OmSlicAppDelegate::applicationWillResignActive");

    if( self.loginViewController!=nil && self.loginViewController==self.window.rootViewController ) {
        [self.loginViewController cancel:self];
    }
    
	[NSURLProtocol unregisterClass:[OmSlicJsApiProtocol class]];
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
    /*
     Use this method to release shared resources, save user data, invalidate timers, 
	 and store enough application state information to restore your application to its 
	 current state in case it is terminated later. 
     If your application supports background execution, 
	 called instead of applicationWillTerminate: when the user quits.
     */
	NSLog(@"in OmSlicAppDelegate::applicationDidEnterBackground");
	[NSURLProtocol unregisterClass:[OmSlicJsApiProtocol class]];
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    /*
     Called as part of  transition from the background to the inactive state: 
	 here you can undo many of the changes made on entering the background.
     */
	
	// for now, we'll re-initialize the view
	// each time the application is foregrounded
	NSLog(@"in OmSlicAppDelegate::applicationWillEnterForeground");
	[self reload];
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
    /*
     Restart any tasks that were paused (or not yet started) while the application was inactive. 
	 If the application was previously in the background, optionally refresh the user interface.
     */
}


- (void)applicationWillTerminate:(UIApplication *)application {
    /*
     Called when the application is about to terminate.
     See also applicationDidEnterBackground:.
     */
	[NSURLProtocol unregisterClass:[OmSlicJsApiProtocol class]];
}


#pragma mark -
#pragma mark Memory management

- (void)applicationDidReceiveMemoryWarning:(UIApplication *)application {
    /*
     Free up as much memory as possible by purging cached data objects that can be recreated (or reloaded from disk) later.
     */
}


- (void)dealloc {
	
	om_config_ptr cfg = self.config;
	om_props_ptr props = (cfg->props);
	om_prefs_ptr prefs = (cfg->prefs);
	om_props_release(props);
	om_prefs_release(prefs);
	om_config_release(self.config);
	om_storage_release(self.storage);
	if( self.updateHeader!=OM_NULL ) {
        om_update_release_update_header(self.updateHeader);
        self.updateHeader=OM_NULL;
    }
    [viewController release];
    [window release];
    [super dealloc];
}

#pragma mark - 
#pragma mark Various Utility Methods

- (void) reload {
    NSLog(@"in OmSlicAppDelegate::reload");

    // make sure the screen is blanked out, in-case they are returning to a running instance
    self.window.rootViewController = nil;
    
    // insure that a stale update isn't lying about
    [self.viewController setUpdateHeaderJSON:nil];
    if( self.updateHeader!=OM_NULL ) {
        om_update_release_update_header(self.updateHeader);
        self.updateHeader=OM_NULL;
    }

    dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
	dispatch_async(queue, ^{ 
        NSLog(@"--about to check for update");
        @synchronized([OmSlicAppDelegate class]) {
            [self performUpdateCheck];
            [self performSelectorOnMainThread:@selector(initializeView) withObject:nil waitUntilDone:NO];
        } 
    });
}

- (void) showAlert:(NSString*)message withTitle:(NSString*)title {
	
	SEL sel = @selector(_showAlert:withTitle:);
	
	NSMethodSignature * mySignature = [OmSlicAppDelegate instanceMethodSignatureForSelector:sel];
	NSInvocation * inv = [NSInvocation invocationWithMethodSignature:mySignature];
	
	NSString *msgCopy = [NSString stringWithString: message];
	[msgCopy retain];
	NSString *titleCopy = [NSString stringWithString: title];
	[titleCopy retain];
	
	[inv setTarget:self];
	[inv setSelector:sel];
	[inv setArgument:&msgCopy atIndex:2];
	[inv setArgument:&titleCopy atIndex:3];
	
	[inv performSelectorOnMainThread:@selector(invokeWithTarget:) withObject:self waitUntilDone:NO];
}

- (void) _showAlert:(NSString*)message withTitle:(NSString*)title {
	
	NSLog(@"in OmSlicAppDelegate _showAlert:withTitle:");
	NSLog(@"--creating alert with title %@ and message %@",title,message);
	
	//Create UIAlertView alert
	UIAlertView *alert = [[UIAlertView alloc] initWithTitle:title 
													message:message 
												   delegate:self 
										  cancelButtonTitle:@"dismiss"
										  otherButtonTitles:nil];
	
	//After some time
	[alert dismissWithClickedButtonIndex:0 animated:TRUE];
	[alert show];
	
	[message release];
	[title release];
}

#pragma mark -
#pragma mark OpenMEAP View Controller related

- (void) clearWebCache {
    NSString * bundleId = [[NSBundle mainBundle] bundleIdentifier]; 
    NSString * cachePath = [[NSString stringWithFormat:@"~/Library/Caches/%@/Cache.db",bundleId] stringByExpandingTildeInPath];
    NSLog(@"--cachePath: %@",cachePath);
    
    NSError *error;
    NSFileManager *fileMgr = [NSFileManager defaultManager];
    if ([fileMgr removeItemAtPath:cachePath error:&error] != YES) {
        NSLog(@"Unable to delete file: %@", [error localizedDescription]);
    }
    
    [[NSURLCache sharedURLCache] removeAllCachedResponses];
}

- (BOOL) initializeView {
	
	NSLog(@"in OmSlicAppDelegate::initializeView");
	
	if( [NSURLProtocol registerClass:[OmSlicJsApiProtocol class]] == NO ) {
		NSLog(@"-- failed to register OmSlicJsApiProtocol");
	}
    
    // this should be flipped to true exclusively at the end of the om_update_perform() func
    int *updated = om_config_get(config,OM_CFG_APP_UPDATED);
    int cachePolicy = NSURLRequestUseProtocolCachePolicy;
	if( updated!=OM_NULL && *updated==1 ) {
        *updated=0;
        om_config_set(config,OM_CFG_APP_UPDATED,updated);
        om_free(updated);
        
		cachePolicy = NSURLRequestReloadIgnoringLocalCacheData;
        
        [self clearWebCache];
	}
	
	// Set the view controller as the window's root view controller and display.
	self.viewController = [[OmSlicViewController alloc] init];
    if( self->updateHeader!=OM_NULL ) {
        char * jsonUpdateHeader = om_update_header_to_json(self->storage,self->updateHeader);
        self.viewController.updateHeaderJSON=[NSString stringWithUTF8String:jsonUpdateHeader];
        om_free(jsonUpdateHeader);
    }
    
    self.viewController.cachePolicy = cachePolicy;
	self.viewController.appDelegate = self;
	self.viewController.view;
    self.window.rootViewController = self.viewController;
    
	[self.window makeKeyAndVisible];
	
	return YES;
}

- (void) restoreToWebView {
	NSLog(@"in OmSlicAppDelegate restoreToWebView");
	self.window.rootViewController = self.viewController;
	[self.window makeKeyAndVisible];
}

#pragma mark -
#pragma mark OpenMEAP Initialization

//~~~~~~~~~~~~~~~~~~~~~~~~~//
// STUFF I SHOULD MOVE OUT //
//~~~~~~~~~~~~~~~~~~~~~~~~~//

/*!
 * @return NO if an IMMEDIATE update failed, else YES
 */
- (BOOL) performUpdateCheck {
    
    if( self->updateHeader!=OM_NULL ) {
        om_update_release_update_header(self->updateHeader);
        self->updateHeader=OM_NULL;
    }
    
	if( om_update_decision(self.config)==OM_TRUE ) {
		
		NSLog(@"-- making update check");
		self->updateHeader = om_update_check(self->config);
        
        // TODO: if there is no network connectivity, then i need to communicate that with the client.
        
		if( self->updateHeader!=OM_NULL ) {
            
            if( self->updateHeader->type==OM_UPDATE_TYPE_IMMEDIATE ) {
                
                NSLog(@"-- performing update");
                const char * updateResult = om_update_perform(self->config,self->storage,self->updateHeader);
                
                if( updateResult!=OmUpdateResultSuccess ) {
                    
                    NSLog(@"-- performing failed");
                    om_update_release_update_header(self->updateHeader);
                    self.updateHeader=OM_NULL;
                    self.viewController.updateHeaderJSON=nil;
                    
                    return NO;
                } else {
                    
                    NSLog(@"-- performing succeeded");
                    om_update_release_update_header(self->updateHeader);
                    self.updateHeader=OM_NULL;
                    self.viewController.updateHeaderJSON=nil;
                    
                    [self reload];
                }
            } 
		} 
	}
    
    return YES;
}

/*!
 * Make sure that the OpenMEAP storage configuration is setup correctly.
 *
 * @return YES if everything was ok, else NO
 */
- (BOOL) initializeStorage {
	
	BOOL ret = YES;
	
	om_error_clear();
	
	char * locationRoot = om_storage_get_localstorage_path(self->storage);
	if( locationRoot==OM_NULL ) {
		return NO;
	}
	
	// make sure the default import archive location is set
	char * importArchLoc = om_config_get(self->config,OM_CFG_IMPORT_ARCHIVE_PATH);
	if( om_error_get_code()!=OM_ERR_NONE ) {
		om_free(locationRoot);
		return NO;
	}
    
	if( importArchLoc==OM_NULL ) {
		char * fullLoc = om_string_format("%s%c%s",locationRoot,OM_FS_FILE_SEP,OM_IMPORT_ARCHIVE_FILENAME);
		om_config_set(self->config,OM_CFG_IMPORT_ARCHIVE_PATH,fullLoc);
		om_free(fullLoc);
	}
    
	om_free(importArchLoc);
	om_free(locationRoot);
	
	return ret;
}

#pragma mark -
#pragma mark OpenMEAP Login form

- (void) doLoginFormForDelegate:(id <OmSlicLoginViewDelegate>)delegate 
	withAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge 
			 andProtectionSpace:(NSURLProtectionSpace *)protectionSpace 
					 fromThread:(NSThread *)thread {
	/*
	 call:
	 - (void) _doLoginFormForDelegate:(id)delegate 
	 withAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
	 andProtectionSpace:(NSURLProtectionSpace*)protectionSpace
	 fromThread:NSThread *thread
	 */
	SEL sel = @selector(_doLoginFormForDelegate:withAuthenticationChallenge:andProtectionSpace:fromThread:);
	NSMethodSignature * mySignature = [OmSlicAppDelegate instanceMethodSignatureForSelector:sel];
	NSInvocation * inv = [NSInvocation invocationWithMethodSignature:mySignature];
	[inv setTarget:self];
	[inv setSelector:sel];
	[inv setArgument:&delegate atIndex:2];
	[inv setArgument:&challenge atIndex:3];
	[inv setArgument:&protectionSpace atIndex:4]; 
	[inv setArgument:&thread atIndex:5];
	[inv performSelectorOnMainThread:@selector(invokeWithTarget:) 
						  withObject:self
					   waitUntilDone:NO];
}

- (void) _doLoginFormForDelegate:(id<OmSlicLoginViewDelegate>)delegate 
	withAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge 
			 andProtectionSpace:(NSURLProtectionSpace*)protectionSpace
					 fromThread:(NSThread *)thread {
	NSLog(@"in OmSlicAppDelegate doLoginFormForDelegate:withAuthenticationChallenge:fromThread");
	NSLog(@"calling delegate class is : %@",[delegate isKindOfClass:[OmSlicConnectionHandler class]] ? @"correct" : @"incorrect" );
	NSLog(@"calling challenge class is : %@",[challenge isKindOfClass:[NSURLAuthenticationChallenge class]] ? @"correct" : @"incorrect" );
	NSLog(@"calling thread class is : %@",[thread isKindOfClass:[NSThread class]] ? @"correct" : @"incorrect" );
	
	self.loginViewController = [[OmSlicLoginViewController alloc] initWithProtectionSpace:protectionSpace];
	self.loginViewController.view;
	
	// set the delegate, challenge and thread in the login view controller
	self.loginViewController.callingDelegate = delegate;
	self.loginViewController.callingThread = thread;
	self.loginViewController.authenticationChallenge = challenge;
	
	self.window.rootViewController = self.loginViewController;
	[self.window makeKeyAndVisible];
}

#pragma mark -
#pragma mark Session Credentials Management

- (void) addCredential:(NSURLCredential*)credential {
}

- (NSURLCredential*) getCredentialForProtectionSpace:(NSURLProtectionSpace*)protectionSpace {
    return nil;
}

@end
