package jfrn.aljavaportatil.utils;

import java.io.File;

public class DirectoryUtils {
	
	public static void createDirIfNonExistant(String outputPath) {
		//Criando pasta do caminho de destino, caso não exista
		if (!(new File(outputPath).exists())) {
			new File(outputPath).mkdirs();
		}
	}

}
