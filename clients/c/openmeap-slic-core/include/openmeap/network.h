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

#ifndef __OPENMEAP_SLIC_CORE_NETWORK__
#define __OPENMEAP_SLIC_CORE_NETWORK__

/**
 * This could be written differently,
 * but for OpenMEAP we aren't currently
 * working with anything that is unlikely
 * to fit in memory.
 */
typedef struct om_http_response_type {
	unsigned status_code;
	void * result;
} om_http_response, *om_http_response_ptr;

/**
 * Post a UTF-8 data to a url
 *
 * @param url The url to post to
 * @param post_data The post data to send
 * @return The response data for the HTTP POST or OM_NULL.  Sets error code.
 */
OM_EXPORT om_http_response_ptr om_net_do_http_post(const char *url, const char *post_data);


/**
 * Called by accepting functions periodically during a network download.
 * Callback info is up to the developer to specify.
 */
typedef void (*om_net_download_callback_func_ptr)(void *callback_info, 
                                                  om_http_response_ptr response, 
                                                  om_uint32 bytes_total, 
                                                  om_uint32 bytes_downloaded);

/**
 * Performs an HTTP GET, dumping the result in the om_file_input_stream pointer passed in
 *
 * @param istream The om_file_input_stream to place content into
 * @return The status code of the response
 */
OM_EXPORT om_http_response_ptr om_net_do_http_get_to_file_output_stream(const char *url, 
	om_file_output_stream_ptr ostream, 
	om_net_download_callback_func_ptr callback_func, 
    void * callback_info);

/**
 *
 */
OM_EXPORT void om_net_release_response(om_http_response_ptr response);

OM_EXPORT char * om_net_get_mac_address();

#endif
