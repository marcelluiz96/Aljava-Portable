package jfrn.aljavaportatil.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import jfrn.aljavaportatil.application.ConverterMidiaApp;
import jfrn.aljavaportatil.application.IntroductionApp;
import jfrn.aljavaportatil.conversor.FFMpegWrapper;
import jfrn.aljavaportatil.conversor.FFProbeWrapper;
import jfrn.aljavaportatil.conversor.MediaInformationProvider;
import jfrn.aljavaportatil.conversor.ProgressListener;
import jfrn.aljavaportatil.model.Midia;
import jfrn.aljavaportatil.utils.DirectoryUtils;
import jfrn.aljavaportatil.utils.NumberUtils;

public class ConverterMidiaController implements Initializable {

	//	private final String formatosPermitidosString = "*.webm, *.mkv, *.flv,*.mp4, *.f4v, *.vob, *.ogv, *.ogg, *.gif, *.avi, *.mov, *.wmv, *.yuv, *.rm, *.rmvb, \n"
	//			+ "*.asf, *.amv, *.m4p, *.m4v, *.mpg, *.mp2, *.mpeg, *.mpe, *.mpv, *.m2v, \n"
	//			+ "*.m4v, *.svi, *.3gp, *.3g2, *.mxf, *.roq, *.nsv, *.flv, *.f4v, *.f4p, *.f4a, *.f4b";

	private final List<String> formatosAudioCompativeis = new ArrayList<String>(Arrays.asList("mp3", "wav", "aac",
			"flac", "wav", "wma", "ogg"));
	private final List<String> formatosVideoCompativeis = new ArrayList<String>(Arrays.asList("mp4", "webm", "mkv", "flv"
			, "vob", "mov", "avi", "wmv", "m4p", "3gp", "mpeg"));

	FFMpegWrapper ffmpegWrapper;
	FFProbeWrapper ffprobeWrapper;
	private ConverterMidiaApp converterMidiaApp;

	int currentFile = 1;
	boolean conversaoConcluida = false;

	private File caminhoDestino; //Caminho do destino 
	private List<Integer> duracaoInputs; //Utilizado para calcular o progresso real dado tempo do video atual / total de tempo dos videos

	@FXML private CheckBox cbSalvarCaminhoPadrao;
	@FXML private Button btEscolherArquivos;
	@FXML private Button btIniciarConversao;
	@FXML private Button btProximaEtapa;
	@FXML private Button btVoltarEtapa;
	@FXML private Button btLimpar;
	@FXML private Button btVoltarMenuInicial;
	@FXML private Button btAbrirLocalArquivos;

	@FXML private TextField txOutputPath;

	//Propriedades do status da conversão (output do ffmpeg)
	@FXML private Label lbBitRate;
	@FXML private Label lbSize;
	@FXML private Label lbDrop;
	@FXML private Label lbSpeed;
	@FXML private ProgressBar pbProgressoConversao;
	@FXML private ProgressBar pbProgressoConversaoArquivoAtual;
	@FXML private Label lbFormatosCompativeis;
	@FXML private Label lbConversaoConcluida;
	@FXML private Label lbNomeArquivoAtual;
	@FXML private Label lbArquivoAtual;
	@FXML private Label lbTempoTotalArquivos;
	@FXML private Label lbTempoTotalArquivoAtual;
	@FXML private Label lbTempoAtualArquivos;
	@FXML private Label lbTempoAtualArquivoAtual;


	@FXML private TableView<Midia> tvMidiasAConverter;
	@FXML private TableColumn<Midia, String> colNome;
	@FXML private TableColumn<Midia, String> colCaminho;
	@FXML private TableColumn<Midia, String> colTamanho;
	@FXML private TableColumn<Midia, String> colDuracao;
	@FXML private TableColumn<Midia, String> colResolucao;
	@FXML private TableColumn<Midia, String> colTipo;
	@FXML private TableColumn<Midia, String> colAcoes;

	@FXML private TabPane tabPane;

	private String previousOutputPath;


	public void initialize(URL location, ResourceBundle resources) {

		initializeTableColumns();
		initializeTooltips();
		initializeListeners();
		configureView();
	}

	private void configureView() {
		lbConversaoConcluida.setVisible(false);
		btAbrirLocalArquivos.setVisible(false);
		lbFormatosCompativeis.setText("Vídeo: "+ formatosVideoCompativeis
				+ "\n Áudio: " + formatosAudioCompativeis);
	}

	private void initializeTableColumns() {
		colNome.setCellValueFactory(cellData -> cellData.getValue().getNome());
		colCaminho.setCellValueFactory(cellData -> cellData.getValue().getCaminho());
		colTamanho.setCellValueFactory(cellData -> cellData.getValue().getTamanho());
		colDuracao.setCellValueFactory(cellData -> cellData.getValue().getDuracao());
		colResolucao.setCellValueFactory(cellData -> cellData.getValue().getResolucao());
		colTipo.setCellValueFactory(cellData -> cellData.getValue().getTipo());
		colAcoes.setCellFactory( createActionCellValueFactory() ); //Ação importante nesse método para adicionar botões dinamicamente
	}

	private void initializeTooltips() {
		Tooltip ttFormatosPermitidos = new Tooltip();
		ttFormatosPermitidos.setText("Vídeo: "+ formatosVideoCompativeis
				+ "\n Áudio: " + formatosAudioCompativeis);
		hackTooltipStartTiming(ttFormatosPermitidos);
		btEscolherArquivos.setTooltip(ttFormatosPermitidos);
	}

	private void initializeListeners() {
		//tableViewListener
		tvMidiasAConverter.setRowFactory(new Callback<TableView<Midia>, TableRow<Midia>>() {  
			@Override  
			public TableRow<Midia> call(TableView<Midia> tableView) {  
				final TableRow<Midia> row = new TableRow<>();  
				final ContextMenu contextMenu = new ContextMenu();  
				final MenuItem removeMenuItem = new MenuItem("Remover");  
				removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {  
					@Override  
					public void handle(ActionEvent event) {  
						tvMidiasAConverter.getItems().remove(row.getItem());  
					}  
				});  
				contextMenu.getItems().add(removeMenuItem);  
				// Set context menu on row, but use a binding to make it only show for non-empty rows:  
				row.contextMenuProperty().bind(  
						Bindings.when(row.emptyProperty())  
						.then((ContextMenu)null)  
						.otherwise(contextMenu)  
						);  
				return row;  
			}  
		});
	}

	@FXML 
	private void actionEscolherArquivos() {
		FileChooser fileChooser = new FileChooser();

		fileChooser.setTitle("Escolha o arquivo");
		List<String> formatosPermitidos = new ArrayList<String>();
		formatosPermitidos.addAll(formatosVideoCompativeis);
		formatosPermitidos.addAll(formatosAudioCompativeis);

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Áudio/Vídeo: ("+String.join(", ", formatosPermitidos)+")"
				, "*.mp4", "*.webm", "*.mkv", "*.flv"
				, "*.vob", "*.mov", "*.avi", "*.wmv"
				,"*.m4p", "*.3gp", "*.mpeg", "*.mp3"
				, "*.wav", "*.aac","*.flac", "*.wav"
				, "*.wma", "*.ogg"); 

		fileChooser.getExtensionFilters().add(extFilter);

		List<File> arquivosEscolhidos = fileChooser.showOpenMultipleDialog(btEscolherArquivos.getScene().getWindow()); 

		if (arquivosEscolhidos != null) {
			MediaInformationProvider mediaInfoProvider = new MediaInformationProvider();
			for (File midia : arquivosEscolhidos) {
				try {
					converterMidiaApp.getMidiasAConverter().add(new Midia (midia.getName(),
							midia.getAbsolutePath(),
							MediaInformationProvider.bytesToMB(midia.length()) + " Mb",
							mediaInfoProvider.getMediaDuration(midia),
							mediaInfoProvider.getMediaResolutionAsString(midia),
							MediaInformationProvider.getFileType(midia),
							midia));
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
			tvMidiasAConverter.setItems(converterMidiaApp.getMidiasAConverter());
		}
		btIniciarConversao.setDisable(false);
	}

	@FXML
	private void actionEscolherDestino() {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Escolha o diretório de destino dos arquivos");
		dirChooser.setInitialDirectory(getUserDirectory());

		File selectedDirectory = dirChooser.showDialog(btEscolherArquivos.getScene().getWindow());

		if (selectedDirectory != null)
			txOutputPath.setText(selectedDirectory.getAbsolutePath() + File.separator);
	}

	@FXML
	private void actionProximaEtapa() {
		tabPane.getSelectionModel().selectNext();
	}

	@FXML
	private void actionVoltarEtapa() {
		tabPane.getSelectionModel().selectPrevious();
	}

	@FXML
	private void actionLimpar() {

	}

	@FXML
	private void actionVoltarMenuInicial() throws Exception {
		if (ffmpegWrapper != null) 
			ffmpegWrapper.killFFmpeg();

		new IntroductionApp().start(new Stage());
		Stage stage = (Stage) txOutputPath.getScene().getWindow();
		stage.close();
	}

	@FXML
	private void actionIniciarConversao() throws IOException {

		this.tabPane.getSelectionModel().selectNext();
		ffmpegWrapper = new FFMpegWrapper();
		ffprobeWrapper = new FFProbeWrapper();
		computarTempoTotalConversao();
		ProgressListener progressListener = createProgressListener();
		DirectoryUtils.createDirIfNonExistant(txOutputPath.getText());

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				for (Midia midiaAtual : tvMidiasAConverter.getItems()) {
					Platform.runLater(() -> updateCurrentFileInfo(midiaAtual));
					handleConversao(midiaAtual, progressListener);
					currentFile++;
				}
				return null;
			}
		};
		Thread conversorThread = new Thread(task);
		conversorThread.setDaemon(true); //Se main fechar, daemon threads também fecham
		conversorThread.start();
	}



	private void onConversaoConcluida() {
		pbProgressoConversao.setProgress(1);
		pbProgressoConversaoArquivoAtual.setProgress(1);

		showDialog(AlertType.INFORMATION, 
				"Conversão concluída", 
				"A conversão foi concluída com sucesso");

		tabPane.getTabs().get(0).setDisable(true);
		tabPane.getTabs().get(1).setDisable(true);
		lbConversaoConcluida.setVisible(true);
		btAbrirLocalArquivos.setVisible(true);
	}

	private void onErroConversao() {
		String fileConversionStatus = new String();
		for (int i = 0; i < getArquivosAConverter().size(); i++) {
			if (i < currentFile - 1) {
				fileConversionStatus.concat("Convertido: " + getArquivosAConverter().get(i).getName() + "\n");
			} else {
				fileConversionStatus.concat("NÃO convertido: " + getArquivosAConverter().get(i).getName() + "\n");
			}
		}

		showDialog(AlertType.ERROR,
				"Ocorreu um erro durante a conversão do arquivo " 
						+ getArquivosAConverter().get(currentFile - 1).getName()
						,fileConversionStatus
				);

	}

	private void updateProgressBars(String time) {
		int segundosConversaoAtual = NumberUtils.timeStringToSeconds(time);
		int duracaoArquivoAtual = NumberUtils.timeStringToSeconds(lbTempoTotalArquivoAtual.getText());
		double progressoAtual = segundosConversaoAtual / (double) duracaoArquivoAtual;

		pbProgressoConversaoArquivoAtual.setProgress(progressoAtual);

		int segundosSomaDasConversoes = 0;
		int duracaoTotal = NumberUtils.timeStringToSeconds(lbTempoTotalArquivos.getText());
		int indexArquivosConcluidos = currentFile - 1;
		for (int i = 0; i < indexArquivosConcluidos; i++) {
			segundosSomaDasConversoes += ffprobeWrapper.getMediaDurationInSeconds(getArquivosAConverter().get(i));
		}
		segundosSomaDasConversoes += segundosConversaoAtual;
		double progressoTotal = segundosSomaDasConversoes / (double) duracaoTotal;

		pbProgressoConversao.setProgress(progressoTotal);
	}

	@FXML
	private void actionAbrirLocalArquivos() {

		try {
			Runtime.getRuntime().exec("explorer.exe /root," + txOutputPath.getText());
			new IntroductionApp().start(new Stage());
			Stage stage = (Stage) txOutputPath.getScene().getWindow();
			stage.close();
		} catch (IOException e) {
			showDialog(AlertType.ERROR, "Erro ao tentar abrir local do arquivo",
					"Não foi possível abrir o local do arquivo. Por favor, verifique se o caminho"
					+ "realmente existe");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			showDialog(AlertType.ERROR, "Erro ao voltar ao menu inicial",
					"Por favor, reinicie o programa e verifique se o problema persiste.");
		}
	}

	@FXML
	private void actionSalvarCaminhoPadrao() {
		if (cbSalvarCaminhoPadrao.isSelected()) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			String path = System.getProperty("user.home") 
					+ File.separator + "Documents"
					+ File.separator + "aljava-portatil"
					+ File.separator + "Minhas Conversões "
					+ sdf.format(new Date())
					+ File.separator;

			previousOutputPath = txOutputPath.getText();
			txOutputPath.setText(path);
		} else {
			txOutputPath.setText(previousOutputPath);
		}
	}

	private void computarTempoTotalConversao() {
		int totalTime = 0;
		for(File midia : getArquivosAConverter()) {
			totalTime += ffprobeWrapper.getMediaDurationInSeconds(midia);
		}

		lbTempoTotalArquivos.setText(MediaInformationProvider.
				secondsToFormattedString(totalTime));
	}

	/**
	 * Updates the total time, current filename, and "current file" text
	 * @param midiaAtual
	 */
	private void updateCurrentFileInfo (Midia midiaAtual) {
		lbTempoTotalArquivoAtual.setText(midiaAtual.getDuracao().get());
		lbNomeArquivoAtual.setText(midiaAtual.getNome().get());
		lbArquivoAtual.setText(currentFile + " de " + getArquivosAConverter().size());
	}

	private void handleConversao(Midia midiaAtual, ProgressListener progressListener) {
		File arquivoConversaoAtual = new File(midiaAtual.getCaminho().get());
		int[] resolution = ffprobeWrapper.getMediaResolutionAsArray(arquivoConversaoAtual);

		ArrayList<String> params = new ArrayList<String>();
		String extensaoMidia = FilenameUtils.getExtension(midiaAtual.getArquivo().getAbsolutePath());
		if (formatosVideoCompativeis.contains(extensaoMidia.toLowerCase())) {
			params = ffmpegWrapper.construirParametrosFfmpegVideo(
					arquivoConversaoAtual.getAbsolutePath(),
					arquivoConversaoAtual.getName(),
					txOutputPath.getText(),
					"192000",
					resolution[0],
					resolution[1]);
		} else if (formatosAudioCompativeis.contains(extensaoMidia.toLowerCase())) {
			params = ffmpegWrapper.construirParametrosFfmpegAudio(
					arquivoConversaoAtual.getAbsolutePath(),
					arquivoConversaoAtual.getName(),
					txOutputPath.getText());
		}

		if (!params.isEmpty())
			ffmpegWrapper.converterMidia(params, progressListener);
		else {
			showDialog(AlertType.ERROR, "Não foi possível construir os parâmetros da conversão",
					"Favor contatar a seção de sistemas do NTI - JFRN para apuração do problema.");
		}
	}

	private ProgressListener createProgressListener() {
		return new ProgressListener() {

			public void progress(int arquivoAtual, int porcentagem) {
				Platform.runLater ( () ->pbProgressoConversao.setProgress(porcentagem /(double) 100));
			}

			@Override
			public void progress(int porcentagem) {
				Platform.runLater ( () ->pbProgressoConversaoArquivoAtual.setProgress(porcentagem /(double) 100));

			}

			@Override
			public void message(String mensagem) {		
				Platform.runLater ( () -> lbBitRate.setText(mensagem));
				Platform.runLater(() -> updateStatusProperties(mensagem));
			}

			@Override
			public void complete(String pathToConvertedFile) {
				File arquivoConvertido = new File(pathToConvertedFile);
				if (MediaInformationProvider.getFileType(arquivoConvertido).contains("audio")) {
					configurarMetaDadosAudio(arquivoConvertido);
				}
				if (currentFile == getArquivosAConverter().size()) {
					Platform.runLater(() -> onConversaoConcluida());
				}
			}

			@Override
			public void error() {
				Platform.runLater(() -> onErroConversao());
			}
		};
	}

	public void configurarMetaDadosAudio(File convertedFile) {
		try {
			Mp3File mp3File = new Mp3File(convertedFile);
			ID3v1 id3v1Tag;
			if (mp3File.hasId3v1Tag()) {
				id3v1Tag =  mp3File.getId3v1Tag();
			} else {
				id3v1Tag = new ID3v1Tag();
				mp3File.setId3v1Tag(id3v1Tag);
			}
			//TODO Substituir por uma chave encriptada baseada em alguma propriedade do arquivo
			id3v1Tag.setComment("convertido");

			String originalFilePath = convertedFile.getAbsolutePath();
			String destinyFilePath = FilenameUtils.removeExtension(originalFilePath) + " processado.mp3";

			mp3File.save(destinyFilePath);
			convertedFile.delete();
			File finalFile = new File(destinyFilePath);
			finalFile.renameTo(convertedFile);


			//			String pathToMp3File = file.getParent() + "tempFile.mp3";
			//			mp3File.save(pathToMp3File);
			//			
			//			String pathToDestinyFile = file.getAbsolutePath();
			//			file.delete();
			//			
			//			FileUtils.copyFileToDirectory(new File(pathToMp3File), new File(pathToDestinyFile), true);
		} catch (UnsupportedTagException | InvalidDataException | IOException | NotSupportedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates all messages in the status bar by splitting the message output from FFMPEG's Stream
	 * Updates the progress bars also
	 * @param mensagem
	 */
	private void updateStatusProperties(String mensagem) {
		try {
			HashMap<String, String> properties = ffmpegWrapper.getFFmpegPropertiesAsHash(mensagem);
			if (properties != null) {
				if (properties.containsKey("size"))
					lbSize.setText("Tamanho: " + properties.get("size"));
				if (properties.containsKey("bitrate"))
					lbBitRate.setText("Bitrate: " + properties.get("bitrate"));
				if (properties.containsKey("drop"))
					lbDrop.setText("Perda: " + properties.get("drop"));
				if (properties.containsKey("speed"))
					lbSpeed.setText("Velocidade: " + properties.get("speed"));
				if (properties.containsKey("time")) {
					lbTempoAtualArquivoAtual.setText(properties.get("time"));
					updateProgressBars(properties.get("time"));
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	/**
	 * Método utilizado para customizar o delay até o aparecimento de uma tooltip
	 * @param tooltip
	 */
	public static void hackTooltipStartTiming(Tooltip tooltip) {
		try {
			Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
			fieldBehavior.setAccessible(true);
			Object objBehavior = fieldBehavior.get(tooltip);

			Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
			fieldTimer.setAccessible(true);
			Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

			objTimer.getKeyFrames().clear();
			objTimer.getKeyFrames().add(new KeyFrame(new Duration(250)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Callback<TableColumn<Midia, String>, TableCell<Midia, String>> createActionCellValueFactory() {

		return new Callback<TableColumn<Midia, String>, TableCell<Midia, String>>()
		{
			@Override
			public TableCell call( final TableColumn<Midia, String> param )
			{
				TableCell<Midia, String> cell = new TableCell<Midia, String>()
				{

					Button btExcluir = new Button( "Remover");

					@Override
					public void updateItem( String item, boolean empty )
					{
						super.updateItem( item, empty );
						if ( empty )
						{
							setGraphic( null );
							setText( null );
						}
						else
						{
							btExcluir.setOnAction( ( ActionEvent event ) ->
							{
								Midia midia = getTableView().getItems().remove( getIndex());
								System.out.println("Removendo "+ midia.getNome() + "   " + midia.getResolucao() );
							} );
							setGraphic( btExcluir );
							setText( null );
						}
					}
				};
				return cell;
			}
		};
	}

	public void showDialog(AlertType type, String header, String content) {
		Alert alert = new Alert(type);

		switch (type) {
		case ERROR:
			alert.setTitle("Erro");
			break;
		case INFORMATION:
			alert.setTitle("Informação");
			break;
		case CONFIRMATION:
			alert.setTitle("Sucesso");
			break;
		case WARNING:
			alert.setTitle("Aviso");
			break;
		}
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	public File getUserDirectory() {
		String userDirectoryString = System.getProperty("user.home");
		File userDirectory = new File(userDirectoryString);
		if(!userDirectory.canRead()) {
			userDirectory = new File("c:/");
		}
		return userDirectory;
	}

	public List<File> getArquivosAConverter() {
		return tvMidiasAConverter.getItems().stream().map(Midia::getArquivo).collect(Collectors.toList());
	}


	public ConverterMidiaApp getConverterMidiaApp() {
		return converterMidiaApp;
	}

	public void setConverterMidiaApp(ConverterMidiaApp converterMidiaApp) {
		this.converterMidiaApp = converterMidiaApp;
	}

	public Button getBtEscolherArquivos() {
		return btEscolherArquivos;
	}

	public void setBtEscolherArquivos(Button btEscolherArquivos) {
		this.btEscolherArquivos = btEscolherArquivos;
	}

	public Button getBtIniciarConversao() {
		return btIniciarConversao;
	}

	public void setBtIniciarConversao(Button btIniciarConversao) {
		this.btIniciarConversao = btIniciarConversao;
	}
	public TextField getTxOutputPath() {
		return txOutputPath;
	}

	public void setTxOutputPath(TextField txOutputPath) {
		this.txOutputPath = txOutputPath;
	}


}
