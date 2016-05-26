/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
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

#include <openmeap-slic-core.h>

#include "network-mock.h"

om_net_mock_vars_ptr om_net_mock_vars_var = OM_NULL;

void om_net_set_mock_vars(om_net_mock_vars_ptr ptr) {
	om_net_mock_vars_var = ptr;
}

om_http_response_ptr om_net_do_http_post(const char *url, const char *post_data) {
	om_net_mock_vars_var->last_post_data = om_string_copy(post_data);
	
	om_http_response_ptr resp = om_malloc(sizeof(om_http_response));
	resp->status_code = om_net_mock_vars_var->do_http_post_result->status_code;
	resp->result = om_string_copy(om_net_mock_vars_var->do_http_post_result->result);
	return resp;
}

om_http_response_ptr om_net_do_http_get_to_file_output_stream(const char *url, 
															  om_file_output_stream_ptr ostream, 
															  void (*call_back)(void *callback_data, om_http_response_ptr response, om_uint32 bytes_total, om_uint32 bytes_downloaded), 
															  void *callback_data) {
	om_net_mock_vars_var->last_input_stream_passed = ostream;
	om_net_mock_vars_var->last_get_url_passed = om_string_copy(url);
	
	om_http_response_ptr resp = om_malloc(sizeof(om_http_response));
	resp->status_code = om_net_mock_vars_var->do_http_get_result->status_code;
	resp->result = om_string_copy(om_net_mock_vars_var->do_http_get_result->result);
	
	return resp;
}
