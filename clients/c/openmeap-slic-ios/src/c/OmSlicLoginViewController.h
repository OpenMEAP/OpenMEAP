/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

#import <openmeap-slic-core.h>
#import <UIKit/UIKit.h>

@protocol OmSlicLoginViewDelegate<NSObject>
@required
- (void) didReceiveURLCredentials:(NSURLCredential*)creds forChallenge:(NSURLAuthenticationChallenge *)challenge;
- (void) didReceiveCancelForChallenge:(NSURLAuthenticationChallenge*)challenge;
@end

/**
 * View controller to handle a specific NSURLAuthenticationChallenge.
 */
@interface OmSlicLoginViewController : UIViewController {
	IBOutlet UILabel *titleLbl;
	IBOutlet UITextField *usernameTxt;
	IBOutlet UITextField *passwordTxt;
	IBOutlet UIButton *doneBtn;
	IBOutlet UIButton *cancelBtn;
	IBOutlet UIActivityIndicatorView *loginIndicator;
    IBOutlet UISwitch *rememberSwitch;
	
	NSURLProtectionSpace * protectionSpace;
	NSURLAuthenticationChallenge * authenticationChallenge;
	NSThread *callingThread;
	id<OmSlicLoginViewDelegate> callingDelegate;
		
}

@property (nonatomic, retain) UILabel *titleLbl;
@property (nonatomic, retain) UITextField *usernameTxt;
@property (nonatomic, retain) UITextField *passwordTxt;
@property (nonatomic, retain) UIButton *doneBtn;
@property (nonatomic, retain) UIButton *cancelBtn;
@property (nonatomic, retain) UIActivityIndicatorView *loginIndicator;
@property (nonatomic, retain) UISwitch *rememberSwitch;

- (IBAction) done: (id) sender;
- (IBAction) cancel: (id) sender;

@property (retain, nonatomic) NSURLProtectionSpace * protectionSpace;
@property (retain, nonatomic) NSURLAuthenticationChallenge * authenticationChallenge;
@property (retain, nonatomic) NSThread *callingThread;
@property (retain, nonatomic) id<OmSlicLoginViewDelegate> callingDelegate;

- (OmSlicLoginViewController*)initWithProtectionSpace:(NSURLProtectionSpace*)protSpace;

@end