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

#import <Foundation/Foundation.h>
#import "OmSlicJsApiProtocol.h"
#import "OmSlicAppDelegate.h"

#pragma mark -
#pragma mark Call-back functions for the C Core API

void ios_update_callback_func(om_update_callback_info_ptr callbackInfo) {
    
    OmSlicAppDelegate *appDel = [OmSlicAppDelegate globalInstance];
    OmSlicViewController *viewController = appDel.viewController;
    
    char * updateStatusJson = om_update_status_to_json(appDel.storage,callbackInfo->update_status);
    NSString *js = [NSString stringWithUTF8String:callbackInfo->javascript];
    NSArray *jsArgs = [NSArray arrayWithObject:[NSString stringWithUTF8String:updateStatusJson]];
    
    [viewController executeJavascriptCallbackInMainThread:js
                                    withArguments:jsArgs
                                    waitUntilDone:YES];
    
    om_free(updateStatusJson);
}

void ios_update_callback_net_download_callback_func_ptr(void *callback_info, 
                                                        om_http_response_ptr response, 
                                                        om_uint32 bytes_total, 
                                                        om_uint32 bytes_downloaded) {
    
    om_update_callback_info_ptr callbackInfo = (om_update_callback_info_ptr)callback_info;
    callbackInfo->update_status->bytes_downloaded = bytes_downloaded;
    ios_update_callback_func(callbackInfo);
}

#pragma mark -
#pragma mark iOS Implementation of the JS-API

@implementation OmSlicJsApiProtocol

+ (BOOL)canInitWithRequest:(NSURLRequest *)request {
	NSString *requestString = [[request URL] absoluteString];
	
	// Intercept custom location change, URL begins with "jsapi:"
	if ([requestString hasPrefix:@"jsapi:"]) {
		return YES;
	} else {
		return NO;
	}
}

+ (NSURLRequest *) canonicalRequestForRequest:(NSURLRequest *)request {
	return nil;
}

- (void)startLoading {
	
	NSLog(@"in OmSlicJsApiProtocol::startLoading");
    
    id urlClient = self.client;
	NSURLRequest * request = self.request;
    NSURL * url = [request URL];
	NSString *host = [url host];
	NSArray *parts = [url pathComponents];
    if( [parts count]<2 ) {
        [urlClient URLProtocol:self didFailWithError:nil]; 
        return;
    }
    
    // split the query string into key/value
    char *queryString = [[url query] UTF8String];
    om_dict_ptr queryParams = OM_NULL;
    if( queryString!=nil ) {
        queryParams = om_dict_from_query_string(queryString);
    } else {
        queryParams = om_dict_new(15);
    }
    
    om_list_ptr queryKeys = om_dict_get_keys(queryParams);
    int c = om_list_count(queryKeys);
    for( int i=0; i<c; i++ ) {
        char *key = om_list_get(queryKeys,i);
        char *val = om_dict_get(queryParams,key);
        NSLog(@"--key:%@, val:%@",[NSString stringWithUTF8String:key],[NSString stringWithUTF8String:val]);
    }
    om_list_release(queryKeys);
    
	// derive method name and arguments from path parts
	NSString *methodHandler = host;
	NSString *methodName = (NSString*)[parts objectAtIndex:1];
    
    OmSlicAppDelegate *appDel = [OmSlicAppDelegate globalInstance];
	
	NSLog(@"--starting to load data for %@::%@", methodHandler, methodName);

    NSString *result = @"null";
    
	if( [parts count]>1 ) {
        
		[parts objectAtIndex:0];
        
        if( [methodHandler compare:@"application"]==NSOrderedSame ) {
            
            if( [methodName compare:@"reload"]==NSOrderedSame ) {
                [appDel reloadView];
                
            } else if( [methodName compare:@"clearCache"]==NSOrderedSame ) {
                [appDel clearWebCache];
                
            } else if( [methodName compare:@"getOrientation"]==NSOrderedSame ) {
                UIInterfaceOrientation orientation = [[UIDevice currentDevice] orientation];
                if( orientation == UIInterfaceOrientationPortrait 
                   || orientation == UIInterfaceOrientationPortraitUpsideDown ) {
                    result=@"\"PORTRAIT\"";
                } else if( orientation == UIInterfaceOrientationLandscapeLeft
                          || orientation == UIInterfaceOrientationLandscapeRight ) {
                    result=@"\"LANDSCAPE\"";
                } else {
                    result=@"\"UNDEFINED\"";
                }
            } else if( [methodName compare:@"notifyReadyForUpdateCheck"]==NSOrderedSame ) {
                NSLog(@"--flipping OmSlicAppDelegate.readyForUpdateCheck to TRUE");
                appDel.readyForUpdateCheck = TRUE;
            }
        }

        else if( [methodHandler compare:@"updates"]==NSOrderedSame ) {
            
            if( [methodName compare:@"isTimeForCheck"]==NSOrderedSame ) {
                if( [appDel isTimeForUpdateCheck] ) {
                    result = @"true";
                } else {
                    result = @"false";
                }
            }
            
            else if( [methodName compare:@"checkForUpdates"]==NSOrderedSame ) {
                
                [appDel performUpdateCheck];
            }
            
            else if( [methodName compare:@"performUpdate"]==NSOrderedSame ) {
                
                char * decodedCallback = om_string_decodeURI(om_dict_get(queryParams,"callback"));
                const char * header = om_dict_get(queryParams,"header");
                char * decodedHeader = om_string_decodeURI(header);

                om_update_header_ptr updateHeader = om_update_header_from_json(decodedHeader);
                NSString *callBack = [NSString stringWithUTF8String:decodedCallback];
                om_free(decodedCallback);
                om_free(decodedHeader);
                
                // in another thread, call 
                dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
                dispatch_async(queue, ^{ 
                    @synchronized([OmSlicJsApiProtocol class]) {
                        [self performUpdate:updateHeader withCallback:callBack];
                    } 
                });
            }
        }
        
        else if( [methodHandler compare:@"preferences"]==NSOrderedSame ) {
            
            char * name = om_dict_get(queryParams, "name");
            char * key = om_dict_get(queryParams, "key");
            char * value = om_dict_get(queryParams, "value");
            
            if( [methodName compare:@"get"]==NSOrderedSame ) {
                // "jsapi://preferences/get/?name="+encodeURIComponent(name)+"&key="+encodeURIComponent(key)
                
                if( name==OM_NULL || key==OM_NULL ) {
                    result = @"null";
                } else {
                    char *decodedName = om_string_decodeURI(name);
                    om_prefs_ptr prefs = om_prefs_acquire(decodedName);
                    om_free(decodedName);
                    
                    if( prefs!=OM_NULL ) {
                        
                        char *decodedKey = om_string_decodeURI(key);
                        
                        char *prefValue = om_prefs_get(prefs,decodedKey);
                        if( prefValue==OM_NULL ) {
                            result = @"null";
                        } else {
                            char *encodedValue = om_string_encodeURI(prefValue);
                            result = [NSString stringWithFormat:@"\"%@\"",[NSString stringWithUTF8String:encodedValue]];
                            om_free(prefValue);
                            om_free(encodedValue);
                        }
                        
                        om_free(decodedKey);                        
                        om_prefs_release(prefs);
                    } else {
                        result = @"null";
                    }
                }
                
            } else if ( [methodName compare:@"put"]==NSOrderedSame ) {
                // "jsapi://preferences/put/?name="+encodeURIComponent(name)+"&key="+encodeURIComponent(key)+"&value="+encodeURIComponent(jsonValue)
                
                if( name==OM_NULL || key==OM_NULL || value==OM_NULL ) {
                    result = @"null";
                } else {
                    char * decodedName = om_string_decodeURI(name);
                    char * decodedKey = om_string_decodeURI(key);
                    char * decodedValue = om_string_decodeURI(value);
                    
                    om_prefs_ptr prefs = om_prefs_acquire(decodedName);
                    if( prefs!=OM_NULL ) {
                        om_prefs_set(prefs,decodedKey,decodedValue);
                        om_prefs_release(prefs);
                        result = @"true";
                    } else {
                        result = @"null";
                    }
                    
                    om_free(decodedName);
                    om_free(decodedKey);
                    om_free(decodedValue);
                }
                
            } else if ( [methodName compare:@"remove"]==NSOrderedSame ) {
                // "jsapi://preferences/remove/?name="+name+"&key="+key
                
                if( name==OM_NULL || key==OM_NULL ) {
                    result = @"null";
                } else {
                    char * decodedName = om_string_decodeURI(name);
                    char * decodedKey = om_string_decodeURI(key);
                    
                    om_prefs_ptr prefs = om_prefs_acquire(decodedName);
                    if( prefs!=OM_NULL ) {
                        om_prefs_remove(prefs,decodedKey);
                        om_prefs_release(prefs);
                        result = @"true";
                    } else {
                        result = @"null";
                    }
                    
                    om_free(decodedName);
                    om_free(decodedKey);
                }
                
            } else if ( [methodName compare:@"clear"]==NSOrderedSame ) {
                // "jsapi://preferences/clear/?name="+name
                
                if( name==OM_NULL || key==OM_NULL || value==OM_NULL ) {
                    result = @"null";
                } else {
                    char * decodedName = om_string_decodeURI(name);
                    
                    om_prefs_ptr prefs = om_prefs_acquire(decodedName);
                    if( prefs!=OM_NULL ) {
                        om_prefs_clear(prefs);
                        om_prefs_release(prefs);
                        result = @"true";
                    } else {
                        result = @"null";
                    }
                    
                    om_free(decodedName);
                }
                
            }
        }
	}
    
    NSLog(@"--loading \"%@\"",result);
    {
        NSData *data = [result dataUsingEncoding:NSUTF8StringEncoding];
        NSURLResponse *response = [[NSURLResponse alloc] 
                                    initWithURL:url 
                                    MIMEType:@"application/javascript" 
                                    expectedContentLength:[data length] 
                                    textEncodingName:@"utf-8"];
        [urlClient URLProtocol:self 
                    didReceiveResponse:response 
                    cacheStoragePolicy:NSURLCacheStorageNotAllowed];
        NSLog(@"--calling client::didLoadData");
        [urlClient URLProtocol:self didLoadData:data];
        NSLog(@"--calling client::URLProtocolDidFinishLoading:");
        [urlClient URLProtocolDidFinishLoading:self];
        NSLog(@"--calling release on the response");
        [response release];
    }
    
    om_dict_release(queryParams);
    
    NSLog(@"leaving OmSlicJsApiProtocol::startLoading");
}

- (void)stopLoading {
	NSLog(@"in OmSlicJsApiProtocol::stopLoading");
}

#pragma mark -
#pragma mark JS-API Implementation methods

- (NSString *) performUpdate:(om_update_header_ptr)header withCallback:(NSString *)callback {
    
    NSLog(@"in OmSlicJsApiProtocol::performUpdate");
    
    OmSlicAppDelegate * appDel = [OmSlicAppDelegate globalInstance];
    
    om_update_status_ptr status = om_update_status_new();
    
    om_update_callback_info_ptr callback_info = om_malloc(sizeof(om_update_callback_info_type));
    callback_info->update_status = status;
    callback_info->net_download_callback_data = callback_info;
    callback_info->net_download_callback_func_ptr = ios_update_callback_net_download_callback_func_ptr;
    callback_info->javascript=om_string_copy([callback UTF8String]);
    
    status->update_header = header;
    
    NSLog(@"--calling om_update_perform_with_callback");
    const char * update_result = om_update_perform_with_callback(appDel.config, appDel.storage, header, ios_update_callback_func, callback_info);
    
    om_update_status_release(status);
    om_free(callback_info->javascript);
    om_free(callback_info);
    return nil;
}

@end
