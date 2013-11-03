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
#import "unit-tests.h"

@implementation update_test

- (void) testOmUpdateParseConnresp {
	
	char * testJson_noUpdate = "{\"connectionOpenResponse\":{\"authToken\":\"${AUTH_TOKEN}\"}}";
	
	char * testJson_update = "{\"connectionOpenResponse\":"\
        "{\"update\":"\
        "{\"storageNeeds\":123,\"updateUrl\":\"${UPDATE_URL}\","\
            "\"hash\":{\"value\":\"${HASH}\",\"algorithm\":\"MD5\"},"\
            "\"versionIdentifier\":\"${VERSION_ID}\","\
            "\"type\":\"REQUIRED\","\
            "\"installNeeds\":213},"\
            "\"authToken\":\"${AUTH_TOKEN}\"}}";
	
	om_update_connresp_ptr result = om_update_parse_connresp(testJson_noUpdate);
	om_STAssert( result->auth_token!=NULL && \
				strcmp(result->auth_token,"${AUTH_TOKEN}")==0, \
		@"The result->auth_token should be \"${AUTH_TOKEN}\".");
	om_STAssert( result->update==OM_NULL, \
		@"The result->update should be OM_NULL.");
	om_update_release_connresp(result);
	
	result = om_update_parse_connresp(testJson_update);
	om_STAssert( result->auth_token!=NULL && \
				strcmp(result->auth_token,"${AUTH_TOKEN}")==0, \
				@"The result->auth_token should be \"${AUTH_TOKEN}\"." );
	om_STAssert( result->update!=OM_NULL, \
				@"The result->update should NOT be OM_NULL.");
	om_STAssert( strcmp(result->update->update_url,"${UPDATE_URL}")==0, \
				@"The update_url should have been \"${UPDATE_URL}\"" );
	om_STAssert( strcmp(result->update->version_id,"${VERSION_ID}")==0, \
				@"The version_id should have been \"${VERSION_ID}\"" );
	om_STAssert( result->update->storage_needs==123,
				@"storage_needs should have been 123." );
	om_STAssert( result->update->install_needs==213,
				@"install_needs should have been 213." );
	om_STAssert( result->update->type==OM_UPDATE_TYPE_REQUIRED,
				@"type should have been OM_UPDATE_TYPE_REQUIRED" );
	om_STAssert( result->update->hash->hash_type==OM_HASH_MD5,
				@"hash_type should have been OM_HASH_MD5" );
	om_STAssert( strcmp(result->update->hash->hash,"${HASH}")==0,
				@"hash should have been \"${HASH}\"" );
			
	om_update_release_connresp(result);

}

@end
