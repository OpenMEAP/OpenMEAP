/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
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
#import "OmSlicAppDelegate.h"

@class OmSlicAppDelegate;

/**
 * Controller for the primary UIWebView
 */
@interface OmSlicViewController : UIViewController {
	
	/**
	 * Used to clear the app's webview cache after an update.
	 */
	int cachePolicy;
	
	/**
	 * Used for access to the OmSlicAppDelegate.
	 */
	OmSlicAppDelegate * appDelegate;
    
    NSString * updateHeaderJSON;
}

@property int cachePolicy;
@property OmSlicAppDelegate * appDelegate;
@property (retain,nonatomic) NSString * updateHeaderJSON;

- (void)setUpdateHeader:(om_update_header_ptr)header withError:(om_update_check_error_ptr)error;
- (void)clear;
- (void)setupWebView;
-(NSString*) executeJavascriptInMainThread:(NSString *)javascript;
-(NSString*) executeJavascriptInMainThread:(NSString *)javascript waitUntilDone:(Boolean)waitTil;
-(NSString*) executeJavascriptCallbackInMainThread:(NSString *)callbackJS withArguments:(NSArray*)args waitUntilDone:(Boolean)waitTil;

@end

