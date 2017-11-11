package application;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * The graphical user interface for the application
 * @author mm44928
 *
 */
public class GUI extends Application {
	private static Stage stage;
	@Override
	public void start(Stage primaryStage) {
		try {
			stage = primaryStage;
			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("GUILayout.fxml"));
			Scene scene = new Scene(root,600,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());	
			stage.setScene(scene);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void run(){
		launch();
	}
	public static Stage getStage(){
		return stage;
	}
}
