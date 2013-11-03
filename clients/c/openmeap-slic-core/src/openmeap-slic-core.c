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

#include <openmeap-slic-core.h>

/**
 * Makes the first run hit to usage.openmeap.com.
 *
 * Dear Developer/Project Management,
 *
 * We've created this function to make a single hit to our tracking url
 * per unique install.  An effort has been made to make it secure and
 * non-reversible.  An effort has been made so that, even should it fail,
 * it only happens once.
 *
 * We're hoping you'll leave this code functional in your production 
 * release so that we can build-up value by tracking unique installs.
 *
 * Yours truly, OpenMEAP
 */
void om_first_run_check(om_config_ptr cfg) {
    
    // don't bother doing this in development mode.
    uint32 *d = om_config_get(cfg,OM_CFG_DEV_MODE);
    if( d!=OM_NULL ) {
        if( *d==1 ) {
            om_free(d);
            return;
        } else {
            om_free(d);
        }
    }
    
    uint32 *n = om_config_get(cfg,OM_CFG_NOT_FIRST_RUN);
    if( n==OM_NULL ) {
        
        // set this first, so even if the request fails
        // it never happens again.
        n = om_malloc(sizeof(uint32));
        *n=1;
        om_config_set(cfg,OM_CFG_NOT_FIRST_RUN,n);
        
        // TODO: determine MAC hash and request to usage.openmeap.com/tracker.gif
        char * mac_address = om_net_get_mac_address();
        char * mac_with_salt = om_string_format("%s.OPENMEAP#$!@3__234",mac_address);
        char * hash = om_digest_hash_string(mac_with_salt,OM_HASH_MD5);
        char * url = om_string_format("http://usage.openmeap.com/tracker.gif?hash=%s",hash);
        om_free(mac_with_salt);
        om_free(mac_address);
        om_free(hash);
        
        om_net_do_http_post(url,OM_NULL);
        om_free(url);
        
    } else {
        
        om_free(n);
    }
}
