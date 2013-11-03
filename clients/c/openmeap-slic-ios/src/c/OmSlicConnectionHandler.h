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
#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>

enum OmSlicConnStatus {
	OmSlicConnStatusSysError = (-2), // if the connection object could not be created
	OmSlicConnStatusNetError = (-1), // if the connection was made, but the status code was 500 or 404, etc
	OmSlicConnStatusPending = 0,
	OmSlicConnStatusDone = 1
};

@interface OmSlicConnectionHandler : NSObject {	
	
	@private int currentStatus;
	
	@private NSURLRequest *request;
	@private NSOutputStream *outputStream;
	
	@private NSError *error;
	@private NSURLResponse *response;
	
	@private NSURLAuthenticationChallenge * authenticationChallenge;
	
	@private NSURLProtectionSpace *lastProtectionSpace;
	
    @private om_net_download_callback_func_ptr callBackFunc;
	@private void *callBackData;
    
    @private long bytesDownloaded;
}

@property int currentStatus;

@property (retain,nonatomic) NSURLRequest * request;
@property (retain,nonatomic) NSOutputStream * outputStream;

@property (retain,nonatomic) NSError * error;
@property (retain,nonatomic) NSURLResponse * response;

@property (retain,nonatomic) NSURLProtectionSpace *lastProtectionSpace;

@property om_net_download_callback_func_ptr callBackFunc;
@property void *callBackData;

@property long bytesDownloaded;

@end
