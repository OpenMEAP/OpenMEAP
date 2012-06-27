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

#ifndef __OPENMEAP_UPDATE_H__
#define __OPENMEAP_UPDATE_H__

/**
 * Embodies a connection open request to the app mgmt svc
 */
typedef struct om_update_connreq_type {
	char *device_type;
	char *device_uuid;
	char *app_name;
	char *app_version;
    char *app_version_hash;
	char *slic_version;
} om_update_connreq, *om_update_connreq_ptr;

typedef enum om_update_type_enum_type {
	OM_UPDATE_TYPE_NONE=2001,
	OM_UPDATE_TYPE_OPTIONAL,
	OM_UPDATE_TYPE_REQUIRED,
	OM_UPDATE_TYPE_IMMEDIATE
} om_update_type_enum;

/**
 * A hash we can use to validate the package downloaded
 */
typedef struct om_hash_type {
	om_hash_enum hash_type;
	char *hash;
} om_hash, *om_hash_ptr;

/**
 * Part of the connection result
 */
typedef struct om_update_check_error_type {
    char *code;
    char *message;
} om_update_check_error, *om_update_check_error_ptr;

/**
 * Part of the app mgmt svc response, if an update is available
 */
typedef struct om_update_header_type {
	om_hash_ptr hash;
	om_uint32 install_needs;
	om_uint32 storage_needs;
	om_update_type_enum type;
	char *update_url;
	char *version_id;
} om_update_header, *om_update_header_ptr;

/**
 * Embodies the response from the application mgmt svc
 */
typedef struct om_update_connresp_type {
	char *auth_token;
	om_update_header_ptr update;
} om_update_connresp, *om_update_connresp_ptr;

typedef struct om_update_check_result_type {
    om_update_connresp_ptr response;
    om_update_check_error_ptr error;
} om_update_check_result, *om_update_check_result_ptr;

/**
 * Enum for the om_uint32_update_parse_helper() function
 */
typedef enum om_update_parse_type_enum {
	OM_UPDATE_TYPE=3001,
	OM_UPDATE_STORAGE_NEEDS,
	OM_UPDATE_INSTALL_NEEDS,
	OM_UPDATE_HASH_TYPE
} om_update_parse_type;

/**
 * Handles the conversion from string to the type appropriate
 */
OM_EXPORT om_uint32 om_uint32_update_parse_helper( \
		om_update_parse_type type, const char *str);

/**
 * Uses the config passed in to determine if it's time to check 
 * for an update or not.
 *
 * Exists in here 
 *
 * @return true if we should update, else false
 */
OM_EXPORT om_bool om_update_decision(om_config_ptr cfg);

/**
 * Posts to the configured service, a connreq struct, and
 * parses the response to determine if an update is available or not.
 *
 * Passes an om_update_header pointer back,
 * which must be freed using om_update_release_update_header().
 *
 * @param cfg A configuration
 * @return Either a pointer to an om_update_header struct or null.
 */
OM_EXPORT om_update_check_result_ptr om_update_check(om_config_ptr cfg);

/**
 * Takes the om_update_connreq structure and converts it to a POST
 * uri-encoded string of key/value pairs.
 *
 * @return The post data for the om_update_check()...must be freed by caller
 */
OM_EXPORT char * om_update_connreq_create_post(om_update_connreq_ptr request);

/* *************** *
 * REQUEST RELATED *
 * *************** */

/**
 * Uses the config passed in to generate a connection request struct
 *
 * @return A connection request struct for passing to the app mgmt svc
 */
OM_EXPORT om_update_connreq_ptr om_update_create_connreq(om_config_ptr cfg);

/**
 * Used to release the resources taken up by the om_update_connreq
 *
 * @param ptr A pointer to the om_update_connreq to free up.
 */
OM_EXPORT void om_update_release_connreq(om_update_connreq_ptr ptr);

/* **************** *
 * RESPONSE RELATED *
 * **************** */

OM_EXPORT void om_update_release_check_result(om_update_check_result_ptr result);

OM_EXPORT void om_update_release_check_error(om_update_check_error_ptr error);

/**
 * Releases the resources acquired by the response object
 */
OM_EXPORT void om_update_release_connresp(om_update_connresp_ptr connresp);

/**
 * Used for releasing the update header passed back by om_update_check.
 */
OM_EXPORT void om_update_release_update_header(om_update_header_ptr ptr);

OM_EXPORT const char * OmUpdateResultPending;// = "PENDING";
OM_EXPORT const char * OmUpdateResultSuccess;// = "SUCCESS";
OM_EXPORT const char * OmUpdateResultOutOfSpace;// = "OUT_OF_SPACE";
OM_EXPORT const char * OmUpdateResultIoException;// = "IO_EXCEPTION";
OM_EXPORT const char * OmUpdateResultHashMismatch;// = "HASH_MISMATCH";
OM_EXPORT const char * OmUpdateResultInterrupted;// = "INTERRUPTED";
OM_EXPORT const char * OmUpdateResultPlatform;// = "PLATFORM";
OM_EXPORT const char * OmUpdateResultResponseStatusCode;// = "RESPONSE_STATUS_CODE";
OM_EXPORT const char * OmUpdateResultImportUnzip;// = "IMPORT_UNZIP";

OM_EXPORT const char * OmUpdateTypeOptional;// = "OPTIONAL";
OM_EXPORT const char * OmUpdateTypeRequired;// = "REQUIRED";
OM_EXPORT const char * OmUpdateTypeImmediate;// = "IMMEDIATE";
OM_EXPORT const char * OmUpdateTypeNone;// = "NONE";

OM_EXPORT const char * om_update_type_enum_to_string(om_update_type_enum type);
OM_EXPORT om_update_type_enum om_update_string_to_type_enum(const char *str);

/**
 *
 */
typedef struct om_update_status_type {
	om_update_header_ptr update_header;
	om_uint32 bytes_downloaded;
	om_bool complete;	
	char * error_type;
	char * error_mesg;
} om_update_status, *om_update_status_ptr;

/**
 * Structure to ferry information into the callback function
 */
typedef struct om_update_callback_info {
    
    /**
     * The status information we're continually passed back during download.
     */
    om_update_status_ptr update_status;
    
    /**
     * The callback function for the networking functions.
     * Seems unnecessary, but separation of responsibilities
     * sort of mandates it.
     */
    om_net_download_callback_func_ptr net_download_callback_func_ptr;
    
    /**
     * The data to pass to the net_download_callback_func_ptr function.
     * In iOS, For the javascript api, this will be a pointer to this structure.
     */
    void *net_download_callback_data;
    
    /**
     * The javascript callback to execute each bit of data downloaded.
     */
    char *javascript;
    
} om_update_callback_info_type, *om_update_callback_info_ptr;

/**
 * The callback function to execute during the course of the update.
 * Necessary because of different device platforms.
 */
typedef void (*om_update_callback_func_ptr)(om_update_callback_info_ptr update_status);

/**
 * @return a new string that must be released by the caller.
 */
OM_EXPORT char * om_update_status_to_json(om_storage_ptr stg, om_update_status_ptr cUpdateStatus);
OM_EXPORT char * om_update_header_to_json(om_storage_ptr stg, om_update_header_ptr header);
OM_EXPORT om_update_header_ptr om_update_header_from_json(const char *json);

/**
 * @return an entirely new om_update_status struct.  everything including the update_header must be released by caller.
 */
OM_EXPORT om_update_status_ptr om_update_status_from_json(char *jsonUpdateStatus);

OM_EXPORT om_update_status_ptr om_update_status_new();

OM_EXPORT void om_update_status_release(om_update_status_ptr status);

/**
 * Perform the update indicated by the update header passed in.
 */
OM_EXPORT const char * om_update_perform(om_config_ptr cfg, om_storage_ptr stg, om_update_header_ptr update);

/**
 * Perform the update indicated by the update header passed in,
 * calling the on_status_change callback passed in at various life-cycle
 * stages of the update.
 *
 * @param cfg
 * @param stg
 * @param update_header information regarding the update to perform.
 * @param on_status_change a callback method to invoke on life-cycle status changes.
 * @param update_status a pointer to a structure which starts with an om_update_status, 
 *        but can contain other stuff used by the on_status_change method as well.
 * @return one of the OmUpdateResult constants
 */
OM_EXPORT const char * om_update_perform_with_callback(om_config_ptr cfg, 
                                                       om_storage_ptr stg, 
                                                       om_update_header_ptr update_header, 
                                                       om_update_callback_func_ptr callback,
                                                       om_update_callback_info_ptr callback_info);

/////
// DEVICE SPECIFIC IMPLEMENTATIONS 
//   - different platforms have different native xml parsing capabilities

/**
 * Parses the xml response from the app mgmt svc into an update header object
 */
OM_EXPORT om_update_connresp_ptr om_update_parse_connresp(char *resp);
OM_EXPORT om_update_check_error_ptr om_update_parse_check_error(char *error);
OM_EXPORT om_update_check_result_ptr om_update_parse_check_result(char *json);

#endif