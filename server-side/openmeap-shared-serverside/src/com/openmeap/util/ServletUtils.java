/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.openmeap.constants.FormConstants;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.GlobalSettings;

public abstract class ServletUtils {
	private ServletUtils() {}
	
	// TODO: seriously, javadoc the piece handleFileUploads method
	/**
	 * @param modelManager
	 * @param request
	 * @param map
	 * @return
	 * @throws FileUploadException
	 */
	static final public Boolean handleFileUploads(GlobalSettings settings, HttpServletRequest request, Map<Object,Object> map) throws FileUploadException {
		
		DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(4096);
        factory.setRepository(new File(settings.getTemporaryStoragePath()));
        
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(settings.getMaxFileUploadSize());
        
        List fileItems = upload.parseRequest(request);
        for(FileItem item : (List<FileItem>)fileItems) {
			// we'll put this in the parameter map,
			// assuming the backing that is looking for it
			// knows what to expect
        	String fieldName = item.getFieldName();
        	Boolean isFormField = item.isFormField();
        	Long size = item.getSize();
        	if( isFormField ) {
        		if( size>0 ) {
        			map.put(fieldName,new String[]{item.getString()});
        		}
        	} else if( !isFormField ){
        		map.put(fieldName,item);
        	}
        }
        
        return true;
	}
	
	// TODO: javadoc the cloneParameterMap method
	/**
	 * 
	 * @param settings
	 * @param request
	 * @return
	 */
	static final public Map<Object,Object> cloneParameterMap(GlobalSettings settings, HttpServletRequest request) {
		
		// make a tidy package of parameters for the DocumentProcessor
		Map<Object,Object> map = new HashMap<Object,Object>();
		
		// check for file uploads
		String contentType = request.getContentType();
		if( contentType!=null 
				&& contentType.startsWith(FormConstants.ENCTYPE_MULTIPART_FORMDATA)
				&& settings.validateTemporaryStoragePath()==null ) {
			try {
	        	ServletUtils.handleFileUploads(settings,request,map);
			} catch( FileUploadException fue ) {
				// TODO: switch over to an error page and pass an event such that the exception is intelligently communicated
				throw new RuntimeException(fue);
			}
		} else {
			for( Map.Entry<Object,Object> ent : ((Map<Object,Object>)request.getParameterMap()).entrySet() ) {
				if( ent.getKey() instanceof String && ent.getValue() instanceof String[] ) {
					String key = (String)ent.getKey();
					String[] values = new String[((String[])ent.getValue()).length];
					int i = 0;
					for( String val : (String[])ent.getValue() ) {
						values[i++]=val;
					}
					map.put(key,values);
				}
			}
		}
		
		return map;
	}
}
