/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

#ifndef __OPENMEAP_SLIC_CORE_PREFERENCES__
#define __OPENMEAP_SLIC_CORE_PREFERENCES__

/**
 * Handle to a preference store for the device
 */
typedef struct om_prefs_type {
	char name[256];    // a name of the preferences store...i'm limiting this to 256, somewhat arbitrarily
	void *device_data; // a pointer to device native information 
	void (*clear)(struct om_prefs_type * prefs);
	om_bool (*set)(struct om_prefs_type * prefs, const char *key, const char *value);
	char * (*get)(struct om_prefs_type * prefs, const char *key);
	void (*remove)(struct om_prefs_type * prefs, const char *key);
} om_prefs, *om_prefs_ptr;

/**
 * Initializes/Loads a preferences store identified by the name passed in
 *
 * @param name The name of the preferences store to acquire.  name must be less than 256 characters.
 * @return A pointer to the om_prefs struct created, or OM_NULL on error.  Sets the global error code.
 */
OM_EXPORT om_prefs_ptr om_prefs_acquire(const char *name);

/**
 * Releases the preferences resource
 *
 * @param prefs The preferences object to release from usage
 */
OM_EXPORT void om_prefs_release(om_prefs_ptr prefs);

/**
 * Clears out the entire preferences store passed in
 */
OM_EXPORT void om_prefs_clear(const om_prefs_ptr prefs);

/**
 * Sets a preference in the devices native implementation of a preferences store.
 * The key and value are copied and may be freed after the call.
 *
 * @param key The key to associate the value to 
 * @param value The value to store
 * @param prefs The prefs store to put the value in
 * @return om_bool OM_TRUE if successful, else OM_FALSE
 */
OM_EXPORT om_bool om_prefs_set(const om_prefs_ptr prefs, const char *key, const char *value);

/**
 * Gets a preference from the devices native implementation of a preferences store.
 * The memory associated to the returned character array (must be freed by the caller).
 *
 * @param prefs The prefs object to search for the key under.
 * @param key The key to look for within the prefs object.
 *
 * @return An allocated c-string that must be freed by the caller, or OM_NULL and the error code is set.
 */
OM_EXPORT char * om_prefs_get(const om_prefs_ptr prefs, const char *key);

/**
 * Removes a key from a preferences store
 */
OM_EXPORT void om_prefs_remove(const om_prefs_ptr prefs, const char *key);

#endif
