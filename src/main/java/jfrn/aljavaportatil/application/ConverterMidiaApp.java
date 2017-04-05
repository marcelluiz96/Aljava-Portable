package jfrn.aljavaportatil.application;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfrn.aljavaportatil.model.Midia;
import jfrn.aljavaportatil.view.ConverterMidiaController;

public class ConverterMidiaApp  extends Application {
	
	private Stage stage;
	private TabPane rootLayout;
	
	private ObservableList<Midia> midiasAConverter = FXCollections.observableArrayList();
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		stage.setTitle("Utilitário portátil Aljava - JFRN v1.0");
		stage.setResizable(false);

//		this.stage.getIcons().add(new Image("file:resources/images/NetflixLogo.png"));
		//this makes all stages close and the app exit when the main stage is closed
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		initRootLayout();
	}
	
	
	private void initRootLayout() throws IOException {
		FXMLLoader loader = new FXMLLoader();
				loader.setLocation(ConverterMidiaApp.class.getResource("/jfrn/aljavaportatil/view/ConverterMidiaView.fxml"));
		rootLayout = (TabPane) loader.load();
		
		Scene scene = new Scene(rootLayout);
		stage.setScene(scene);

		ConverterMidiaController controller = loader.getController();
		controller.setConverterMidiaApp(this);

		stage.show();
	}


	public Stage getStage() {
		return stage;
	}


	public void setStage(Stage stage) {
		this.stage = stage;
	}


	public ObservableList<Midia> getMidiasAConverter() {
		return midiasAConverter;
	}


	public void setMidiasAConverter(ObservableList<Midia> midiasAConverter) {
		this.midiasAConverter = midiasAConverter;
	}


	public TabPane getRootLayout() {
		return rootLayout;
	}


	public void setRootLayout(TabPane rootLayout) {
		this.rootLayout = rootLayout;
	}
	
	
}
