package com.yeswesail.rest.users;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class TaxcodeChecker {
	public static final int PERSONAL = 1;
	public static final int COMPANY = 2;
	public static final Logger log = Logger.getLogger(TaxcodeChecker.class);
	
	private static boolean checkTaxcodePersonalIT(String taxcode)
	{
	    int i, s, c;
	    int setdisp[] = {1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20,
	        11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23 };
	    if( taxcode.length() != 16 )
	    {
	    	log.warn("The taxcode has an invalid lenght (" + taxcode + ")");
	        return false;
	    }
	    taxcode = taxcode.toUpperCase();
	    if (!StringUtils.isAlphanumeric(taxcode))
	    {
	    	log.warn("The taxcode contains invalid characters (" + taxcode + ")");
	        return false;
	    }
	    s = 0;
	    for(i = 1; i <= 13; i += 2)
	    {
	        c = taxcode.charAt(i);
	        if(c >= '0' && c <= '9')
	            s = s + c - '0';
	        else
	            s = s + c - 'A';
	    }
	    for(i = 0; i <= 14; i += 2)
	    {
	        c = taxcode.charAt(i);
	        if( c>='0' && c<='9' )     c = c - '0' + 'A';
	        s = s + setdisp[c - 'A'];
	    }
	    if (s % 26 + 'A' != taxcode.charAt(15))
	    {
	    	log.warn("The checksum char is incorrect (" + taxcode + ")");
	        return false;
	    }
	    return true;
	}
	
	private static boolean checkTaxcodeCompanyIT(String taxcode)
	{
	    int i, c, s;
	    if( taxcode.length() != 11 )
	    {
	    	log.warn("The taxcode has an invalid lenght (" + taxcode + ")");
	        return false;
	    }
	    if (!StringUtils.isAlphanumeric(taxcode))
	    {
	    	log.warn("The taxcode contains invalid characters (" + taxcode + ")");
	        return false;
	    }
	    s = 0;
	    for(i = 0; i <= 9; i += 2)
	        s += taxcode.charAt(i) - '0';
	    for(i = 1; i <= 9; i += 2)
	    {
	        c = 2 * (taxcode.charAt(i) - '0');
	        if( c > 9 )
	        	c = c - 9;
	        s += c;
	    }
	    if ((10 - s % 10) % 10 != taxcode.charAt(10) - '0')
	    {
	    	log.warn("The checksum char is incorrect (" + taxcode + ")");
	        return false;
	    }
	    return true;
	}

	public static boolean checkTaxcode(String countryCode, String taxcode, int type)
	{
		switch(countryCode.toUpperCase())
		{
		case "IT":
			if (type == PERSONAL)
				return checkTaxcodePersonalIT(taxcode);
			else if (type == COMPANY)
				return checkTaxcodeCompanyIT(taxcode);
			return false;
			
		default:
			return true;
		}
	}
}
