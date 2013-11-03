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

#import <Foundation/Foundation.h>
#import "OmSlicConnectionHandler.h"
#import "OmSlicAppDelegate.h"

@implementation OmSlicConnectionHandler

@synthesize currentStatus;

@synthesize request;
@synthesize outputStream;

@synthesize error;
@synthesize response;

@synthesize lastProtectionSpace;

@synthesize callBackData;
@synthesize callBackFunc;

@synthesize bytesDownloaded;

#pragma mark -
#pragma mark Initialization

+ (OmSlicConnectionHandler*)initWithRequest:(NSURLRequest *)urlRequest 
							outputStream:(NSOutputStream*)stream {
	OmSlicConnectionHandler *handler = [[OmSlicConnectionHandler alloc] init];
	handler.callBackFunc = nil;
	handler.callBackData = nil;
	handler.request = urlRequest;
	handler.outputStream = stream;
    handler.bytesDownloaded = 0;
	return handler;
}

- init {
	authenticationChallenge = nil;
	currentStatus = OmSlicConnStatusPending;
	lastProtectionSpace = nil;
	return self;
}

- (void) dealloc {
	[response release];
	[outputStream release];
	if( error!=nil ) {
		[error release];
	}
	if( response!=nil ) {
		[response release];
	}
	if( authenticationChallenge!=nil ) {
		[authenticationChallenge release];
	}
	if( lastProtectionSpace!=nil ) {
		[lastProtectionSpace release];
	}
}

#pragma mark -
#pragma mark Core methods of NSURLConnectionDelegate

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)thisResponse {
	NSLog(@"in OmSlicConnectionHandler connection: didReceiveResponse:");
	self.response = thisResponse;
}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)thisError {
	NSLog(@"in OmSlicConnectionHandler connection: didFailWithError:");
	NSLog(@"-- error was %@ code %d",[thisError domain],[thisError code]);
	self.error = thisError;
	currentStatus = OmSlicConnStatusNetError;
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
	NSLog(@"in OmSlicConnectionHandler connection: didReceiveData:");
	NSLog(@"-- writing %d bytes",[data length]);
	int written = [self.outputStream write:[data bytes] maxLength:[data length]];
	if( written == (-1) ) {
		NSError *thisError = [self.outputStream streamError];
		NSLog(@"-- error writing to stream: %@ code %@",[thisError domain],[thisError code]);
        self.error = thisError;
		self.currentStatus = OmSlicConnStatusSysError;
	}
    if( self.callBackFunc!=nil ) {
        self.bytesDownloaded+=[data length];
        self.callBackFunc(self.callBackData,
                          (om_http_response_ptr)OM_NULL,
                          [self.response expectedContentLength],
                          self.bytesDownloaded);
    }
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection {
	NSLog(@"in OmSlicConnectionHandler connectionDidFinishLoading:");
	[outputStream close];
	if( currentStatus==OmSlicConnStatusPending ) {
		currentStatus = OmSlicConnStatusDone;
	}
}

/*
 - (NSInputStream *)connection:(NSURLConnection *)connection needNewBodyStream:(NSURLRequest *)request {
 NSLog(@"in OmSlicConnectionHandler connection: needNewBodyStream:");
 }*/

#pragma mark -
#pragma mark Authentication Methods of NSURLConnectionDelegate

- (BOOL)connection:(NSURLConnection *)connection canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace {
	NSLog(@"in OmSlicConnectionHandler connection: canAuthenticateAgainstProtectionSpace:");
	NSLog(@"-- protection space %@",[protectionSpace authenticationMethod]);

	@synchronized(lastProtectionSpace) {
		if( lastProtectionSpace!=nil ) {
			[lastProtectionSpace release];
		}
		lastProtectionSpace = protectionSpace;
		[lastProtectionSpace retain];
	}
	
	NSString * authMethod = [protectionSpace authenticationMethod];
    
    if( [authMethod compare:NSURLAuthenticationMethodServerTrust]==NSOrderedSame 
            && [OmSlicAppDelegate isDevelopmentMode] ) {
        return YES;
    }
    
	if( [authMethod compare:NSURLAuthenticationMethodDefault]==NSOrderedSame 
	   || [authMethod compare:NSURLAuthenticationMethodHTTPBasic]==NSOrderedSame
	   || [authMethod compare:NSURLAuthenticationMethodHTTPDigest]==NSOrderedSame ) {
		return YES;
	} else {
		return NO;
	}
}

- (void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge {
	NSLog(@"in OmSlicConnectionHandler connection: didReceiveAuthenticationChallenge:");
	
	NSURLProtectionSpace * protectionSpace = challenge.protectionSpace;
    
    NSString * authMethod = [protectionSpace authenticationMethod];
    if( [authMethod compare:NSURLAuthenticationMethodServerTrust]==NSOrderedSame 
            && [OmSlicAppDelegate isDevelopmentMode]) {
        [challenge.sender useCredential:[NSURLCredential credentialForTrust:challenge.protectionSpace.serverTrust] 
             forAuthenticationChallenge:challenge];
        return;
    }
    
	NSThread * thread = [NSThread currentThread];
	[[OmSlicAppDelegate globalInstance] doLoginFormForDelegate:self 
			withAuthenticationChallenge:challenge 
			andProtectionSpace:protectionSpace 
			fromThread:thread];
	
	authenticationChallenge = challenge;
	
	[challenge retain];
}

/*- (void)connection:(NSURLConnection *)connection willSendRequestForAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge {
	NSLog(@"in OmSlicConnectionHandler connection: willSendRequestForAuthenticationChallenge:");
	OmSlicAppDelegate *appDel = [OmSlicAppDelegate globalInstance];
	[appDel performSelectorOnMainThread:@selector(restoreToWebView) withObject:nil waitUntilDone:NO];
}*/

#pragma mark -
#pragma mark Login View Controller Delegate Callback methods

- (void) didReceiveURLCredentials:(NSURLCredential*)creds forChallenge:(NSURLAuthenticationChallenge *)challenge {
	
	NSLog(@"in OmSlicConnectionHandler didReceiveURLCredentials:forChallenge:");
	NSLog(@"--challenge is of type:%@", [challenge isKindOfClass:[NSURLAuthenticationChallenge class]] ? @"correct" : @"incorrect" );
	NSLog(@"--creds is of type:%@", [creds isKindOfClass:[NSURLCredential class]] ? @"correct" : @"incorrect" );
	
	// if credentials, pass credentials back to creds.sender
	id<NSURLAuthenticationChallengeSender> sender = [challenge sender];
	[sender useCredential:creds forAuthenticationChallenge:challenge];
	[creds release];
	[challenge release];
}

- (void) didReceiveCancelForChallenge:(NSURLAuthenticationChallenge*)challenge {
	NSLog(@"in OmSlicConnectionHandler didReceiveCancelForChallenge:");
	// otherwise, end login
	[[challenge sender] cancelAuthenticationChallenge:challenge];
	[challenge release];
}

@end
