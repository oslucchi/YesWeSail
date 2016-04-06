package com.yeswesail.rest;

public class Constants {
	public static int LNG_IT	= 1;
	public static int LNG_EN	= 2;
	public static int LNG_FR	= 3;
	public static int LNG_DE	= 4;
	public static int LNG_SP	= 5;
	
	public static int getLanguageCode(String language)
	{
		switch(language)
		{
		case "IT":
			return(LNG_IT);

		case "EN":
			return(LNG_EN);

		case "FR":
			return(LNG_FR);

		case "DE":
			return(LNG_DE);

		case "SP":
			return(LNG_SP);

		default:
			return(LNG_EN);
		}
	}
}
