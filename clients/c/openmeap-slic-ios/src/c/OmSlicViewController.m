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

#import <openmeap-slic-core.h>

#import "OmSlicViewController.h"
#import "OmSlicJsApiProtocol.h"

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
	[self setupWebView];
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

- (void)setupWebView {
    
    if( self.appDelegate==nil ) {
        self.appDelegate = [OmSlicAppDelegate globalInstance];
    }
	om_storage_ptr stg = (om_storage_ptr)(self.appDelegate.storage);
	char * baseUrlChars = om_storage_get_current_assets_prefix(stg);
	
	char * indexHtmlPath = om_string_format("%s%c%s",baseUrlChars,OM_FS_FILE_SEP,"index.html");
	NSURL * indexHtmlUrl = [NSURL fileURLWithPath:[NSString stringWithUTF8String:indexHtmlPath]];
    NSLog(@"--index url %@",indexHtmlUrl); 
    
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
    om_free(strJsApi);
	
	NSURLRequest * request = [NSURLRequest requestWithURL:indexHtmlUrl
                                              cachePolicy:self.cachePolicy
                                          timeoutInterval:10.0f];
    
	[((UIWebView*)self.view) loadRequest:request];
    
    // kick off the update check in a different thread
    dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
	dispatch_async(queue, ^{ 
        NSLog(@"--about to check for update");
        @synchronized([OmSlicAppDelegate class]) {
            
            // wait for the api to notify that it has been loaded
            // but only 5 seconds...as there may be an IMMEDIATE update
            // pushed by the customer to fix the app.
            long count = 0;
            // TODO: make the timeout here configurable
            while( self.appDelegate.readyForUpdateCheck==FALSE && count<500 ) {
                [NSThread sleepForTimeInterval:.01];
                count++;
            }
            self.appDelegate.viewController=self;
            if( [self.appDelegate isTimeForUpdateCheck] ) {
                [self.appDelegate performUpdateCheck];
            }
        } 
    });
}


- (void)setUpdateHeader:(om_update_header_ptr)header withError:(om_update_check_error_ptr)error {
    
    NSLog(@"in OmSlicViewController::setUpdateHeader:header");
    
    char * js = OM_NULL;
    
    if( header != nil ) {
        
        char * jsonUpdateHeader = om_update_header_to_json(OmSlicAppDelegate.globalInstance.storage,header);
        self.updateHeaderJSON=[NSString stringWithUTF8String:jsonUpdateHeader];
        om_free(jsonUpdateHeader);
        js = om_string_format("if(OpenMEAP.updates.onUpdate!='undefined'){OpenMEAP.updates.onUpdate(%s);};",[self.updateHeaderJSON UTF8String]);
        
    } else if( error == nil ) {
        
        self.updateHeaderJSON=[NSString stringWithUTF8String:"null"];
        js = om_string_format("if(OpenMEAP.updates.onNoUpdate!='undefined'){OpenMEAP.updates.onNoUpdate();};");
        
    } else {
        
        self.updateHeaderJSON=[NSString stringWithUTF8String:"null"];
        js = om_string_format("if(OpenMEAP.updates.onCheckError!='undefined'){OpenMEAP.updates.onCheckError({code:\"%s\",message:\"%s\"});};",error->code,error->message);
    }
    
    if(js!=OM_NULL) {
        NSLog(@"--%@",[NSString stringWithUTF8String:js]);
        [self executeJavascriptInMainThread:[NSString stringWithUTF8String:js]];
    }
    om_free(js);
}

-(NSString*) executeJavascriptCallbackInMainThread:(NSString *)callbackJS withArguments:(NSArray*)args waitUntilDone:(Boolean)waitTil {
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
	
    NSLog(@"in executeJavascriptInMainThread");
    
	SEL sel = @selector(stringByEvaluatingJavaScriptFromString:);
	
	NSMethodSignature * mySignature = [UIWebView instanceMethodSignatureForSelector:sel];
	NSInvocation * inv = [NSInvocation invocationWithMethodSignature:mySignature];
	UIWebView *view = (UIWebView*)self.view;

	NSString *msgCopy = [NSString stringWithString: javascript];
	[msgCopy retain];
	
	[inv setTarget:view];
	[inv setSelector:sel];
	[inv setArgument:&msgCopy atIndex:2];
	
	[inv performSelectorOnMainThread:@selector(invokeWithTarget:) withObject:view waitUntilDone:waitTil];
    
    if( waitTil==YES ) {
        /*NSString *result;
        [inv getReturnValue:&result];
        if(result==nil) {
            NSLog(@"--execute javascript failed");
        } else {
            NSLog(@"--returned %@",result);
        }*/
        return nil;
    } else {
        return nil;
    }
}

- (void)clear {
    //[(UIWebView*)self.view loadHTMLString:@"" baseURL:[NSURL URLWithString:@"http://nowhere.com"]];
    //[self setView:nil];
}

@end
