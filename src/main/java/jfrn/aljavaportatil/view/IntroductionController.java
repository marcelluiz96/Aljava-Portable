package jfrn.aljavaportatil.view;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfrn.aljavaportatil.application.ConverterMidiaApp;

public class IntroductionController implements Initializable{


	public IntroductionController() {	}

	private Application IntroductionApp;

	@FXML private AnchorPane anchorPane;

	@FXML Button btConverterAudioVideo;

	@FXML
	public void initialize() {

	}

	@FXML
	public void onConverterAudioVideoClick(ActionEvent event) {
		try {
			new ConverterMidiaApp().start(new Stage());
			Stage stage = (Stage) btConverterAudioVideo.getScene().getWindow();
			stage.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	public void onComprimirPdfClick(ActionEvent event) {

	}

	@FXML
	public void onAssinarPdfClick(ActionEvent event) {

	}

	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

	}

	public Application getIntroductionApp() {
		return IntroductionApp;
	}

	public void setIntroductionApp(Application introductionApp) {
		IntroductionApp = introductionApp;
	}

	public AnchorPane getAnchorPane() {
		return anchorPane;
	}

	public void setAnchorPane(AnchorPane anchorPane) {
		this.anchorPane = anchorPane;
	}

	public Button getBtConverterAudioVideo() {
		return btConverterAudioVideo;
	}

	public void setBtConverterAudioVideo(Button btConverterAudioVideo) {
		this.btConverterAudioVideo = btConverterAudioVideo;
	}



}
