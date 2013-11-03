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
#import "OmSlicViewController.h"
#import "OmSlicLoginViewController.h"

@class OmSlicViewController;

@interface OmSlicAppDelegate : NSObject <UIApplicationDelegate> {
	
    UIWindow *window;
    OmSlicViewController *viewController;
	OmSlicLoginViewController *loginViewController;

	om_config_ptr config;
	om_storage_ptr storage;
    
    BOOL readyForUpdateCheck;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet OmSlicViewController *viewController;
@property (nonatomic, retain) IBOutlet OmSlicLoginViewController *loginViewController;

@property (nonatomic) om_storage_ptr storage;
@property (nonatomic) om_config_ptr config;

@property (nonatomic) BOOL readyForUpdateCheck;

+ (OmSlicAppDelegate*)globalInstance;
- (void) showAlert:(NSString*)message withTitle:(NSString*)title;
- (void) reloadView;
- (void) clearWebCache;
- (BOOL) isTimeForUpdateCheck;
- (BOOL) performUpdateCheck;

@end

