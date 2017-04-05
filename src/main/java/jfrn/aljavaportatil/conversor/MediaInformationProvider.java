package jfrn.aljavaportatil.conversor;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import jfrn.aljavaportatil.model.Midia;

/**
 * Classe baseada em métodos do Aljava-Web, aqui incorporados para obter informações das mídias
 * Extende a classe FFProbeWrapper para reduzir a necessidade de criação de objetos
 * @author marcel
 *
 */
public class MediaInformationProvider extends FFProbeWrapper{
	
	public static double bytesToMB(Object tamanho) {

		double size;
		if (tamanho instanceof Integer) {
			int i = (int) tamanho;
			size = ((double) (i / 100)) * 100;
		} else {
			size = ((Long) tamanho).doubleValue();
		}

		size = size / (1024 * 1024);
		BigDecimal bd = new BigDecimal(size);
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		double db = Double.parseDouble(bd.toPlainString());
		return db;
	}
	
	public static String bytesToMBAsString(Object tamanho) {
		double size = bytesToMB(tamanho);
		return Double.toString(size);
	}
	
	public static String secondsToFormattedString(int segundos) {
		int hours = segundos / 3600;
		int minutes = (segundos % 3600) / (60);
		int seconds = segundos % 60;
		return String.format("%02d:%02d:%02d",hours, minutes, seconds);
	}
	
	public static String getFileType(File midia) {
		MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		Collection<?> mimeTypes = MimeUtil.getMimeTypes(midia);
		
		MimeType m = MimeUtil.getFirstMimeType(mimeTypes.toString());
		if (m.getMediaType().equals("video"))
			return "video";
		else if (m.getMediaType().equals("audio"))
			return "audio";
		else
			return "";
	}

}
