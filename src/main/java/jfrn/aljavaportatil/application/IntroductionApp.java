package jfrn.aljavaportatil.application;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfrn.aljavaportatil.view.IntroductionController;

public class IntroductionApp extends Application {
	
	private Stage stage;
	private AnchorPane rootLayout;

	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		stage.setTitle("Aljava Portátil - JFRN v1.0");
		stage.setResizable(false);
		
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
				loader.setLocation(IntroductionApp.class.getResource("/jfrn/aljavaportatil/view/IntroductionView.fxml"));
		rootLayout = (AnchorPane) loader.load();
		
		Scene scene = new Scene(rootLayout);
		stage.setScene(scene);

		IntroductionController controller = loader.getController();
		controller.setIntroductionApp(this);
		
		stage.show();
	}
	
	public static void main( String[] args ) {
		launch(args);
	}

}
