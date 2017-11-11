package application;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class PrinterCSV implements Printable{
	private static volatile double width, height;
	protected static volatile int currentColor = 0;
	private static PrintGenerator image;
	private static boolean initialized = false;
	private static File chosenFolder;
	
	public PrinterCSV() {
		//Do nothing
	}
	
	@Override
	public void initialize(PrintGenerator imagePreview, double printWidth) throws Exception{
		//Directory chooser
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("Choose output folder");
		chosenFolder = dc.showDialog(GUI.getStage());
		if(chosenFolder.isFile())throw new Exception();
		//TODO sanity checks
		
		//Image handling
		image = imagePreview;
		System.out.println("Point set dimensions: " + image.width() + " x " + image.height());
		width = printWidth;
		height = width/(double)image.width()*image.height();
		System.out.println("Print dimensions: " + width + " x " + height);
		System.out.println("PrinterInterface done initializing");
	}


	private static volatile ArrayList<Point> pointList;
	private static volatile Point currentPoint;
	private static volatile int size;
	private static volatile CalcService calcService = new CalcService();
	private static volatile DrawService drawService = new DrawService();
	
	private static class CalcService extends Service<Void>{

		@Override
		protected Task<Void> createTask() {
			//Initialize task
			Task<Void> calcPoints = new Task<Void>(){
				@Override
				public Void call(){
					System.out.println("Calculating points for color " + currentColor);
					image.calculatePath(currentColor);
					GUIController.progress.set(0.0);
					pointList = image.getPoints();
					size = pointList.size();
					return null;
				}
			};
			calcPoints.setOnSucceeded(e->{
				showDialogue( "Set the pen color to " + currentColor);
				drawService.reset();
				drawService.start();
			});
			return calcPoints;
		}
		
	}
	
	private static class DrawService extends Service<Void>{

		@Override
		protected Task<Void> createTask() {
			Task<Void> drawPoints = new Task<Void>(){
				@Override
				public Void call(){
					//TODO create file in folder
					
					int i = 0;
					while(pointList.size()>1){
						currentPoint = pointList.remove(0);
						//TODO output point list to file
						//Update periodically
						i = (i+1)%1000;
						if(i==0) {
							image.draw();
							GUIController.progress.set(1.0-(double)pointList.size()/size);
						}
					}
					pointList.clear();
					return null;
				}
			};
			
			drawPoints.setOnSucceeded(e->{
				if(currentColor<PrintGenerator.getNumColors()-1){
					System.out.println("Printing color " + ++currentColor);
					showDialogue( "Lift the pen.");
					//TODO
					calcService.reset();
					calcService.start();
				}
				else{

					showDialogue( "Yay!! It's finally done!  Next time you should use a real printer :)");
					System.out.println("Yippee! Done printing!");
				}
			});
			
			
			return drawPoints;
		}
		
	}
	
	@Override
	public void print() throws Exception{
		if(!initialized) initialize(GUIController.previewController, 3.0);
		calcService.start();
		/*
		int numDots = 0;
		for(int i = 0; i < image.width(); i ++){
			for (int j = 0; j < image.height(); j++) {
				if(image.isBlack(i, j))numDots++;
			}
		}
		steps = numDots-1;
		int p = 0, q = 0;
		while(numDots>0){
			int x = -1, y = 0, d = 1;
			//While no point has been found
			while(x==-1){
				//Check for points within a range of d
				for(int i = Math.max(0, p-d); i < Math.min(image.width(), p+d);i++){
					for(int j = Math.max(0, q-d); j < Math.min(image.height(), q+d);j++){
						if(image.isBlack(i, j)){
							if(x==-1||Math.hypot(i-p, j-q)<Math.hypot(i-x, j-y)){
								x = i; y = j;
							}
						}
					}
				}
				d++;
			}
			image.setBlack(x, y, false);
			int timeElapsed = 0;
			while(steps>=numDots)
				try {
					Thread.sleep(100);
					timeElapsed ++;
					if(timeElapsed%100==0){
						sendCoordinatesTo(p, q);
					}
				} 
				catch (InterruptedException e) {}
			p = x; q = y;
			sendCoordinatesTo(x, y);
			image.draw();
			numDots--;
		}
		System.out.println("Done printing");
		 */


	}

	public static boolean isinitialized(){
		return initialized;
	}

	private static void delay(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void showDialogue(String message){
		/*Stage dialog = new Stage();
		dialog.initStyle(StageStyle.UTILITY);
		dialog.centerOnScreen();
		Scene scene = new Scene(new Group(new Text(25, 25, message)));
		dialog.setScene(scene);
		dialog.showAndWait();*/
		GUIController.info(message);
	}


}
