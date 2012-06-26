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
#include <stdio.h>
#include <time.h>
#include "cJSON.h"

const char * OmUpdateResultPending = "PENDING";
const char * OmUpdateResultSuccess = "SUCCESS";
const char * OmUpdateResultOutOfSpace = "OUT_OF_SPACE";
const char * OmUpdateResultIoException = "IO_EXCEPTION";
const char * OmUpdateResultHashMismatch = "HASH_MISMATCH";
const char * OmUpdateResultInterrupted = "INTERRUPTED";
const char * OmUpdateResultPlatform = "PLATFORM";
const char * OmUpdateResultResponseStatusCode = "RESPONSE_STATUS_CODE";
const char * OmUpdateResultImportUnzip = "IMPORT_UNZIP";

const char * OmUpdateTypeOptional = "OPTIONAL";
const char * OmUpdateTypeRequired = "REQUIRED";
const char * OmUpdateTypeImmediate = "IMMEDIATE";
const char * OmUpdateTypeNone = "NONE";

om_uint32 om_uint32_update_parse_helper( \
			om_update_parse_type type, const char *str_in) {
	
	char * str = om_string_toupper(str_in);
	om_uint32 ret = 0;
	
	switch(type) {
		
		// update type enumeration
		case OM_UPDATE_TYPE:
			ret = (om_uint32)om_update_string_to_type_enum(str);
			break;
			
		case OM_UPDATE_HASH_TYPE:
			ret = (om_uint32)om_hash_string_to_enum(str);
			break;
			
		// om_uint32 products
		case OM_UPDATE_INSTALL_NEEDS:
		case OM_UPDATE_STORAGE_NEEDS:
			ret = (om_uint32)atoi(str);
			break;
			
		default:
			ret = 0;
			break;
	}
	
	om_free(str);
	return ret;
}

const char * om_update_type_enum_to_string(om_update_type_enum type) {
    switch(type) {
        case OM_UPDATE_TYPE_IMMEDIATE: return OmUpdateTypeImmediate;
        case OM_UPDATE_TYPE_REQUIRED:  return OmUpdateTypeRequired;
        case OM_UPDATE_TYPE_OPTIONAL:  return OmUpdateTypeOptional;
        case OM_UPDATE_TYPE_NONE:      return OmUpdateTypeNone;
    }
    return OM_NULL;
}

om_update_type_enum om_update_string_to_type_enum(const char *str) {
    char *copy = om_string_toupper(str);
    if( strcmp(copy,OmUpdateTypeRequired)==0 ) {
        om_free(copy);return OM_UPDATE_TYPE_REQUIRED;
    }
    if( strcmp(copy,OmUpdateTypeOptional)==0 ) {
        om_free(copy);return OM_UPDATE_TYPE_OPTIONAL;
    }
    if( strcmp(copy,OmUpdateTypeImmediate)==0 ) {
        om_free(copy);return OM_UPDATE_TYPE_IMMEDIATE;
    }
    if( strcmp(copy,OmUpdateTypeNone)==0 ) {
        om_free(copy);return OM_UPDATE_TYPE_NONE;
    }
    return 0;
}

om_bool om_update_decision(om_config_ptr cfg) {
	om_uint32 * lastUpdate = om_config_get(cfg,OM_CFG_UPDATE_LAST_ATTEMPT);
	om_uint32 * updateFreq = om_config_get(cfg,OM_CFG_UPDATE_FREQ);
	om_bool * shouldPullUpdates = om_config_get(cfg,OM_CFG_UPDATE_SHOULD_PULL);
	om_bool shouldUpdate = OM_TRUE;
	
	if( //lastUpdate == OM_NULL || // may be null, if they've never attempted to update
	   updateFreq == OM_NULL || 
	   shouldPullUpdates == OM_NULL ) {
		om_free(lastUpdate);
		om_free(updateFreq);
		om_free(shouldPullUpdates);
		return OM_FALSE;
	}
	
	if( shouldUpdate && lastUpdate!=OM_NULL ) {
		om_uint32 now = om_time(NULL);
		shouldUpdate = *updateFreq != 0 ? (now - *lastUpdate > *updateFreq) : OM_FALSE;
	}
	
	shouldUpdate = (*shouldPullUpdates) && shouldUpdate;
	om_free(lastUpdate);
	om_free(updateFreq);
	om_free(shouldPullUpdates);
	
	return shouldUpdate;
}

char * om_update_connreq_create_post(om_update_connreq_ptr request) {
	const char *template = "action=connection-open-request&device-type=%s&device-uuid=%s&" \
						"app-name=%s&app-version=%s&hash=%s&slic-version=%s";
    
    om_update_connreq_ptr encodedRequest = om_malloc( sizeof(om_update_connreq) );
    if( encodedRequest==OM_NULL ) {
        return OM_NULL;
    }
    encodedRequest->device_type=om_string_encodeURI(request->device_type);
    encodedRequest->device_uuid=om_string_encodeURI(request->device_uuid);
    encodedRequest->app_name=om_string_encodeURI(request->app_name);
    encodedRequest->app_version=om_string_encodeURI(request->app_version);
    encodedRequest->app_version_hash=om_string_encodeURI(request->app_version_hash!=OM_NULL?request->app_version_hash:"");
    encodedRequest->slic_version=om_string_encodeURI(request->slic_version);
    
	int size = (
				( strlen(encodedRequest->device_type) + strlen(encodedRequest->device_uuid) \
				+ strlen(encodedRequest->app_name) + strlen(encodedRequest->app_version) 
                + strlen(encodedRequest->app_version_hash) \
				+ strlen(encodedRequest->slic_version) + strlen(template) ) * sizeof(char) ) \
				- (5*2*sizeof(char)) + (1*sizeof(char));
    
	char * ret = om_malloc(size+1);
	if( ret==OM_NULL ) {
        om_update_release_connreq(encodedRequest);
		return OM_NULL;
    }
	sprintf(ret,template,encodedRequest->device_type,encodedRequest->device_uuid, \
			encodedRequest->app_name,encodedRequest->app_version,encodedRequest->app_version_hash,encodedRequest->slic_version);	
    
    om_update_release_connreq(encodedRequest);
    
	return ret;
}

om_update_check_result_ptr om_update_check(om_config_ptr cfg) {
	
	om_error_clear();
	
	om_update_header_ptr ret = OM_NULL;
	
	// put together the connection open request and post data
	om_update_connreq_ptr request = om_update_create_connreq(cfg);
	char *postData = om_update_connreq_create_post(request);
	char *svcUrl = om_config_get(cfg,OM_CFG_APP_MGMT_URL);
	char *fullUrl = om_string_format("%s%c%s",svcUrl,'?',postData);
	if( 
	   fullUrl==OM_NULL || 
	   postData==OM_NULL || 
	   svcUrl==OM_NULL 
	) {
		om_update_release_connreq(request);
		om_free(svcUrl);
		om_free(postData);
		om_free(fullUrl);
		return OM_NULL;
	}
	om_free(svcUrl);
	svcUrl=fullUrl;
	
	// post to the service url
	om_http_response_ptr response = om_net_do_http_post(svcUrl,postData);
	if( response==OM_NULL || response->status_code!=200 || om_error_get_code()!=OM_ERR_NONE ) {
		om_update_release_connreq(request);
		om_free(svcUrl);
		om_free(postData);
		// the post function should have set the appropriate error code
		return OM_NULL;
	}
	
	om_update_check_result_ptr updateResult = om_update_parse_check_result(response->result);
	if( !updateResult || om_error_get_code()!=OM_ERR_NONE ) {
		
		om_update_release_connreq(request);
		om_net_release_response(response);
		om_free(svcUrl);
		om_free(postData);
		
		// the parsing function should have set the appropriate error code
		return OM_NULL;
	}
	
	// these are no longer needed and may be freed
	om_update_release_connreq(request);
	om_net_release_response(response);
	om_free(svcUrl);
	om_free(postData);
	
	// TODO: determine if the lack of an auth_token represents an error
	if( updateResult->response!=OM_NULL ) {
        
        if( updateResult->response->auth_token!=OM_NULL ) {
            om_config_set(cfg,OM_CFG_AUTH_LAST_TOKEN,updateResult->response->auth_token);
        }
                
        om_uint32 lastCheck = om_time(0);
        om_config_set(cfg,OM_CFG_UPDATE_LAST_CHECK,&lastCheck);
	
    }
	
	return updateResult;
}

void om_update_release_connreq(om_update_connreq_ptr ptr) {
	om_free(ptr->device_type);
	om_free(ptr->device_uuid);
	om_free(ptr->app_name);
	om_free(ptr->app_version);
    om_free(ptr->app_version_hash);
	om_free(ptr->slic_version);
	om_free(ptr);
}

om_update_connreq_ptr om_update_create_connreq(om_config_ptr cfg) {
	om_error_clear();
	om_update_connreq_ptr ptr = om_malloc(sizeof(om_update_connreq));
	if( ptr==OM_NULL ) {
		om_error_set_code(OM_ERR_MALLOC);
		return ptr;
	}
	ptr->device_type = om_config_get(cfg,OM_CFG_DEV_TYPE);
	ptr->device_uuid = om_config_get(cfg,OM_CFG_DEV_UUID);
	ptr->app_name = om_config_get(cfg,OM_CFG_APP_NAME);
	ptr->app_version = om_config_get(cfg,OM_CFG_APP_VER);
    ptr->app_version_hash = om_config_get(cfg,OM_CFG_APP_VER_HASH);
	ptr->slic_version = om_config_get(cfg,OM_CFG_SLIC_VER);
	if( om_error_get_code()!=OM_ERR_NONE ) {
		om_update_release_connreq(ptr);
		return OM_NULL;
	}
	return ptr;
}

void om_update_release_check_result(om_update_check_result_ptr result) {
    if(result->response!=OM_NULL) {
        om_update_release_connresp(result->response);
        result->response=OM_NULL;
    }
    if(result->error!=OM_NULL) {
        om_update_release_check_error(result->error);
        result->error=OM_NULL;
    }
    om_free(result);
}

void om_update_release_check_error(om_update_check_error_ptr error) {
    om_free(error->code);
    error->code=OM_NULL;
    om_free(error->message);
    error->message=OM_NULL;
    om_free(error);
}

void om_update_release_connresp(om_update_connresp_ptr connresp) {
	if( connresp->auth_token != OM_NULL )   om_free(connresp->auth_token);
	if( connresp->update != OM_NULL ) {
		om_update_release_update_header(connresp->update);
		connresp->update=OM_NULL;
	}
	om_free(connresp);
}
					 
void om_update_release_update_header(om_update_header_ptr update) {
	om_free(update->update_url);
	om_free(update->version_id);
	if( update->hash!=OM_NULL ) {
		om_hash_ptr hash = update->hash;
		om_free(hash->hash);
		om_free(hash);
	}
	om_free(update);
}

om_update_status_ptr om_update_status_new() {
	return om_malloc(sizeof(struct om_update_status_type));
}

void om_update_status_release(om_update_status_ptr status) {
    om_update_release_update_header(status->update_header);
	if( status->error_mesg!=OM_NULL ) {
		om_free(status->error_mesg);
	}
	om_free(status);
}

char * om_update_status_to_json(om_storage_ptr stg, om_update_status_ptr cUpdateStatus) {
    
    cJSON *jsonUpdate = cJSON_CreateObject();
    cJSON_AddItemToObject(jsonUpdate,"bytesDownloaded",cJSON_CreateNumber((double)cUpdateStatus->bytes_downloaded));
    cJSON_AddItemToObject(jsonUpdate,"complete",cJSON_CreateBool((int)cUpdateStatus->complete));
    
    cJSON *err = cUpdateStatus->error_type==OM_NULL ? cJSON_CreateNull( ) : cJSON_CreateObject();
    if( cUpdateStatus->error_type!=OM_NULL ) {
        cJSON_AddItemToObject(err,"type",cJSON_CreateString(cUpdateStatus->error_type));
        cJSON_AddItemToObject(err,"message",cJSON_CreateString(cUpdateStatus->error_mesg));
    }
    cJSON_AddItemToObject(jsonUpdate,"error",err);

    char * headerJson = om_update_header_to_json(stg, cUpdateStatus->update_header);
    cJSON_AddItemToObject(jsonUpdate,"update",cJSON_Parse(headerJson));
    om_free(headerJson);
    
    char * ret = cJSON_Print(jsonUpdate);
    cJSON_Delete(jsonUpdate);
    return ret;
}

char * om_update_header_to_json(om_storage_ptr stg, om_update_header_ptr header) {
    
    cJSON *hdr = cJSON_CreateObject();
    cJSON_AddItemToObject(hdr,"spaceAvailable",cJSON_CreateNumber(om_storage_get_bytes_free(stg)));
    cJSON_AddItemToObject(hdr,"installNeeds",cJSON_CreateNumber(header->install_needs));
    cJSON_AddItemToObject(hdr,"storageNeeds",cJSON_CreateNumber(header->storage_needs));
    cJSON_AddItemToObject(hdr,"updateUrl",cJSON_CreateString(header->update_url));
    cJSON_AddItemToObject(hdr,"versionIdentifier",cJSON_CreateString(header->version_id));
    cJSON_AddItemToObject(hdr,"type",cJSON_CreateString(om_update_type_enum_to_string(header->type)));
    
    cJSON *hash = cJSON_CreateObject();
    cJSON_AddItemToObject(hash,"algorithm",cJSON_CreateString(om_hash_enum_to_string(header->hash->hash_type)));
    cJSON_AddItemToObject(hash,"value",cJSON_CreateString(header->hash->hash));
    cJSON_AddItemToObject(hdr,"hash",hash);
    
    char * ret = cJSON_Print(hdr);
    cJSON_Delete(hdr);
    return ret;
}

om_update_header_ptr __om_update_header_from_cJSON(cJSON *jsonHeader) {
    
    cJSON *installNeeds = cJSON_GetObjectItem(jsonHeader,"installNeeds");
    cJSON *storageNeeds = cJSON_GetObjectItem(jsonHeader,"storageNeeds");
    cJSON *updateUrl = cJSON_GetObjectItem(jsonHeader,"updateUrl");
    cJSON *versionId = cJSON_GetObjectItem(jsonHeader,"versionIdentifier");
    cJSON *type = cJSON_GetObjectItem(jsonHeader,"type");
    cJSON *jsonHash = cJSON_GetObjectItem(jsonHeader,"hash");
    if(  ( installNeeds==NULL || installNeeds->type!=cJSON_Number )
       || ( storageNeeds==NULL || storageNeeds->type!=cJSON_Number )
       || ( updateUrl==NULL || updateUrl->type!=cJSON_String )
       || ( versionId==NULL || versionId->type!=cJSON_String )
       || ( type==NULL || type->type!=cJSON_String )
       || ( jsonHash==NULL || jsonHash->type!=cJSON_Object )
       ) {
        return OM_NULL;
    }
    cJSON *hashAlg = cJSON_GetObjectItem(jsonHash,"algorithm");
    cJSON *hashValue = cJSON_GetObjectItem(jsonHash,"value");
    if(  ( installNeeds==NULL || installNeeds->type!=cJSON_Number )
       || ( storageNeeds==NULL || storageNeeds->type!=cJSON_Number )
       ) {
        return OM_NULL;
    }
    
    om_update_header_ptr header = om_malloc(sizeof(om_update_header));
    if( header==OM_NULL ) {
        return OM_NULL;
    }
    om_hash_ptr hash = om_malloc(sizeof(om_hash));
    if( hash==OM_NULL ) {
        om_free(header);
        return OM_NULL;
    }
    
    header->install_needs = installNeeds->valueint;
    header->storage_needs = storageNeeds->valueint;
    header->type = om_update_string_to_type_enum(type->valuestring);
    header->update_url = om_string_copy(updateUrl->valuestring);
    header->version_id = om_string_copy(versionId->valuestring);
    
    header->hash = hash;
    hash->hash_type = om_hash_string_to_enum(hashAlg->valuestring);
    hash->hash = om_string_copy(hashValue->valuestring);
    
    return header;
}

om_update_check_result_ptr om_update_parse_check_result(char *json) {
    
    om_update_check_result_ptr result = OM_NULL;
    cJSON *resultJson = cJSON_Parse(json);
    if( resultJson==NULL ) {
        return result;
    }
    result = om_malloc(sizeof(om_update_check_result));
    
    cJSON *errorJson = cJSON_GetObjectItem(resultJson,"error");
    cJSON *connRespJson = cJSON_GetObjectItem(resultJson,"connectionOpenResponse");
    
    om_update_header_ptr header = OM_NULL;
    if( connRespJson!=NULL ) {
        om_update_connresp_ptr response = om_malloc(sizeof(om_update_connresp));
        cJSON *updateJson = cJSON_GetObjectItem(connRespJson,"update");
        if( updateJson!=NULL ) {
            header = __om_update_header_from_cJSON(updateJson);
            response->update = header;
        }
        cJSON *authTokenJson = cJSON_GetObjectItem(connRespJson,"authToken");
        if( authTokenJson!=NULL ) {
            response->auth_token = om_string_copy(authTokenJson->valuestring);
        }
        result->response = response;
    }
    
    if( errorJson!=NULL ) {
        om_update_check_error_ptr error = om_malloc(sizeof(om_update_check_error));
        cJSON *code = cJSON_GetObjectItem(errorJson,"code");
        if(code!=NULL) {
            error->code=om_string_copy(code->valuestring);
        }
        cJSON *message = cJSON_GetObjectItem(errorJson,"message");
        if(code!=NULL) {
            error->code=om_string_copy(code->valuestring);
        }
        result->error = error;
    }
    
    cJSON_Delete(resultJson);    
    return result;
}

om_update_header_ptr om_update_header_from_json(const char *strJsonUpdateHeader) {
    cJSON *jsonHeader = cJSON_Parse(strJsonUpdateHeader);
    om_update_header_ptr header = __om_update_header_from_cJSON(jsonHeader);
    cJSON_Delete(jsonHeader);
    return header;
}

/**
 * @return OM_NULL if nothing done, OmUpdateResultIoException if unable to reset storage, OmUpdateResultPlatform if unable to set config, else OmUpdateResultSuccess
 */
const const char * __om_update_check_revert_to_original(om_config_ptr cfg, om_storage_ptr stg, om_update_header_ptr update, 
														om_update_callback_func_ptr callback,
                                                        om_update_callback_info_ptr callback_info) {
	// if the new version is the original version,
	// then we'll just update the app version, delete internal storage and return
	char * origVersionId = om_config_get_original(cfg,OM_CFG_APP_VER);
	const char * retVal = 0;
	
	if( strcmp(origVersionId,update->version_id)==0 ) {
		
		if( om_config_set(cfg,OM_CFG_APP_VER,update->version_id)==OM_FALSE || om_error_get_code()!=OM_ERR_NONE ) {
			retVal=OmUpdateResultPlatform;
		}
		if( !om_storage_reset_storage(stg) ) {
			// we tried to delete whatever had been downloaded
			// but some error occurred during the delete
			retVal=OmUpdateResultIoException;
		} else {
			retVal=OmUpdateResultSuccess;
		}
		
		// clear out the current storage location
		om_prefs_remove(cfg->prefs,om_config_map_to_str(OM_CFG_CURRENT_STORAGE));
	} 
	
	om_free(origVersionId);
	return retVal;
}

const char * __om_update_import_check_space(om_config_ptr cfg, om_storage_ptr stg, om_update_header_ptr update, 
											om_update_callback_func_ptr callback,
                                            om_update_callback_info_ptr callback_info) {
	om_uint32 avail = om_storage_get_bytes_free(stg);
	
	if( avail < update->install_needs ) {
		om_error_set(OM_ERR_FILE_SPACE,"Not enough space to install new version");
		return OmUpdateResultOutOfSpace;
	}
	
	return OmUpdateResultSuccess;
}

const char * __om_update_import_download(om_config_ptr cfg, om_storage_ptr stg, 
									   om_update_header_ptr update,
                                         om_update_callback_func_ptr callback,
                                         om_update_callback_info_ptr callback_info) {
	
	const char * retVal = OmUpdateResultSuccess;
	
	om_file_output_stream_ptr fos = om_storage_get_import_archive_output_stream(stg);
	if( fos==OM_NULL ) {
		retVal = OmUpdateResultIoException;
	}
    
    om_http_response_ptr resp = OM_NULL;
	if( callback!=OM_NULL ) {
        resp = om_net_do_http_get_to_file_output_stream(update->update_url,fos,
                                                                             callback_info->net_download_callback_func_ptr,
                                                                             callback_info->net_download_callback_data);
    } else {
        resp = om_net_do_http_get_to_file_output_stream(update->update_url,fos,OM_NULL,OM_NULL);
    }
    
	if( resp == OM_NULL ) {
		om_error_set_code(OM_ERR_NET_CONN);
		retVal = OmUpdateResultIoException;
	} else if( resp->status_code!=200 ) {
		om_error_set_format(OM_ERR_NET_DOWNLOAD,"The server returned a status code %i for url %s",resp->status_code,update->update_url);
		retVal = OmUpdateResultResponseStatusCode;
	}
	
	if( resp!=OM_NULL ) {
		om_net_release_response(resp);
	}
	
	om_storage_close_file_output_stream(fos);
	
	return retVal;
}

const char * __om_update_import_validate(om_config_ptr cfg, om_storage_ptr stg, om_update_header_ptr update, 
                                         om_update_callback_func_ptr callback,
                                         om_update_callback_info_ptr callback_info) {
	
	char * filePath = om_config_get(cfg,OM_CFG_IMPORT_ARCHIVE_PATH);
	if( filePath == OM_NULL )
		return OmUpdateResultPlatform;
	
	om_file_input_stream_ptr fis = om_storage_open_file_input_stream(stg,filePath);
	om_free(filePath);
	if( fis==OM_NULL ) {
		return OmUpdateResultIoException;
	}
	
	char * hashValue = om_digest_hash_file(fis,update->hash->hash_type);
	if( hashValue==OM_NULL ) {
		;// TODO: handle this error
	}
	const char * retVal = strcmp(hashValue,update->hash->hash)==0 
                        ? OmUpdateResultSuccess 
                        : OmUpdateResultHashMismatch;
	om_storage_close_file_input_stream(fis);
	om_free(hashValue);
	
	return retVal;
}

char * __om_localstorage_path_for_update(om_storage_ptr stg, const om_update_header_ptr update) {
    // create a storage_base/hash_value path to unzip to
    char * local_storage_path = om_storage_get_localstorage_path(stg);
    char * origAppVer = om_config_get_original(stg->cfg,OM_CFG_APP_VER);
    if( strcmp(origAppVer,update->version_id)==0 ) {
        om_free(origAppVer);
        return local_storage_path;
    }
    char * unzip_location = om_string_format("%s%c%s",local_storage_path,OM_FS_FILE_SEP,update->hash->hash);
    om_free(local_storage_path);
    return unzip_location;
}

const char * __om_update_import_unzip(om_config_ptr cfg, om_storage_ptr stg, om_update_header_ptr update, 
                                      om_update_callback_func_ptr callback,
                                      om_update_callback_info_ptr callback_info) {
	
    // create a storage_base/hash_value path to unzip to
    char * unzip_location = __om_localstorage_path_for_update(stg,update);
	
	char * import_archive_location = om_config_get(cfg,OM_CFG_IMPORT_ARCHIVE_PATH);
    
	om_unzip_archive_ptr archive = om_unzip_open_archive(import_archive_location);
	
	if( archive==OM_NULL || import_archive_location==OM_NULL || unzip_location==OM_NULL ) {
        
        om_storage_delete_directory(stg,unzip_location,OM_TRUE);
		om_free(import_archive_location);
		om_free(unzip_location);
		om_unzip_close_archive(archive);
		return OmUpdateResultPlatform;
	}
	
	om_storage_create_directories_for_path(stg, unzip_location);
    const char * retVal = om_unzip_archive_into_path(archive,unzip_location) 
							? OmUpdateResultSuccess 
							: OmUpdateResultImportUnzip;
	
	// if we either weren't able to unzip the archive
	// or weren't able to signify the change in version
	// then return false
	if( retVal==OmUpdateResultImportUnzip || ! om_config_set(cfg,OM_CFG_APP_VER,update->version_id) ) {
        
        om_storage_delete_directory(stg,unzip_location,OM_TRUE);
        retVal=OmUpdateResultImportUnzip;
	}
	
	om_free(import_archive_location);
	om_free(unzip_location);
	om_unzip_close_archive(archive);
	
	return retVal;
}

const char * om_update_perform(om_config_ptr cfg, om_storage_ptr stg, om_update_header_ptr update) {
	return om_update_perform_with_callback(cfg,stg,update,OM_NULL,OM_NULL);
}

const char * om_update_perform_with_callback(om_config_ptr cfg, 
                                             om_storage_ptr stg, 
                                             om_update_header_ptr update_header, 
                                             om_update_callback_func_ptr callback,
                                             om_update_callback_info_ptr callback_info)
{
    om_uint32 * lastTime = om_config_get(cfg,OM_CFG_UPDATE_LAST_ATTEMPT);
    om_uint32 * pendingTimeout = om_config_get(cfg,OM_CFG_UPDATE_PENDING_TIMEOUT);
    om_uint32 currentTime = time(0);
    char * lastUpdateResult = om_config_get(cfg,OM_CFG_UPDATE_LAST_RESULT);
    om_uint32 timeoutTime = 0;    
    
	if( lastUpdateResult!=OM_NULL && lastTime!=OM_NULL 
            && (timeoutTime = *lastTime + *pendingTimeout)
            && currentTime < timeoutTime
            && strcmp(lastUpdateResult,OmUpdateResultPending)==0
       ) {
        om_free(lastUpdateResult);
        om_free(lastTime);
        om_free(pendingTimeout);
		return OmUpdateResultPending;
	} else {
        om_free(lastUpdateResult);
        om_free(lastTime);
        om_free(pendingTimeout);
        om_config_set(cfg,OM_CFG_UPDATE_LAST_RESULT,OmUpdateResultPending);
    }
	
	const char * retVal = OmUpdateResultSuccess;
	const char * r=0;
	
	if( om_config_set(cfg,OM_CFG_UPDATE_LAST_ATTEMPT,&currentTime) ) {
		if( 
			// check to see if we're just reverting to the original version
			__om_update_check_revert_to_original(cfg,stg,update_header,callback,callback_info)==0
			   
			// make sure there is enough space for the update
			&& (r=__om_update_import_check_space(cfg,stg,update_header,callback,callback_info))==OmUpdateResultSuccess

			// download to the import.zip location
			&& (r=__om_update_import_download(cfg,stg,update_header,callback,callback_info))==OmUpdateResultSuccess

			// validate the zip file against the hash of the response
			&& (r=__om_update_import_validate(cfg,stg,update_header,callback,callback_info))==OmUpdateResultSuccess

			// flip-flop storage and unzip the import archive into it
			// and update the config to point to the new version
			&& (r=__om_update_import_unzip(cfg,stg,update_header,callback,callback_info))==OmUpdateResultSuccess
			) {} 
            if( callback_info!=OM_NULL && callback!=OM_NULL ) {
                
                callback_info->update_status->complete=OM_TRUE;
                callback_info->update_status->error_type=OM_NULL;
                callback(callback_info);
            }
			retVal = r;
	} else {
		retVal = OmUpdateResultPlatform;
	}
	
	if( retVal == OmUpdateResultSuccess ) {
		
        int res = 1;
        om_config_set(cfg,OM_CFG_APP_UPDATED,(int *)&res);
        if( callback_info!=OM_NULL && callback!=OM_NULL ) {
            callback_info->update_status->complete=OM_TRUE;
            callback_info->update_status->error_type=OM_NULL;
            callback(callback_info);
        }
        
        om_storage_reset_storage(stg);
        char * unzip_location = __om_localstorage_path_for_update(stg,update_header);
        om_config_set(cfg,OM_CFG_CURRENT_STORAGE,unzip_location);
        om_free(unzip_location);
	}
    
    // it's possible that we've reverted to the original version
    // in which case the current retVal will be 0 at this point
    // so we need to correct that.
    retVal = retVal==0?OmUpdateResultSuccess:retVal;
    if( retVal == OmUpdateResultSuccess ) {
        om_config_set(cfg,OM_CFG_APP_VER_HASH,update_header->hash->hash);
    }
    
    om_config_set(cfg,OM_CFG_UPDATE_LAST_RESULT,retVal);
    
    // doesn't matter what happened...we're not taking up space needlessly
    om_storage_delete_import_archive(stg);
	
	return retVal;
}

