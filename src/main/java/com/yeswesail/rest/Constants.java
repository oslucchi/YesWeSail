package com.yeswesail.rest;

public class Constants {
	public static final int LNG_IT	= 1;
	public static final int LNG_EN	= 2;
	public static final int LNG_FR	= 3;
	public static final int LNG_DE	= 4;
	public static final int LNG_ES	= 5;
	
	public static int getLanguageCode(String language)
	{
		switch(language.substring(0,2).toUpperCase())
		{
		case "IT":
			return(LNG_IT);

		case "EN":
			return(LNG_EN);

		case "FR":
			return(LNG_FR);

		case "DE":
			return(LNG_DE);

		case "ES":
			return(LNG_ES);

		default:
			return(LNG_EN);
		}
	}
}
