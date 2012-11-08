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

package com.openmeap.util;

final public class StringUtils {
	private StringUtils() {}
	
	static public String orEmpty(String string) {
		return string==null ? "" : string;
	}
	
	static public boolean isEmpty(String string) {
		return string==null || string.trim().length()==0;
	}
	
	static public String join(String[] parts, String strDelimiter, int startIdx, int endIdx) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for(int i=startIdx; i<endIdx; i++) {
			if(!first) {
				sb.append(strDelimiter);
			} else first = false;
			sb.append(parts[i]);
		}
		return sb.toString();
	}
	
	static public String join(String[] parts, String strDelimiter) {
		return join(parts,strDelimiter,0,parts.length);
	}
	
	static public String[] split(String strString, String strDelimiter)
	{
		int iOccurrences = 0;
		int iIndexOfInnerString = 0;
		int iIndexOfDelimiter = 0;
		int iCounter = 0;

		// Check for null input strings.
		if (strString == null)
		{
			throw new NullPointerException("Input string cannot be null.");
		}
		// Check for null or empty delimiter
		// strings.
		if (strDelimiter.length() <= 0 || strDelimiter == null)
		{
			throw new NullPointerException("Delimeter cannot be null or empty.");
		}

		// If strString begins with delimiter
		// then remove it in
		// order
		// to comply with the desired format.

		if (strString.startsWith(strDelimiter))
		{
			strString = strString.substring(strDelimiter.length());
		}

		// If strString does not end with the
		// delimiter then add it
		// to the string in order to comply with
		// the desired format.
		if (!strString.endsWith(strDelimiter))
		{
			strString += strDelimiter;
		}

		// Count occurrences of the delimiter in
		// the string.
		// Occurrences should be the same amount
		// of inner strings.
		while((iIndexOfDelimiter= strString.indexOf(strDelimiter,iIndexOfInnerString))!=-1)
		{
			iOccurrences += 1;
			iIndexOfInnerString = iIndexOfDelimiter + strDelimiter.length();
		}

		// Declare the array with the correct
		// size.
		String[] strArray = new String[iOccurrences];

		// Reset the indices.
		iIndexOfInnerString = 0;
		iIndexOfDelimiter = 0;

		// Walk across the string again and this
		// time add the
		// strings to the array.
		while((iIndexOfDelimiter= strString.indexOf(strDelimiter,iIndexOfInnerString))!=-1)
		{

			// Add string to
			// array.
			strArray[iCounter] = strString.substring(iIndexOfInnerString, iIndexOfDelimiter);

			// Increment the
			// index to the next
			// character after
			// the next
			// delimiter.
			iIndexOfInnerString = iIndexOfDelimiter + strDelimiter.length();

			// Inc the counter.
			iCounter += 1;
		}
            return strArray;
	}
	
	/**
	 * Got this at {@link http://supportforums.blackberry.com/t5/Java-Development/String-Manipulation-split-replace-replaceAll/ta-p/620038}
	 * 
	 * @param source
	 * @param pattern
	 * @param replacement
	 * @return
	 */
	static final public String replaceAll(String source, String pattern, String replacement)
	{    

	    //If source is null then Stop
	    //and retutn empty String.
	    if (source == null)
	    {
	        return "";
	    }

	    StringBuffer sb = new StringBuffer();
	    //Intialize Index to -1
	    //to check agaist it later 
	    int idx = 0;
	    //Search source from 0 to first occurrence of pattern
	    //Set Idx equal to index at which pattern is found.

	    String workingSource = source;
	    
	    //Iterate for the Pattern till idx is not be -1.
	    while ((idx = workingSource.indexOf(pattern, idx)) != -1)
	    {
	        //append all the string in source till the pattern starts.
	        sb.append(workingSource.substring(0, idx));
	        //append replacement of the pattern.
	        sb.append(replacement);
	        //Append remaining string to the String Buffer.
	        sb.append(workingSource.substring(idx + pattern.length()));
	        
	        //Store the updated String and check again.
	        workingSource = sb.toString();
	        
	        //Reset the StringBuffer.
	        sb.delete(0, sb.length());
	        
	        //Move the index ahead.
	        idx += replacement.length();
	    }

	    return workingSource;
	}
	
	/**
	 * Got this at {@link http://supportforums.blackberry.com/t5/Java-Development/String-Manipulation-split-replace-replaceAll/ta-p/620038}
	 * 
	 * @param source
	 * @param pattern
	 * @param replacement
	 * @return
	 */
	public String replace(String source, String pattern, String replacement)
	{	
	
		//If source is null then Stop
		//and return empty String.
		if (source == null)
		{
			return "";
		}

		StringBuffer sb = new StringBuffer();
		//Intialize Index to -1
		//to check against it later 
		int idx = -1;
		//Intialize pattern Index
		int patIdx = 0;
		//Search source from 0 to first occurrence of pattern.
		//Set Idx equal to index at which pattern is found.
		idx = source.indexOf(pattern, patIdx);
		//If Pattern is found, idx will not be -1 anymore.
		if (idx != -1)
		{
			//append all the string in source till the pattern starts.
			sb.append(source.substring(patIdx, idx));
			//append replacement of the pattern.
			sb.append(replacement);
			//Increase the value of patIdx
			//till the end of the pattern
			patIdx = idx + pattern.length();
			//Append remaining string to the String Buffer.
			sb.append(source.substring(patIdx));
		}
		//Return StringBuffer as a String

                if ( sb.length() == 0)
                {
                    return source;
                }
                else
                {
                    return sb.toString();
                }
	}
}
