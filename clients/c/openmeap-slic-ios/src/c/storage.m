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

#include <stdio.h>
#include <stdlib.h>
#include <memory.h>

#include <openmeap-slic-core.h>

///////////////////////////
// THE FOUR FUNCTIONS THAT AREN'T IN SLIC CORE

char * ios_om_storage_get_localstorage_path(om_storage_ptr storage) {
	NSArray *savePaths = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES);
	NSMutableString *savePath = [NSMutableString stringWithString:[savePaths objectAtIndex:0]];
	return om_string_copy([savePath UTF8String]);
}

char * ios_om_storage_get_bundleresources_path(om_storage_ptr storage) {
	NSString *resourcePath = [[NSBundle mainBundle] resourcePath];
	return om_string_copy([resourcePath UTF8String]);
}

/**
 * Returns the current prefix to search for bundled assets in.
 */
char * ios_om_storage_get_current_assets_prefix(om_storage_ptr storage) {
	
	char * currentLocation = om_storage_get_current_location(storage);
	
	// if get_current_location returns OM_NULL,
	// then we haven't downloaded an update yet
	// and so the assets prefix is whatever was bundled with the app
	char * applicationPath = OM_NULL;
	char * fileFullPath = OM_NULL;
	if( currentLocation == OM_NULL ) {
		char * assetsRoot = storage->get_bundleresources_path(storage);
		char * assetsPathSuffix = om_config_get(storage->cfg,OM_CFG_APP_PKG_ROOT);
		if( assetsRoot==OM_NULL || assetsPathSuffix==OM_NULL ) {
			om_free(assetsRoot);
			om_free(assetsPathSuffix);
			return OM_NULL;
		}
		fileFullPath = om_string_format("%s%c%s",assetsRoot,OM_FS_FILE_SEP,assetsPathSuffix);
		if( fileFullPath==OM_NULL ) {
			om_free(assetsRoot);
			om_free(assetsPathSuffix);
			return OM_NULL;
		}
	} else {
        fileFullPath = currentLocation;
    }
	
	return fileFullPath;
}

/**
 * @return The number of bytes left for storage
 */
om_uint64 ios_om_storage_get_bytes_free(om_storage_ptr storage) {
	
	om_uint64 totalSpace = 0;  
	NSError *error = nil;  
	char * path = storage->get_localstorage_path(storage);
	if( path==OM_NULL )
		return OM_NULL;
	NSString * nsPath = [NSString stringWithUTF8String:path];
	NSDictionary *dictionary = [[NSFileManager defaultManager] attributesOfFileSystemForPath:nsPath error: &error];  
	
	if (dictionary) {  
		NSNumber *fileSystemSizeInBytes = [dictionary objectForKey: NSFileSystemFreeSize];  
		totalSpace = [fileSystemSizeInBytes longLongValue];  
	} else {  
		om_error_set(OM_ERR_FILESYS_SPACE_AVAIL,[[error domain] UTF8String]);
		return 0;
	}  
	
	return totalSpace;  
}

///////////////////////////
// INITIALIZATION IMPLEMENTATION

om_storage_ptr om_storage_alloc(om_config_ptr *cfg) {
	om_storage_ptr ptr = default_om_storage_alloc(cfg);
	ptr->get_localstorage_path=ios_om_storage_get_localstorage_path;
	ptr->get_bundleresources_path=ios_om_storage_get_bundleresources_path;
	ptr->get_bytes_free=ios_om_storage_get_bytes_free;
	ptr->get_current_assets_prefix=ios_om_storage_get_current_assets_prefix;
	return ptr;
}

void om_storage_release(om_storage_ptr storage) {
	om_free(storage);
}
