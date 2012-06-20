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

#import <openmeap-slic-core.h>

#import "OmSlicViewController.h"

@implementation OmSlicViewController

@synthesize appDelegate;
@synthesize cachePolicy;
@synthesize updateHeaderJSON;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	self.cachePolicy = NSURLRequestUseProtocolCachePolicy;
	return [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	
    if( self.appDelegate==nil ) {
        self.appDelegate = [OmSlicAppDelegate globalInstance];
    }
	om_storage_ptr stg = (om_storage_ptr)(self.appDelegate.storage);
	char * baseUrlChars = om_storage_get_current_assets_prefix(stg);
	
	char * indexHtmlPath = om_string_format("%s%c%s",baseUrlChars,OM_FS_FILE_SEP,"index.html");
	NSURL * indexHtmlUrl = [NSURL fileURLWithPath:[NSString stringWithUTF8String:indexHtmlPath]];
    
	char * bundledStorage = om_storage_get_bundleresources_path(stg);
	char * jsApiPath = om_string_format("%s%c%s",bundledStorage,OM_FS_FILE_SEP,"openmeap-ios-api.js");
	char * strJsApi = om_storage_file_get_contents(stg,jsApiPath);
    if( strJsApi==OM_NULL ) {
        [[OmSlicAppDelegate globalInstance] showAlert:@"openmeap-ios-api.js is missing from the resource bundle." withTitle:@"Resource Error"];
    }
	om_free(jsApiPath);
	om_free(bundledStorage);
    
	// TODO: check for null return and handle appropriately
	
	[[NSHTTPCookieStorage sharedHTTPCookieStorage] setCookieAcceptPolicy:NSHTTPCookieAcceptPolicyAlways];
	[self executeJavascriptInMainThread:[NSString stringWithUTF8String:strJsApi]];
    [self setUpdateHeader:updateHeaderJSON];
    om_free(strJsApi);
	
	NSURLRequest * request = [NSURLRequest requestWithURL:indexHtmlUrl
		cachePolicy:self.cachePolicy
		timeoutInterval:10.0f];
	[((UIWebView*)self.view) loadRequest:request];
}

- (void)setUpdateHeader:(NSString*)headerJSON {
    NSLog(@"in OmSlicViewController::setUpdateHeader:headerJSON");
    self.updateHeaderJSON = headerJSON;
    const char * makeSureOpenMEAPExists="if(typeof OpenMEAP=='undefined') { OpenMEAP={data:{},config:{},persist:{cookie:{}}}; };OpenMEAP.updates.onInit();";
    if( self.updateHeaderJSON != nil ) {
        char * js = om_string_format("%s%s%s;OpenMEAP.updates.onInit();",makeSureOpenMEAPExists,"OpenMEAP.data.update=",[self.updateHeaderJSON UTF8String]);
        NSLog(@"--%@",[NSString stringWithUTF8String:js]);
        [self executeJavascriptInMainThread:[NSString stringWithUTF8String:js]];
        om_free(js);
    } else {
        char * js = om_string_format("%s%s",makeSureOpenMEAPExists,"OpenMEAP.data.update=null;OpenMEAP.updates.onInit();");
        NSLog(@"--%@",[NSString stringWithUTF8String:js]);
        [self executeJavascriptInMainThread:[NSString stringWithUTF8String:js]];
        om_free(js);
    }
}


// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
	// TODO: defer this decision to the client's javascript 
    return YES;//(interfaceOrientation == UIInterfaceOrientationPortrait);
}


- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	// Release any cached data, images, etc that aren't in use.
}


- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
}


- (void)dealloc {
    [super dealloc];
}

#pragma mark -
#pragma mark WebView interaction

-(NSString*) executeJSCallbackInMainThread:(NSString *)callbackJS withArguments:(NSArray*)args waitUntilDone:(Boolean)waitTil {
    int r = rand();
    NSString *argsStr = [args componentsJoinedByString:@","];
    char *str = om_string_format("var func%X = %s; func%X(%s); delete func%X;",r,[callbackJS UTF8String],r,[argsStr UTF8String],r);
    NSString * ret = [self executeJavascriptInMainThread:[NSString stringWithCString:str encoding:NSUTF8StringEncoding] waitUntilDone:waitTil];
    om_free(str);
    return ret;
}

-(NSString*) executeJavascriptInMainThread:(NSString *)javascript { 
    return [self executeJavascriptInMainThread:javascript waitUntilDone:NO];
}

-(NSString*) executeJavascriptInMainThread:(NSString *)javascript waitUntilDone:(Boolean)waitTil {
	
	SEL sel = @selector(stringByEvaluatingJavaScriptFromString:);
	
	NSMethodSignature * mySignature = [UIWebView instanceMethodSignatureForSelector:sel];
	NSInvocation * inv = [NSInvocation invocationWithMethodSignature:mySignature];
	UIWebView *view = self.view;
	
	NSString *msgCopy = [NSString stringWithString: javascript];
	[msgCopy retain];
	
	[inv setTarget:view];
	[inv setSelector:sel];
	[inv setArgument:&msgCopy atIndex:2];
	
	[inv performSelectorOnMainThread:@selector(invokeWithTarget:) withObject:view waitUntilDone:waitTil];
    
    if( waitTil==YES ) {
        // TODO: figure out how to get this to work, once i need it.
        //NSString **result;
        //[inv getReturnValue:result];
        //return *result;
        return nil;
    } else {
        return nil;
    }
}


@end
