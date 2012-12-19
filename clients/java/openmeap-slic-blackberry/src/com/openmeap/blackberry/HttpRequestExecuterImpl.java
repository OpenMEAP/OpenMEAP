/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2013 OpenMEAP, Inc.                                   #
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

package com.openmeap.blackberry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;
import javax.microedition.io.InputConnection;
import javax.microedition.io.OutputConnection;

import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.io.transport.options.TcpCellularOptions;
import net.rim.device.api.util.Arrays;

import com.openmeap.constants.FormConstants;
import com.openmeap.digest.DigestException;
import com.openmeap.digest.DigestInputStream;
import com.openmeap.digest.DigestInputStreamFactory;
import com.openmeap.http.HttpHeader;
import com.openmeap.http.HttpRequestException;
import com.openmeap.http.HttpRequestExecuter;
import com.openmeap.http.HttpResponse;
import com.openmeap.http.HttpResponseImpl;
import com.openmeap.util.GenericRuntimeException;
import com.openmeap.util.Utils;

/**
 * 
 * @author Schang
 */
public class HttpRequestExecuterImpl implements HttpRequestExecuter {
	
	public HttpResponse postContent(String url, String content, String contentType) throws HttpRequestException {
		
		try {
			return makeRequest(url,content.getBytes(FormConstants.CHAR_ENC_DEFAULT),new HttpHeader[]{new HttpHeader(FormConstants.CONTENT_TYPE,contentType)});
		} catch (UnsupportedEncodingException e) {
			throw new HttpRequestException(e);
		}
	}

	public HttpResponse postData(String url, Hashtable params) throws HttpRequestException {
		
		try {
			return makeRequest(url,Utils.createParamsString(params).getBytes(FormConstants.CHAR_ENC_DEFAULT),null);
		} catch (UnsupportedEncodingException e) {
			throw new HttpRequestException(e);
		}
	}

	public HttpResponse postData(String url, Hashtable urlParams, Hashtable postParams) throws HttpRequestException {
		
		try {
			return postData(url+FormConstants.QUERY_STRING_DELIM+Utils.createParamsString(urlParams),postParams);
		} catch (UnsupportedEncodingException e) {
			throw new HttpRequestException(e);
		}
	}

	public HttpResponse get(String url) throws HttpRequestException {
		return makeRequest(url,null,null);
	}

	public HttpResponse get(String url, Hashtable params) throws HttpRequestException {
		
		try {
			return makeRequest(url,Utils.createParamsString(params).getBytes(FormConstants.CHAR_ENC_DEFAULT),null);
		} catch (UnsupportedEncodingException e) {
			throw new HttpRequestException(e);
		}
	}

	public void shutdown() {

	}

	private HttpResponse makeRequest(String url, byte[] postData, HttpHeader[] headers) throws HttpRequestException {
		
		String forwardUrl = null;
		
		int[] intTransports = {
				TransportInfo.TRANSPORT_TCP_WIFI,
				TransportInfo.TRANSPORT_WAP2,
				TransportInfo.TRANSPORT_TCP_CELLULAR
			};
		
		// Remove any transports that are not currently available.
        for(int i = 0; i < intTransports.length ; i++)
        {
            int transport = intTransports[i];
            if(!TransportInfo.isTransportTypeAvailable(transport)
                  || !TransportInfo.hasSufficientCoverage(transport))
            {
                Arrays.removeAt(intTransports, i);
            }
        }
		
		TcpCellularOptions tcpOptions = new TcpCellularOptions();
		
		final ConnectionFactory factory = new ConnectionFactory();
		if(intTransports.length > 0) {
            factory.setPreferredTransportTypes(intTransports);
        }
		factory.setTransportTypeOptions(TransportInfo.TRANSPORT_TCP_CELLULAR, tcpOptions);
		factory.setAttemptsLimit(1);
		factory.setTimeoutSupported(true);
		factory.setTimeLimit(5000);
		
		HttpResponseImpl response = new HttpResponseImpl();
        ConnectionDescriptor cd = factory.getConnection(url);
        if(cd==null) {
        	throw new HttpRequestException("Could not establish connection to "+url);
        }
    	HttpConnection c = (HttpConnection)cd.getConnection();
    	
        OutputConnection oc = (OutputConnection) c;
        InputConnection ic = (InputConnection) c;
        
        OutputStream os = null;
        InputStream is = null;
        
        try {
        		            
            if(postData==null) {
            	os = makeGETRequest(url,headers,c);
            } else {
            	os = makePOSTRequest(url,headers,postData,c);
            }
            
            is = ic.openInputStream();
            int i=0;
            HttpHeader[] responseHeaders = new HttpHeader[0];
            while(true) {
        		String key = c.getHeaderFieldKey(i);
        		if(key==null) {
        			break;
        		}
        		String value = c.getHeaderField(i);
        		Arrays.add(responseHeaders, new HttpHeader(key,value));
            	i++;
            }
            response.setStatusCode(c.getResponseCode());
            response.setHeaders(responseHeaders);
            response.setResponseBody(is);
            response.setContentLength(c.getLength());
            
            // see if we need to handle a redirect
            if(response.getStatusCode()==301 || response.getStatusCode()==303) {
            	for(i=0;i<responseHeaders.length;i++) {
            		HttpHeader header = responseHeaders[i];
            		if( header.getKey().equalsIgnoreCase("location") ) {
            			forwardUrl = header.getValue();
            			break;
            		}
            	}
            }
            
        } catch(Exception e) {
        	throw new HttpRequestException(e);
        } finally {
        	if(os!=null) {
        		try {
					os.close();
				} catch (IOException e) {
					throw new GenericRuntimeException(e);
				}
        	}
        }

        if(forwardUrl==null) {
        	return response;
        } else {
       		try {
				response.getResponseBody().close();
			} catch (IOException e) {
				throw new GenericRuntimeException();
			}
       		return makeRequest(forwardUrl,postData,headers);
        }        
	}
	
	private OutputStream makePOSTRequest(String url, HttpHeader[] headers, byte[] postData, HttpConnection c) throws IOException, DigestException, HttpRequestException {
		
		c.setRequestMethod(HttpConnection.POST);
    	writeHeaders(headers,c);
		c.setRequestProperty(FormConstants.CONTENT_LENGTH,String.valueOf(postData.length));
		c.setRequestProperty(FormConstants.CONTENT_MD5,getMd5OfPostData(postData));
		c.setRequestProperty(FormConstants.CONTENT_TYPE, FormConstants.CONT_TYPE_DEFAULT);
		OutputStream os = c.openOutputStream();
        os.write(postData);
        os.flush();
        return os;
	}
	
	private OutputStream makeGETRequest(String url, HttpHeader[] headers, HttpConnection c) throws IOException {
		
		c.setRequestMethod(HttpConnection.GET);
		OutputStream os = c.openOutputStream();
        os.flush();
        return os;
	}
	
	private void writeHeaders(HttpHeader[] headers, HttpConnection c) throws IOException {
		if(headers!=null) {
	        for(int i=0; i<headers.length; i++) {
	        	HttpHeader header = headers[i];
	        	c.setRequestProperty(header.getKey(),header.getValue());
	        }	           
		}
	}
	
	private String getMd5OfPostData(byte[] postData) throws HttpRequestException {
		try
		{
			DigestInputStream md = DigestInputStreamFactory.getDigestInputStream("md5");
	    	md.setInputStream(new ByteArrayInputStream(postData));
			byte[] md5Sum;
			md5Sum = md.digest();
		    byte[] encoded = Base64OutputStream.encode(md5Sum,0,md5Sum.length,false,false);
		    return new String(encoded, FormConstants.CHAR_ENC_DEFAULT);
		} catch (Exception ioe) {
			throw new HttpRequestException(ioe);
		}
	}
	
	private void writeString(String string, OutputStream os) throws UnsupportedEncodingException, IOException {
		os.write(string.getBytes(FormConstants.CHAR_ENC_DEFAULT));
	}
}
