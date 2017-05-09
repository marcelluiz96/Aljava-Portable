package jfrn.aljavaportatil.conversor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import jfrn.aljavaportatil.utils.NumberUtils;
import jfrn.aljavaportatil.utils.StringUtils;
import jfrn.aljavaportatil.utils.TaskContainer;


public class FFMpegWrapper {

	private String pathToFFMpeg;
	private static int TOLERANCE_TO_NULLS = 10;
	boolean isComplete = false;
	ProgressListener progressListener;
	Process ffmpeg;
	InputStream ffmpegErrorStream;
	BufferedReader errorStreamReader;
	OutputStream ffmpegOutputStream;

	/**
	 * Construtor utilizado caso a localização do ffmpeg não seja a padrão
	 * @param pathToFFMpeg
	 */
	public FFMpegWrapper(String pathToFFMpeg) {
		this.pathToFFMpeg = pathToFFMpeg;
	}

	public FFMpegWrapper() {
		//Produção e testes
		this.pathToFFMpeg = "ff/ffmpeg.exe";
	}


	//throws IOException, InterruptedException, ExecutionException, TimeoutException
	public void converterMidia(ArrayList<String> params, ProgressListener progressListener)  {
		this.progressListener = progressListener;
		String[] parameters = StringUtils.stringListToArray(params);

		try {
			executeFFmpeg(parameters);

			initializeFFmpegStreams();

			readFFmpegStream();

			if (isComplete) {
				//Enviando o último parâmetro, que é o caminho absoluto para o arquivo
				progressListener.progress(100);
				progressListener.complete(params.get(params.size() - 1)); 
			} else {
				progressListener.error();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void executeFFmpeg(String[] parameters) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		ffmpeg = runtime.exec(parameters); //CONVERSÃO COMEÇA AQUI

		ProcessKiller ffmpegKiller = new ProcessKiller(ffmpeg);
		runtime.addShutdownHook(ffmpegKiller);
	}

	private void initializeFFmpegStreams() {
		ffmpegErrorStream = ffmpeg.getErrorStream();
		errorStreamReader = new BufferedReader(new InputStreamReader(ffmpegErrorStream));
		ffmpegOutputStream = ffmpeg.getOutputStream();
	}

	private void readFFmpegStream() {
		String lineContent;
		int currentNullAmount = 0;
		int currLine = 0;

		for (currLine = 0; true; currLine++) {
			try {
				System.out.println("for  "+ currLine);
				ExecutorService executor = Executors.newSingleThreadExecutor();
				Future<String> future = executor.submit(new TaskContainer(errorStreamReader));

				lineContent = future.get(3, TimeUnit.SECONDS);


				if (lineContent == null || lineContent.isEmpty()) {
					ffmpegOutputStream.write(new String("A").getBytes()); //Escrevendo algo para o ffmpeg destravar (resolvendo caso raro)
					currentNullAmount +=1;

					if(!ffmpeg.isAlive()) {
						isComplete = true;
						break;
					}

					if (currentNullAmount >= TOLERANCE_TO_NULLS) {
						isComplete = false;
						ffmpeg.destroy();
						break;
					}

				} else {
					currentNullAmount = 0;
					System.out.println(lineContent);
					progressListener.message(lineContent);
				}
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
				currentNullAmount +=1;
				if (currentNullAmount >= TOLERANCE_TO_NULLS) {
					isComplete = false;
					ffmpeg.destroy();
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public HashMap<String, String> getFFmpegPropertiesAsHash(String mensagem) {
		HashMap<String, String> properties = new HashMap<String, String>();
		String[] propertiesArray;
		if (mensagem.contains("time")) {
			Iterable<String> results = Splitter.on(CharMatcher.anyOf("= "))
					.trimResults()
					.omitEmptyStrings()
					.split(mensagem);

			List<String> propertiesList = Lists.newArrayList(results);

			if (propertiesList.size() % 2 == 0) {
				for(int i = 0; i < propertiesList.size(); i+= 2) {
					properties.put(propertiesList.get(i), propertiesList.get(i+1));
				}
				return properties;
			}
		}
		return null;
	}


	public ArrayList<String> construirParametrosFfmpegVideo(String filePath, String fileName, String savePath, String bitRate, 
			int width, int height) {

		ArrayList<String> params = new ArrayList<String>(); 

		File ffmpegLocation = new File(pathToFFMpeg);
		String pathToFFmpeg = ffmpegLocation.getAbsolutePath();

		params.add(pathToFFmpeg); //Caminho para o executável
		params.add("-i"); //Caminho do arquivo de origem
		params.add(filePath);
		params.add("-y"); //Substitui arquivos já existentes de mesmo nome

		if (!bitRate.isEmpty() && bitRate != null) {
			params.add("-maxrate");
			params.add(bitRate);
		}

		//Mantendo aspect ratio e definindo resolução do vídeo

		double aspectRatio = NumberUtils.truncateDecimal((double) width / height, 1).doubleValue();

		if (aspectRatio == 1.7) { // 16:9
			params.add("-s");
			params.add("768x432");
		} else if (aspectRatio == 1.6) { // 16:10
			params.add("-s");
			params.add("768x480");
		} else { // 4:3 e qualquer outro
			params.add("-s");
			params.add("640x480");
		}

		params.add("-crf");
		params.add("23");

		params.add("-c:v");
		params.add("libx264");

		params.add(savePath + FilenameUtils.removeExtension(fileName) + " - Convertido.mp4");

		return params;

	}

	public ArrayList<String> construirParametrosFfmpegAudio(String filePath, String fileName, String savePath) {

		ArrayList<String> params = new ArrayList<String>(); 

		File ffmpegLocation = new File(pathToFFMpeg);
		String pathToFFmpeg = ffmpegLocation.getAbsolutePath();

		params.add(pathToFFmpeg); //Caminho para o executável
		params.add("-i"); //Caminho do arquivo de origem
		params.add(filePath);
		params.add("-y"); //Substitui arquivos já existentes de mesmo nome
		params.add(savePath + FilenameUtils.removeExtension(fileName) + " - Convertido.mp3");

		return params;

	}

	public void killFFmpeg() throws IOException {
		if (ffmpeg.isAlive()) {
			ffmpegErrorStream.close();
			ffmpegOutputStream.close();
			errorStreamReader.close();
			ffmpeg.destroyForcibly();
		}
	}

}
