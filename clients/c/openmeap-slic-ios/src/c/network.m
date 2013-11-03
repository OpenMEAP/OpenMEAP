/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

#import <openmeap-slic-core.h>
#import "OmSlicConnectionHandler.h"
#import "OmSlicAppDelegate.h"

/**
 * Our own synchronous download method, that can still handle authentication requests.
 *
 * schedule the asynchronous operation to run in a new thread
 * so that it will use that new threads run loop
 * then loop in this thread till the operation has completed
 */
OM_PRIVATE_FUNC int __download(OmSlicConnectionHandler *connHandler) {
	NSURLConnection * conn = nil;
	dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
	dispatch_async(queue, ^{
		@try {
			NSLog(@"-- current thread runloop mode %@",[[NSRunLoop currentRunLoop] currentMode]);
			NSURLConnection * conn = [NSURLConnection connectionWithRequest:connHandler.request delegate:connHandler];
			if( conn == nil ) {
				NSLog(@"-- could not create connection");
				connHandler.currentStatus = OmSlicConnStatusSysError;
			}
			[[NSRunLoop currentRunLoop] run];
		} @catch( NSException *e ) {
			connHandler.currentStatus = OmSlicConnStatusSysError;
		}
	});
	// because we just wait here while the download goes,
	// we can call the callback here, rather than 
	while( connHandler.currentStatus == OmSlicConnStatusPending ) {
		[NSThread sleepForTimeInterval:(NSTimeInterval)0.01];
	}
	/*if( [connHandler.outputStream propertyForKey:NSStreamDataWrittenToMemoryStreamKey] == nil ) {
		NSLog(@"NSStreamDataWrittenToMemoryStreamKey returned null");
		om_error_set(OM_ERR_NET_CONN,"could not create NSURLConnection");
		return OmSlicConnStatusSysError;
	}*/
	return connHandler.currentStatus;
}

OM_EXPORT om_http_response_ptr om_net_do_http_get_to_file_output_stream(const char *url, 
                                                                        om_file_output_stream_ptr ostream, 
                                                                        om_net_download_callback_func_ptr callback_func, 
                                                                        void *callback_data)
{
	
	NSURL *nsUrl = [NSURL URLWithString:[NSString stringWithUTF8String:url]];
	
	NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:nsUrl];
	[request setHTTPMethod:@"GET"];
	[request setCachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData];
	
	NSOutputStream * stream = [NSOutputStream 
							   outputStreamToFileAtPath:[NSString stringWithUTF8String:ostream->device_path] 
							   append:NO];
	[stream open];
	[stream retain];
	OmSlicConnectionHandler *connHandler = [OmSlicConnectionHandler initWithRequest:request outputStream:stream];
    if( callback_func!=OM_NULL ) {
        connHandler.callBackFunc = callback_func;
        connHandler.callBackData = callback_data;
    }
	if( __download(connHandler) < OmSlicConnStatusPending ) {
		return OM_NULL;
	}
	[stream release];
	NSError *error = connHandler.error;
	NSURLResponse *response = connHandler.response;
	
	if(!error) {		
		// at this point, everything has passed
		// so we can allocate the response struct
		// and fill it with data
		om_http_response_ptr resp = om_malloc(sizeof(om_http_response));
		resp->status_code = [response statusCode];
		resp->result = om_string_copy(ostream->device_path);
		
		return resp;
	} 
	// connection failed
	else {

		om_error_set(OM_ERR_NET_CONN,[[error domain] UTF8String]);
		return OM_NULL;
	}
}

om_http_response_ptr om_net_do_http_post(const char *url, const char *post_data) {
	
	NSString *post = post_data!=OM_NULL ? [NSString stringWithUTF8String:post_data] : @"";
    NSLog(@"Intending to post %@",post);
	NSURL *nsUrl=[NSURL URLWithString:[NSString stringWithUTF8String:url]];
    NSLog(@"to url %@",[nsUrl absoluteString]);

	NSData *postData = [post dataUsingEncoding:NSUTF8StringEncoding allowLossyConversion:YES];
	NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:nsUrl];
	[request setHTTPMethod:@"POST"];
	[request setHTTPBody:postData];
    [request setCachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData];
	
	NSOutputStream *outputStream = [NSOutputStream outputStreamToMemory];
	[outputStream open];
	OmSlicConnectionHandler *connHandler = [OmSlicConnectionHandler initWithRequest:request outputStream:outputStream];
	if( __download(connHandler) < 0 ) {
        NSError *error = connHandler.error;
        if(error!=OM_NULL) {
            
            om_error_set(OM_ERR_NET_CONN,[[error domain] UTF8String]);
            return OM_NULL;
        }
		return OM_NULL;
	}
    
	NSHTTPURLResponse *response = connHandler.response;	
	NSData *urlData=[outputStream propertyForKey:NSStreamDataWrittenToMemoryStreamKey];
	NSString *data=[[NSString alloc]initWithData:urlData encoding:NSUTF8StringEncoding];
	
	// now that the request has gone through
	// attempt to allocate a response struct
	om_http_response_ptr resp = om_malloc(sizeof(om_http_response));
	if( resp==NULL ) {
		//[[OmSlicAppDelegate globalInstance] showAlert:@"Failed to allocate response object" withTitle:@"Memory Error"];
		om_error_set(OM_ERR_MALLOC,"malloc() failed for om_http_response");
		return OM_NULL;
	}
	
	// attempt to allocate a result string and 
	// copy the urlData into it
	resp->result = om_malloc([data length]+1);
	if( resp->result == NULL ) {
		//[[OmSlicAppDelegate globalInstance] showAlert:@"Failed to allocation data buffer" withTitle:@"Memory Error"];
		om_error_set(OM_ERR_MALLOC,"malloc() failed for om_http_response.result");
		return OM_NULL;
	}
	resp->status_code = [response statusCode];
	memcpy(resp->result,[data UTF8String],[data length]+1);
	
	return resp;
}
