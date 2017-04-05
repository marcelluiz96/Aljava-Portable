package jfrn.aljavaportatil.utils;

import java.math.BigDecimal;

public class NumberUtils {
	
	public static BigDecimal truncateDecimal(double x,int numberofDecimals)
	{
	    if ( x > 0) {
	        return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
	    } else {
	        return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
	    }
	}
	
	public static int timeStringToSeconds(String time) {
		int resultingTime = 0;

		//Removing dots representing milliseconds
		String[] splitString = time.split("\\.");
		if (splitString.length == 0)
			splitString = time.split(":"); // If doesnt contain dots, splits using :
		else
			splitString = splitString[0].split(":"); //if contains, get everything before the dot, and splits using :
		
		if (splitString.length == 3) { // If is in the format HH:mm:ss
			resultingTime += Integer.valueOf(splitString[0]) * 3600;
			resultingTime += Integer.valueOf(splitString[1]) * 60;
			resultingTime += Integer.valueOf(splitString[2]);
		} else if (splitString.length == 2) { //if it's mm:ss
			resultingTime += Integer.valueOf(splitString[0]) * 60;
			resultingTime += Integer.valueOf(splitString[1]);
		} else if (splitString.length == 1) { //Shouldn't ever come to this unless this method is being incorrectly called
			resultingTime += Integer.valueOf(splitString[0]);
		}
		
		
		return resultingTime;
		
	}

}
