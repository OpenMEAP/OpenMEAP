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

package com.openmeap.protocol.dto;

import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.JSONProperty;
import com.openmeap.json.JSONGetterSetter;

public class UpdateHeader implements HasJSONProperties {

	protected Hash hash;
	protected String versionIdentifier;
	protected Long installNeeds;
	protected Long storageNeeds;
	protected String updateUrl;
	protected UpdateType type;
	protected Long spaceAvailable;

	private static JSONProperty[] jsonProperties = new JSONProperty[] {
			new JSONProperty("hash", Hash.class, new JSONGetterSetter() {
				public Object getValue(Object src) {
					return ((UpdateHeader) src).getHash();
				}

				public void setValue(Object dest, Object value) {
					((UpdateHeader) dest).setHash((Hash) value);
				}
			}),
			new JSONProperty("versionIdentifier", String.class,
					new JSONGetterSetter() {
						public Object getValue(Object src) {
							return ((UpdateHeader) src).getVersionIdentifier();
						}

						public void setValue(Object dest, Object value) {
							((UpdateHeader) dest)
									.setVersionIdentifier((String) value);
						}
					}),
			new JSONProperty("installNeeds", Long.class,
					new JSONGetterSetter() {
						public Object getValue(Object src) {
							return ((UpdateHeader) src).getInstallNeeds();
						}

						public void setValue(Object dest, Object value) {
							((UpdateHeader) dest).setInstallNeeds((Long) value);
						}
					}),
			new JSONProperty("storageNeeds", Long.class,
					new JSONGetterSetter() {
						public Object getValue(Object src) {
							return ((UpdateHeader) src).getStorageNeeds();
						}

						public void setValue(Object dest, Object value) {
							((UpdateHeader) dest).setStorageNeeds((Long) value);
						}
					}),
			new JSONProperty("updateUrl", String.class, new JSONGetterSetter() {
				public Object getValue(Object src) {
					return ((UpdateHeader) src).getUpdateUrl();
				}

				public void setValue(Object dest, Object value) {
					((UpdateHeader) dest).setUpdateUrl((String) value);
				}
			}),
			new JSONProperty("type", UpdateType.class, new JSONGetterSetter() {
				public Object getValue(Object src) {
					return ((UpdateHeader) src).getType();
				}

				public void setValue(Object dest, Object value) {
					((UpdateHeader) dest).setType((UpdateType) UpdateType.fromValue((String)value));
				}
			}),
			new JSONProperty("spaceAvailable", Long.class,
					new JSONGetterSetter() {
						public Object getValue(Object src) {
							return ((UpdateHeader) src).getSpaceAvailable();
						}

						public void setValue(Object dest, Object value) {
							((UpdateHeader) dest)
									.setSpaceAvailable((Long) value);
						}
					}) };

	public JSONProperty[] getJSONProperties() {
		return jsonProperties;
	}

	public Hash getHash() {
		return hash;
	}

	public void setHash(Hash value) {
		this.hash = value;
	}

	public String getVersionIdentifier() {
		return versionIdentifier;
	}

	public void setVersionIdentifier(String value) {
		this.versionIdentifier = value;
	}

	public Long getInstallNeeds() {
		return installNeeds;
	}

	public void setInstallNeeds(Long value) {
		this.installNeeds = value;
	}

	public Long getStorageNeeds() {
		return storageNeeds;
	}

	public void setStorageNeeds(Long value) {
		this.storageNeeds = value;
	}

	public String getUpdateUrl() {
		return updateUrl;
	}

	public void setUpdateUrl(String value) {
		this.updateUrl = value;
	}

	public UpdateType getType() {
		return type;
	}

	public void setType(UpdateType value) {
		this.type = value;
	}

	public Long getSpaceAvailable() {
		return spaceAvailable;
	}

	public void setSpaceAvailable(Long spaceAvailable) {
		this.spaceAvailable = spaceAvailable;
	}

}
