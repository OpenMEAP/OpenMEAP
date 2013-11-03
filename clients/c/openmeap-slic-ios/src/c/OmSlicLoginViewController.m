/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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
#import "OmSlicLoginViewController.h"
#import "OmSlicAppDelegate.h"
#import "OmSlicConnectionHandler.h"

@implementation OmSlicLoginViewController

@synthesize titleLbl;
@synthesize usernameTxt;
@synthesize passwordTxt;
@synthesize doneBtn;
@synthesize cancelBtn;
@synthesize loginIndicator;
@synthesize rememberSwitch;

@synthesize protectionSpace;
@synthesize authenticationChallenge;
@synthesize callingThread;
@synthesize callingDelegate;

- (OmSlicLoginViewController*)initWithProtectionSpace:(NSURLProtectionSpace*)protSpace {
	self.protectionSpace = protSpace;
	return [super init];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    NSString *titleFormat = @"%@:%i\n%@";
	titleLbl.text = [NSString stringWithFormat:titleFormat,
                     protectionSpace.host,
                     protectionSpace.port,
                     protectionSpace.realm!=nil ? protectionSpace.realm : @"default" ];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning]; // Releases the view if it doesn't have a superview
    // Release anything that's not essential, such as cached data
}

- (void)dealloc {
	[protectionSpace release];
	[authenticationChallenge release];
	[callingThread release];
	[callingDelegate release];
    [super dealloc];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
	if( textField==self.usernameTxt ) {
		[self.passwordTxt becomeFirstResponder];
		return NO;
	} else if( textField==self.passwordTxt ) {
		[textField resignFirstResponder];
		[self done:nil];
		return NO;
	}
	return YES;
}

/**
 * NOTE: should only ever be called on the main thread
 */
- (IBAction) done: (id) sender
{
	NSLog(@"in OmSlicLoginViewController done:");
	
	NSURLCredential *creds = [NSURLCredential 
							  credentialWithUser:[usernameTxt text] 
							  password:[passwordTxt text] 
							  persistence:NSURLCredentialPersistenceForSession];
	[creds retain];
	
	// call 
	// - (void) didReceiveURLCredentials:(NSURLCredential *)creds forChallenge:challenge{
	// on the delegate and thread passed in	
	SEL sel = @selector(didReceiveURLCredentials:forChallenge:);
	
	NSMethodSignature * mySignature = [[callingDelegate class] instanceMethodSignatureForSelector:sel];
	
	NSInvocation * inv = [NSInvocation invocationWithMethodSignature:mySignature];
	[inv setTarget:callingDelegate];
	[inv setSelector:sel];
	[inv setArgument:&creds atIndex:2];
	[inv setArgument:&authenticationChallenge atIndex:3];
	
	[inv performSelector:@selector(invokeWithTarget:) 
			onThread:callingThread
			withObject:callingDelegate
		    waitUntilDone:NO];
	
	doneBtn.enabled = FALSE;
	cancelBtn.enabled = FALSE;
	loginIndicator.hidden = FALSE;
	[loginIndicator startAnimating];
}

/**
 * NOTE: should only ever be called on the main thread
 */
- (IBAction) cancel: (id) sender
{
	NSLog(@"in OmSlicLoginViewController cancel:");
	
	// call 
	// - (void) didReceiveCancelForChallenge:(NSURLAuthenticationChallenge*)challenge {
	// on the delegate and thread passed in	
	SEL sel = @selector(didReceiveCancelForChallenge:);
	
	NSMethodSignature * mySignature = [[callingDelegate class] instanceMethodSignatureForSelector:sel];
	
	NSInvocation * inv = [NSInvocation invocationWithMethodSignature:mySignature];
	[inv setTarget:callingDelegate];
	[inv setSelector:sel];
	[inv setArgument:&authenticationChallenge atIndex:2];
	
	[inv performSelector:@selector(invokeWithTarget:) 
				onThread:callingThread
				withObject:callingDelegate
				waitUntilDone:NO];
	
	doneBtn.enabled = FALSE;
	cancelBtn.enabled = FALSE;
	loginIndicator.hidden = TRUE;
	[loginIndicator stopAnimating];
}

/**
 * NOTE: should only ever be called on the main thread
 */
- (void) loginDidFail {
	NSLog(@"in OmSlicLoginViewController loginDidFail");
	doneBtn.enabled = TRUE;
	cancelBtn.enabled = TRUE;
	loginIndicator.hidden = TRUE;
	[loginIndicator stopAnimating];
}

@end
