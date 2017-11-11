package application;

import java.util.ArrayList;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class PrinterInterface{
	private static volatile boolean hasRecievedMessage = false, hasRecievedErrorMessage = false;
	private static volatile double width, height;
	protected static volatile int currentColor = 0;
	private static PrintGenerator image;
	public static ArduinoSerialComm printer = new ArduinoSerialComm();
	private static boolean initialized = false;
	public static void initialize(PrintGenerator imagePreview, double printWidth) throws Exception{
		image = imagePreview;
		System.out.println("Point set dimensions: " + image.width() + " x " + image.height());
		width = printWidth;
		height = width/(double)image.width()*image.height();
		System.out.println("Print dimensions: " + width + " x " + height);
		printer.initialize();
		delay(1000);
		String response = GUIController.prompt("Arduino Configuration", "What is the length of the string on the left?", "12.0");
		printer.out.print(response);
		delay(1000);
		response = GUIController.prompt("Arduino Configuration", "And the right?", "12.0");
		printer.out.print(response);
		System.out.println("PrinterInterface done initializing");
		delay(1000);

		//Calibrate the printer.
		GUIController.info("Calibrating: Lift pen");
		for(int i = 0; i < 3; i ++){
			safeSendCoordinates((int)(image.width()*1.1),(int)(image.height()*1.1));
			safeSendCoordinates(0,0);
		}
		initialized = true;
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

					//Get to the first line so the printer doesn't draw an extra line.
					currentPoint = pointList.remove(0);
					safeSendCoordinates(currentPoint.x,currentPoint.y);
					currentPoint = pointList.remove(0);
					safeSendCoordinates(currentPoint.x,currentPoint.y);
					
					image.draw();
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
					int i = 0;
					while(pointList.size()>1){
						currentPoint = pointList.remove(0);
						safeSendCoordinates(currentPoint.x,currentPoint.y);
						//Update periodically
						i = (i+1)%15;
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
					safeSendCoordinates(0, 0);
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
	
	public static void print() throws Exception{
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

	private static void safeSendCoordinates(int x, int y){
		hasRecievedMessage=false;
		sendCoordinatesTo(x,y);
		while(!hasRecievedMessage){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				System.err.println("Can't wait for ya");
			}
			if(hasRecievedErrorMessage){
				sendCoordinatesTo(x,y);
				hasRecievedErrorMessage = false;
			}
		}
	}
	private static void sendCoordinatesTo(int x, int y){
		double calculatedX = (double)x/(image.width()-1)*width;
		double calculatedY = (double)y/(image.height()-1)*height;
		printer.out.println("@"+calculatedX+","+calculatedY);
		if(ArduinoSerialComm.VERBOSE) System.out.println("Application sent "+calculatedX+","+calculatedY);
	}

	public static void sendNextMessage(){
		hasRecievedMessage = true;
		hasRecievedErrorMessage = false;
	}

	public static void sendErrorMessage(){
		hasRecievedErrorMessage = true;
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
