package jfrn.aljavaportatil.conversor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class FFProbeWrapper {

	public File ffProbeLocation;	

	public FFProbeWrapper() {
		ffProbeLocation = new File("src/main/ffmpeg/ffprobe");
	}

	public FFProbeWrapper(String pathToFFProbe) {
		ffProbeLocation = new File(pathToFFProbe);
	}

	/**
	 * Utiliza o executável ffprobe.exe para retornar a duração do vídeo em segundos
	 * O comando por extenso segue:
	 * path/to/ffprobe -v error -show_entries format=duration -of default=nokey=1:noprint_wrappers=1 >arquivo.extensao<
	 * @param media
	 * @return
	 * @throws IOException
	 */
	public String getMediaDuration(File media) throws IOException {
		int seconds = getMediaDurationInSeconds(media);
		if (seconds != -1)
			return MediaInformationProvider.secondsToFormattedString(seconds);
		else return "Erro ao obter duração";
	}

	public int getMediaDurationInSeconds(File media) {
		try {
			ArrayList<String> params = new ArrayList<String>();
			params.add(ffProbeLocation.getAbsolutePath());
			params.add("-v");
			params.add("error");
			params.add("-show_entries");
			params.add("format=duration");
			params.add("-of");
			params.add("default=nokey=1:noprint_wrappers=1");
			params.add(media.getAbsolutePath());
			ProcessBuilder pb = new ProcessBuilder(params);
			Process ffprobe = pb.start();
			InputStream ffprobeErrorStream = ffprobe.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(ffprobeErrorStream));
			String line;
			while ((line = reader.readLine()) != null) {
				int seconds = Integer.parseInt(line.split(Pattern.quote("."))[0]);
				return seconds;
			}
			return -1;
		} catch (Exception e) {
			return -1;
		}

	}

	/**
	 * Retorna os parâmetros Width x Height como String ([0] width e [1] height)
	 * ffprobe -v error -show_entries stream=width,height \ -of default=noprint_wrappers=1 input.mp4
	 * @param midia
	 * @return
	 * @throws IOException 
	 */
	public int[] getMediaResolutionAsArray(File midia)  {
		ArrayList<String> params = new ArrayList<String>();
		int[] resolution = new int[2];

		params.add(ffProbeLocation.getAbsolutePath());
		params.add("-v");
		params.add("error");
		params.add("-show_entries");
		params.add("stream=width,height");
		params.add("-of");
		params.add("default=nokey=1:noprint_wrappers=1");
		params.add(midia.getAbsolutePath());

		try {
			ProcessBuilder pb = new ProcessBuilder(params);
			Process ffprobe = pb.start();
			InputStream ffprobeErrorStream = ffprobe.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(ffprobeErrorStream));
			String line;
			for (int pos = 0; (line = reader.readLine()) != null; pos++) {
				resolution[pos] = Integer.valueOf(line);
				if (pos == 1) //Pode gerar problemas em raros casos em que vídeos que possuem múltiplos streams (I.E um stream a 480p e outro a 1080p. Torna-se impossível saber a resolução real)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
			resolution[0] = 0;
			resolution[1] = 0;
		}

		return resolution;
	}

	public String getMediaResolutionAsString(File midia) throws IOException {
		int[] resolution = getMediaResolutionAsArray(midia);
		if (resolution[0] == 0 && resolution[1] == 0)
			return "N/A";
		return resolution[0] + "x" + resolution[1];
	}



}
