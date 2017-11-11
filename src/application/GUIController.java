package application;



import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ProgressBar;

public class GUIController implements Initializable {

    @FXML
    private SplitPane splitPanel;

    @FXML
    private AnchorPane imageContainer;

    @FXML
    private ImageView imageViewer;

    @FXML
    private AnchorPane previewContainer;

    @FXML
    private Canvas printPreview;

    @FXML
    private MenuBar menuBar;

    @FXML
    private Menu menuFile;

    @FXML
    private MenuItem menuClose;

    @FXML
    private MenuItem menuOpen;

    @FXML
    private MenuItem menuPrint;

    @FXML
    private Menu menuHelp;

    @FXML
    private MenuItem menuAbout;

    @FXML
    private SplitMenuButton printTypeMenu;
    
    @FXML
    private AnchorPane panelColors;

    @FXML
    private ColorPicker colorPicker1;

    @FXML
    private ColorPicker colorPicker2;

    @FXML
    private ColorPicker colorPicker3;

    @FXML
    private ColorPicker colorPicker4;

    @FXML
    private ColorPicker colorPicker5;

    @FXML
    private ColorPicker colorPicker6;

    @FXML
    private ColorPicker colorPicker7;

    @FXML
    private ColorPicker colorPicker8;

    @FXML
    private ProgressBar progressBar;
    
    public static SimpleDoubleProperty progress = new SimpleDoubleProperty(); 
    
    public static GUIController instance;
    
    public static PrintGenerator previewController;
    
    private static int DESTINATION_ARDUINO = 0,
    		DESTINATION_CSV = 1;
    
    private static int printDestination = DESTINATION_ARDUINO;

    @Override
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
    	instance = this;
    	previewController = new PrintGenerator(printPreview);
    	printPreview.widthProperty().bind(previewContainer.widthProperty());
    	printPreview.heightProperty().bind(previewContainer.heightProperty());
    	imageViewer.fitWidthProperty().bind(imageContainer.widthProperty());
    	imageViewer.fitHeightProperty().bind(imageContainer.heightProperty());
    	printPreview.widthProperty().addListener((obs, oldWidth, newWidth) -> draw());
    	printPreview.heightProperty().addListener((obs, oldheight, newheight) -> draw());
    	colorPicker1.setValue(Color.MAGENTA);
    	colorPicker2.setValue(Color.YELLOW);
    	colorPicker3.setValue(Color.CYAN);
    	colorPicker4.setValue(Color.DARKGREEN);
    	colorPicker5.setValue(Color.BLACK);
    	colorPicker6.setValue(null);
    	colorPicker7.setValue(null);
    	colorPicker8.setValue(null);
    	progressBar.progressProperty().bind(progress);
    }
    
    
    @FXML
    void closeApp(ActionEvent event) {
    	PrinterArduino.printer.close();
    	GUI.getStage().close();
    	System.exit(0);
    }

    @FXML
    void openImage(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Open Resource File");
    	File imageFile = fileChooser.showOpenDialog(GUI.getStage());
    	if(imageFile!=null) {
    		String path = imageFile.toURI().toString();
    		System.out.println("Image path: " + path);
    		try{
    			Image newImage = new Image(path);
    			imageViewer.setImage(newImage);
    			previewController.setImage(newImage);
    			draw();
    		}
    		catch(Exception e){
    			Alert a = new Alert(Alert.AlertType.ERROR, "Uh oh, the file you chose just didn't work.\n\n" + e.getMessage(), ButtonType.OK);
    			a.setResizable(true);
    			a.setTitle("Oh no!!!");
    			a.showAndWait();
    		}
    	}
    }

    @FXML
    void panelDragDetected(MouseEvent event) {

    }
    
    @FXML
    void panelDragDone(MouseDragEvent event) {
    	draw();
    }

    @FXML
    void print(ActionEvent event) {
    	try{
    		//Create, initialize, and run a printer
    		//XXX Use a switch instead of a ternary operator if any other printers are written
    		Printable pi = printDestination==DESTINATION_ARDUINO?new PrinterArduino():new PrinterCSV();
    		if(!PrinterArduino.isinitialized()){
    			double width = Double.parseDouble(prompt("Width of print", "What is the length of the print?", "6.0"));
    			pi.initialize(previewController, width);
    		}
    		pi.print();
    	}
    	catch(Exception e){
    		String message = "Uh oh, we couldn't print\n\n\n"+e.toString()+"\n"+e.getMessage()+"\n";
    		for(StackTraceElement element: e.getStackTrace()){
    			if(element.getLineNumber()<0)break;
    			message += element.toString() + "\n";
    		}
			Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
			a.setResizable(true);
			a.setTitle("Oh no!!!");
			a.showAndWait();
    	}
    }

    @FXML
    void setMenuColor(ActionEvent event) {
    	panelColors.setVisible(true);
    	previewController.setPrintMethod(PrintGenerator.METHOD_COLOR);
    	printTypeMenu.setText("Color");
    	printDestination = DESTINATION_ARDUINO;
    }

    @FXML
    void setMenuBW(ActionEvent event) {
    	panelColors.setVisible(false);
    	previewController.setPrintMethod(PrintGenerator.METHOD_BLACK_AND_WHITE);
    	printTypeMenu.setText("Black and White");
    	printDestination = DESTINATION_ARDUINO;
    }
    
    @FXML
    void setMenuCSVBW(ActionEvent event) {
    	panelColors.setVisible(false);
    	previewController.setPrintMethod(PrintGenerator.METHOD_BLACK_AND_WHITE);
    	printTypeMenu.setText("Black and White (to CSV)");
    	printDestination = DESTINATION_CSV;
    }
    
    /**
	 * @return the progressBar
	 */
	public ProgressBar getProgressBar() {
		return progressBar;
	}


	@FXML
    void pickColor(ActionEvent event){
    	ColorPicker source = (ColorPicker) event.getSource();
    	if (source == colorPicker1) previewController.setColorPalette(0, source.getValue());
    	if (source == colorPicker2) previewController.setColorPalette(1, source.getValue());
    	if (source == colorPicker3) previewController.setColorPalette(2, source.getValue());
    	if (source == colorPicker4) previewController.setColorPalette(3, source.getValue());
    	if (source == colorPicker5) previewController.setColorPalette(4, source.getValue());
    	if (source == colorPicker6) previewController.setColorPalette(5, source.getValue());
    	if (source == colorPicker7) previewController.setColorPalette(6, source.getValue());
    	if (source == colorPicker8) previewController.setColorPalette(7, source.getValue());
    }

    @FXML
    void showAboutDialog(ActionEvent event) {

    }
   

    /**
	 * @return the previewController
	 */
	public PrintGenerator getPreviewController() {
		return previewController;
	}


	public void draw() {
    	previewController.draw();
    }
    
    public static String prompt(String title, String message, String defaultValue){
    	TextInputDialog dialog = new TextInputDialog(defaultValue);
		dialog.setTitle(title);
		dialog.setHeaderText(title);
		dialog.setContentText(message);
		dialog.initOwner(GUI.getStage());
		dialog.showAndWait();
		String answer = dialog.getResult();
		return answer;
    }
    
    public static void info(String message){
    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.setTitle("Change color");
    	alert.setHeaderText(null);
    	alert.setContentText(message);

    	alert.showAndWait();
    }

}
