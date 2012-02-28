/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2012 OpenMEAP, Inc.                                   #
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

#include <openmeap-slic-core.h>

#include "unit-tests.h"
#include "mock/network-mock.h"

om_update_header_ptr om_update_header_ptr_response;
om_update_header_ptr om_update_parse_connreq_resp(char *resp) {
	return om_update_header_ptr_response;
}

const char *test_response = "{\"connectionOpenResponse\":{\"update\":{\"storageNeeds\":809126,\"updateUrl\":\"http://dev.openmeap.com:8081/openmeap-services-web/application-management/?action=archiveDownload&app-name=Application%20Name&auth=auth_token&app-version=cof-banking-v1\",\"hash\":{\"value\":\"99eabf34466dbb2e11d6c0550cbaccfb\",\"algorithm\":\"MD5\"},\"versionIdentifier\":\"version-2.0.3\",\"type\":\"IMMEDIATE\",\"installNeeds\":1157102},\"authToken\":\"auth_token\"}}";

// defined in config.c, but not explicitly in the config.h
const char * om_config_map_to_str(int om_config_enum);

START_TEST(test_update_decision){
	om_prefs_ptr prefs = om_prefs_acquire("new prefs");
	om_props_ptr props = om_props_acquire("new props");
	
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_DEV_TYPE),"device_type");
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_DEV_UUID),"device_uuid");
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_APP_NAME),"app_name");
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_APP_VER),"app_version");
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_SLIC_VER),"slic_version");
	
	om_config_ptr cfg = om_config_obtain(prefs,props);
	
	// create a connreq object from the configuration passed in
	om_update_connreq_ptr connreq = om_update_create_connreq(cfg);
	ASSERT(strcmp("device_type",connreq->device_type)==0,"device_type should have been 'device_type'");
	ASSERT(strcmp("device_uuid",connreq->device_uuid)==0,"device_uuid should have been 'device_uuid'");
	ASSERT(strcmp("app_name",connreq->app_name)==0,"app_name should have been 'app_name'");
	ASSERT(strcmp("app_version",connreq->app_version)==0,"app_version should have been 'app_version'");
	ASSERT(strcmp("slic_version",connreq->slic_version)==0,"slic_version should have been 'slic_version'");
	char * str = om_update_connreq_create_post(connreq);
	ASSERT(strcmp("action=connection-open-request&device-type=device_type&device-uuid=device_uuid&"\
                  "app-name=app_name&app-version=app_version&hash=&slic-version=slic_version", \
				  str)==0, "the post data created for the connection request generated was not correct");
	om_free(str);
	om_update_release_connreq(connreq);
	
	// use the mock method to create a connresp struct
	// so that we can verify releasing it with the ASSERT_FREE
	om_update_connresp_ptr om_update_parse_connresp_result = om_update_parse_connresp(test_response);
	om_update_release_connresp(om_update_parse_connresp_result);

	// validate the om_update_decision function
	char tmp[200];
	sprintf(tmp,"%u",om_time(0)-16);
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_UPDATE_LAST_ATTEMPT),tmp);
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_UPDATE_FREQ),"15");
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_UPDATE_SHOULD_PULL),"1");
	ASSERT(om_update_decision(cfg),"we should need to pull an update now");
	
	// assert that we won't needlessly pull updates
	sprintf(tmp,"%u",om_time(0)-10);
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_UPDATE_LAST_ATTEMPT),tmp);
	ASSERT(!om_update_decision(cfg),"we should not need to pull an update now");
	
	// assert that we won't say we need an update if the customer doesn't want them
	sprintf(tmp,"%u",om_time(0)-16);
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_UPDATE_LAST_ATTEMPT),tmp);
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_UPDATE_SHOULD_PULL),"0");
	ASSERT(!om_update_decision(cfg),"we should not need to pull an update now");
	
	ASSERT( om_uint32_update_parse_helper(OM_UPDATE_TYPE,"required") == OM_UPDATE_TYPE_REQUIRED, \
		   "should be OM_UPDATE_TYPE_REQUIRED" );
	ASSERT( om_uint32_update_parse_helper(OM_UPDATE_TYPE,"optional") == OM_UPDATE_TYPE_OPTIONAL, \
		   "should be OM_UPDATE_TYPE_OPTIONAL" );
	ASSERT( om_uint32_update_parse_helper(OM_UPDATE_TYPE,"none") == OM_UPDATE_TYPE_NONE, \
		   "should be OM_UPDATE_TYPE_NONE" );
	ASSERT( om_uint32_update_parse_helper(OM_UPDATE_STORAGE_NEEDS,"23432") == 23432, \
		   "result should have been 23432" );
	ASSERT( om_uint32_update_parse_helper(OM_UPDATE_INSTALL_NEEDS,"23432") == 23432, \
		   "result should have been 23432" );
	
	om_prefs_release(prefs);
	om_props_release(props);
	om_config_release(cfg);
	
	ASSERT_FREE
} 
END_TEST()

START_TEST(test_update_perform) {
	{
		om_prefs_ptr prefs = om_prefs_acquire("new prefs");
		om_props_ptr props = om_props_acquire("new props");
		
		om_prefs_set(prefs,om_config_map_to_str(OM_CFG_DEV_TYPE),"device_type");
		om_prefs_set(prefs,om_config_map_to_str(OM_CFG_DEV_UUID),"device_uuid");
		om_prefs_set(prefs,om_config_map_to_str(OM_CFG_APP_NAME),"app_name");
		om_prefs_set(prefs,om_config_map_to_str(OM_CFG_APP_VER),"app_version");
		om_prefs_set(prefs,om_config_map_to_str(OM_CFG_SLIC_VER),"slic_version");
		om_prefs_set(prefs,om_config_map_to_str(OM_CFG_UPDATE_LAST_CHECK),"0");
		
		om_config_ptr cfg = om_config_obtain(prefs,props);
		om_config_set(cfg,OM_CFG_APP_MGMT_URL,"http://tests.openmeap.com/doesntexist");
		
		// we don't need valid xml here, as we're mocking the result of parsing
		// but we do need some content
		 om_net_mock_vars vars;
		memset( (void*)&vars, 0, sizeof(om_net_mock_vars) );
		om_net_set_mock_vars(&vars);
		om_http_response http_post_result;
		memset( (void*)&http_post_result, 0, sizeof(om_http_response) );
		vars.do_http_post_result = &http_post_result;
		vars.do_http_post_result->status_code=200;
		vars.do_http_post_result->result="<none/>";
	
		/////
		
		
		om_prefs_release(prefs);
		om_props_release(props);
		om_config_release(cfg);
		
		ASSERT_FREE
	}
} END_TEST()

#include "storage-test.h"
START_TEST(test_update_header_json_functions) {

    om_prefs_ptr prefs = om_prefs_acquire("new prefs");
    om_props_ptr props = om_props_acquire("new props");
    om_config_ptr cfg = om_config_obtain(prefs,props);
    om_storage_ptr stg = om_storage_alloc(cfg);
    
    om_storage_mock_vars_ptr mock_vars = om_malloc(sizeof(om_storage_mock_vars));
    mock_vars->get_bytes_free_result = 666;
    om_storage_set_mock_vars(mock_vars);
    
    om_update_header_ptr header = om_malloc(sizeof(om_update_header));
    header->install_needs=1000;
    header->storage_needs=11661;
    header->update_url="update_url";
    header->version_id="version_id";
    header->type=OM_UPDATE_TYPE_REQUIRED;
    header->hash=om_malloc(sizeof(om_hash_ptr));
    header->hash->hash_type=OM_HASH_MD5;
    header->hash->hash="hash";
    
    char * json = om_update_header_to_json(stg, header);
    om_update_header_ptr header2 = om_update_header_from_json(json);
    char * roundTripJson = om_update_header_to_json(stg, header2);
    ASSERT(strcmp(json,roundTripJson)==0,"header to json round trip failed");
    om_free(json);
    om_free(roundTripJson);
    om_update_release_update_header(header2);
    
    om_free(header->hash);
    om_free(header);
    om_free(mock_vars);
    om_storage_release(stg);
    om_prefs_release(prefs);
    om_props_release(props);
    om_config_release(cfg);
    
    ASSERT_FREE
    
} END_TEST()

START_TEST(test_update_check) {
	
	om_prefs_ptr prefs = om_prefs_acquire("new prefs");
	om_props_ptr props = om_props_acquire("new props");
	
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_DEV_TYPE),"device_type");
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_DEV_UUID),"device_uuid");
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_APP_NAME),"app_name");
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_APP_VER),"app_version");
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_SLIC_VER),"slic_version");
	om_prefs_set(prefs,om_config_map_to_str(OM_CFG_UPDATE_LAST_CHECK),"0");
	
	om_config_ptr cfg = om_config_obtain(prefs,props);
	om_config_set(cfg,OM_CFG_APP_MGMT_URL,"http://tests.openmeap.com/doesntexist");
	
	om_net_mock_vars vars;
	memset( (void*)&vars, 0, sizeof(om_net_mock_vars) );
	om_net_set_mock_vars(&vars);
	om_http_response http_post_result;
	memset( (void*)&http_post_result, 0, sizeof(om_http_response) );
	vars.do_http_post_result = &http_post_result;
	vars.do_http_post_result->status_code=200;
	vars.do_http_post_result->result=test_response;
	
	{
		om_update_header_ptr header = om_update_check(cfg);
        ASSERT(header!=OM_NULL,"om_update_check should have returned an update pointer");
        om_update_release_update_header(header);
		
        // verify that the posted data passed to om_net_do_http_post() was correct
		ASSERT(strcmp(vars.last_post_data,"action=connection-open-request&device-type=device_type&device-uuid=device_uuid&"\
                      "app-name=app_name&app-version=app_version&hash=&slic-version=slic_version")==0, \
               "posted data was incorrect");
		om_free(vars.last_post_data);
        
		char *authToken = om_config_get(cfg,OM_CFG_AUTH_LAST_TOKEN);
		ASSERT(strcmp(authToken,"auth_token")==0,"the auth token should have been \"auth_token\"");
		om_free(authToken);
        
        // maybe we shouldn't rely on the clock or runtime of the test in a unit test,
		// but i'm sure that 10 seconds will be sufficient for most processors... >_<
        om_uint32 * lastUpdateCheck = om_config_get(cfg,OM_CFG_UPDATE_LAST_CHECK);
		ASSERT(*lastUpdateCheck>(om_time(0)-10),"om_update_connect should have updated the last update check config");
		om_free(lastUpdateCheck);
	}
	
	om_prefs_release(prefs);
	om_props_release(props);
	om_config_release(cfg);
	
	ASSERT_FREE
} END_TEST()

void run_update_tests() {
	UNIT_TEST(test_update_perform);
	UNIT_TEST(test_update_decision);
	UNIT_TEST(test_update_check);
    UNIT_TEST(test_update_header_json_functions);
}