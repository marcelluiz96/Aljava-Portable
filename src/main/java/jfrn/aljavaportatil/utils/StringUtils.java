package jfrn.aljavaportatil.utils;

import java.util.List;

public class StringUtils {
	
	public static String[] stringListToArray(List<String> params) {
		int paramsSize = params.size();
		String returnString[] = new String[params.size()];

		for (int i = 0; i < paramsSize; i++) 
		{
			returnString[i] = params.get(i);
		}
		
		return returnString;
	}

}
