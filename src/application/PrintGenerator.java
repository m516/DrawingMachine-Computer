package application;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;


public class PrintGenerator{
	private Image image;
	private Canvas canvas;
	private byte[][] pixels;
	private ArrayList<Point> points = new ArrayList<Point>();
	public static final int METHOD_BLACK_AND_WHITE = 0,
			METHOD_COLOR = 1;
	private static Color palette[] = {Color.MAGENTA,
			Color.YELLOW,
			Color.CYAN,
			Color.DARKGREEN,
			Color.BLACK,
			null,
			null, 
			null};
	private static int numColors = 5;
	private static int printMethod = METHOD_BLACK_AND_WHITE;
	public PrintGenerator(Canvas previewCanvas){
		canvas = previewCanvas;
	}
	public PrintGenerator(Canvas previewCanvas, Image image, int printMethod){
		canvas = previewCanvas;
		this.image = image;
		PrintGenerator.printMethod = printMethod;
		computePoints();
	}
	/**
	 * @return the image
	 */
	public Image getImage() {
		return image;
	}
	/**
	 * @param image the new image
	 */
	public void setImage(Image image) {
		this.image = image;
		computePoints();
	}
	/**
	 * @return the print method
	 */
	public int getPrintMethod() {
		return printMethod;
	}
	/**
	 * @return the pixel array
	 */
	public byte[][] getPixels() {
		return pixels;
	}
	/**
	 * @return an ArrayList of dark points
	 * @see calculatePath()
	 */
	public ArrayList<Point> getPoints(){
		return points;
	}
	/**
	 * @param printMethod the new print method
	 */
	public void setPrintMethod(int newPrintMethod) {
		printMethod = newPrintMethod;
		if(image!=null)computePoints();
	}

	/**
	 * @return the numColors
	 */
	public static int getNumColors() {
		if(printMethod==METHOD_BLACK_AND_WHITE)return 1;
		return numColors;
	}
	private void computePoints(){
		if(printMethod==METHOD_BLACK_AND_WHITE) System.out.println("Printing in black and white");
		else if(printMethod==METHOD_COLOR) System.out.println("Printing in color");
		else System.out.println("I don't know how to print!");
		PixelReader pixelReader = image.getPixelReader();
		pixels = new byte[(int)image.getWidth()][(int)image.getHeight()];

		if(printMethod==METHOD_BLACK_AND_WHITE){
			double total = 0;
			for(int i = 0; i < image.getWidth(); i ++){
				for (int j = 0; j < image.getHeight(); j++) {
					if(pixelReader.getColor(i, j).getBrightness()<0.96){
						total += Math.pow(1-pixelReader.getColor(i, j).getBrightness(), 2);
						if(total>=1.0){
							total -= 1.0;
							setBlackAt(i, j, true);
						}
					}
				}
			}
		}
		else if(printMethod == METHOD_COLOR){
			double total[] = new double[8];
			for(int i = 0; i < image.getWidth(); i ++){
				for (int j = 0; j < image.getHeight(); j++) {
					Color color = pixelReader.getColor(i, j);
					for (byte c = 0; c < numColors; c++) {
						total[c] += 1-colorDifference(color, subtractColors(color, palette[c]));
						if(total[c]>1.0){
							total[c]-=1.0;
							setColorAt(i, j, c, true);
						}
						color = subtractColors(color, palette[c], colorDifference(color, subtractColors(color, palette[c])));
					}
				}
			}
		}
		draw();
	}

	private double colorDifference(Color color1, Color color2){
		return Math.max(Math.max(
				color1.getRed()-color2.getRed(), 
				color1.getGreen()-color2.getGreen()),
				color1.getBlue()-color2.getBlue());
	}

	private Color subtractColors(Color color1, Color color2){
		return Color.rgb((int)(color1.getRed()*255)&(int)(color2.getRed()*255),
				(int)(color1.getGreen()*255)&(int)(color2.getGreen()*255),
				(int)(color1.getBlue()*255)&(int)(color2.getBlue()*255));
	}
	private Color subtractColors(Color color1, Color color2, double amount){
		color2 = colorToWhite(color2, amount);
		return Color.rgb(
				(int)(color1.getRed()*255.0)&(int)(color2.getRed()*255.0),
				(int)(color1.getGreen()*255.0)&(int)(color2.getGreen()*255.0),
				(int)(color1.getBlue()*255.0)&(int)(color2.getBlue()*255.0));
	}
	/**
	 * Creates a new color on the gradient between a pre-existing color and white
	 * @param color the original color
	 * @param value the distance between the old color and the new one, i.e. 0 is
	 * returns the original color, and 1 returns white
	 * @return a new color on the gradient between a pre-existing color and white
	 */
	private Color colorToWhite(Color color, double value){
		return new Color(color.getRed()+value*(1-color.getRed()),
				color.getGreen()+value*(1-color.getGreen()),
				color.getBlue()+value*(1-color.getBlue()), 1.0);
	}
	private void drawPoints(){
		GraphicsContext graphics = canvas.getGraphicsContext2D();
		graphics.setFill(Color.BLACK);
		graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		if(image != null){
			double projectedWidth = canvas.getWidth();
			double projectedHeight = image.getHeight()*projectedWidth/image.getWidth();
			if(projectedHeight > canvas.getHeight()){
				projectedWidth *= canvas.getHeight()/projectedHeight;
				projectedHeight = canvas.getHeight();
			}
			double deltaX = projectedWidth/image.getWidth();
			double deltaY = projectedHeight/image.getHeight();
			graphics.setFill(Color.WHITE);
			graphics.fillRect(0, 0, projectedWidth, projectedHeight);

			if (printMethod == METHOD_BLACK_AND_WHITE){
				graphics.setFill(Color.BLACK);
				if(points.size()==0){
					for(int i = 0; i < pixels.length; i ++){
						for (int j = 0; j < pixels[i].length; j++) {
							if(isBlackAt(i,j)) graphics.fillRect(i*deltaX, j*deltaY, deltaX, deltaY);
						}
					}
				}
				else{//If there are points in the list
					graphics.setStroke(Color.BLACK);
					Point oldPoint = points.get(0);
					for(Point p: points){
						graphics.strokeLine(oldPoint.x*deltaX, oldPoint.y*deltaY, p.x*deltaX, p.y*deltaY);
						oldPoint = p;
					}
				}
			}
			else if (printMethod == METHOD_COLOR){
				if(points.size()==0){
					for(int i = 0; i < pixels.length; i ++){
						for (int j = 0; j < pixels[i].length; j++) {
							Color paintColor = Color.WHITE;
							for(int colorIndex = 0; colorIndex<numColors; colorIndex++){
								if(isColorAt(i, j, colorIndex))paintColor = subtractColors(paintColor, palette[colorIndex]);
							}
							graphics.setFill(paintColor);
							graphics.fillRect(i*deltaX, j*deltaY, deltaX, deltaY);
						}
					}
				}
				else{//If there are points in the points list
					for(int i = 0; i < pixels.length; i ++){
						for (int j = 0; j < pixels[i].length; j++) {
							Color paintColor = Color.WHITE;
							for(int colorIndex = 0; colorIndex<numColors; colorIndex++){
								if(isColorAt(i, j, colorIndex))paintColor = subtractColors(paintColor, palette[colorIndex]);
							}
							graphics.setFill(colorToWhite(paintColor, 0.65));
							graphics.fillRect(i*deltaX, j*deltaY, deltaX, deltaY);
						}
					}
					graphics.setStroke(Color.BLACK);
					Point oldPoint = points.get(0);
					for(Point p: points){
						graphics.strokeLine(oldPoint.x*deltaX, oldPoint.y*deltaY, p.x*deltaX, p.y*deltaY);
						oldPoint = p;
					}
				}
			}
		}
		else{
			graphics.setFill(Color.WHITE);
			graphics.fillOval(0, 0, canvas.getWidth(), canvas.getHeight());
		}
	}

	/**
	 * Gets the value of a bit in a byte
	 * @param b the byte
	 * @param position the index of the bit, where zero is the LSB and seven is the MSB
	 * @return true if the bit is a one, false if zero
	 */
	private boolean getBit(byte b, int position){
		return (b>>position&0x01)==0x01;
	}
	void setBlackAt(int x, int y, boolean isBlack){
		pixels[x][y]|=0x01;
	}
	void setColorAt(int x, int y, int color, boolean isBlack){
		pixels[x][y]|=0x01<<color;
	}

	public boolean isBlackAt(int x, int y){
		return getBit(pixels[x][y],0);
	}
	public boolean isColorAt(int x, int y, int colorIndex){
		return getBit(pixels[x][y],colorIndex);
	}
	public int width(){
		return pixels.length;
	}
	public int height(){
		return pixels[0].length;
	}
	public void setColorPalette(int colorIndex, Color newColor){
		palette[colorIndex] = newColor;
		computePoints();
	}
	public boolean isInBounds(int x, int y){
		return x>0&&y>0&&x<width()&&y<height();
	}


	/**
	 * Draws the preview on a graphics context
	 * @param graphics the graphics context
	 */
	public void draw(){
		Platform.runLater(()->{
			drawPoints();
		});
	}

	/**
	 * Finds the best path to all colored points of the type "color"
	 * <br>By "best" I mean a set of points, all reasonably close to one
	 * another, whose connecting lines do not cross with any others in
	 * the path
	 * @param color the color to calculate
	 */
	public void calculatePath(int color){
		System.out.println("Calculating color "+color);
		ArrayList<Point> tempPointsList = new ArrayList<Point>();
		double nextProgress = 0.01;
		GUIController.progress.set(0.0);
		delay(20);
		//Find all points
		for (int i = 0; i < width(); i++) {
			for (int j = 0; j < height(); j++) {
				if (isColorAt(i,j,color)) tempPointsList.add(new Point(i,j));
			}
		}
		int numPoints = tempPointsList.size();
		System.out.println("\tEstimating TSP for "+numPoints+" points");
		//Populate path (though this might change) with a greedy search algorithm
		System.out.println("\tPopulating path");
		points.add(tempPointsList.remove(0));
		//While there are still points left
		while(tempPointsList.size()>0){
			int closestPoint = 0;
			//Find the closestPoint
			for(int i = 0; i < tempPointsList.size(); i ++) {
				if(points.get(points.size()-1).distance(tempPointsList.get(i))<
						points.get(points.size()-1).distance(tempPointsList.get(closestPoint))){
					closestPoint = i;
				}
			}
			//Put the closest point in the points array
			points.add(tempPointsList.remove(closestPoint));
			//Periodically set the progress bar and wait for it to respond
			if((double)points.size()/(tempPointsList.size()+points.size())>nextProgress){
				GUIController.progress.set((double)points.size()/tempPointsList.size());
				nextProgress += 0.01;
				draw();
				delay(50);
			}
		}
		double progressBenchmark = 0.0;
		//Calculate the path
		System.out.println("\tCalculating path");
		GUIController.progress.set(0);
		//While a valid solution has not been found
		for(int i = 1; i < points.size(); i ++){
			int swapWith = -1;
			if(points.get(i).distance(points.get(i-1))<5)continue;
			for(int j = 1; j < i-1; j ++){
				if (lineIntersect(
						points.get(i).x,
						points.get(i).y,
						points.get(i-1).x,
						points.get(i-1).y,
						points.get(j).x,
						points.get(j).y,
						points.get(j-1).x,
						points.get(j-1).y)) {
					if(swapWith==-1||(swapWith>=0&&
							points.get(i).distance(points.get(j))<
							points.get(i).distance(points.get(swapWith)))){
						swapWith = j;
						continue;
					}
				}
			}

			if (swapWith>=0){
				int s1 = swapWith, s2 = i-1;
				while(s1<s2){
					points.add(s1, points.remove(s2));
					points.add(s2, points.remove(s1+1));
					s1 ++;
					s2 --;
				}
				i = swapWith-1;
			}
			if ((double)i/points.size()>=progressBenchmark){
				GUIController.progress.set((double)i/points.size());
				draw();
				delay(100);
				progressBenchmark = (double)i/points.size()+0.005;

			}
		}
		/*
		System.out.println("Calculating color "+0);
		ArrayList<Point> tempPointsList = new ArrayList<Point>();
		GUIController.progress.set(0.0);
		delay(20);
		for (int i = 0; i < width(); i++) {
			for (int j = 0; j < height(); j++) {
				if (isColorAt(i,j,color)) tempPointsList.add(new Point(i,j));
				//Display progress on progress bar
			}
		}
		int closest[][] = new int[tempPointsList.size()][4];
		int path[] = new int[tempPointsList.size()];
		int subIndices[] = new int[path.length];
		System.out.println("\tEstimating TSP for "+path.length+" points");
		//Populate Closest
		GUIController.progress.set(0.4);
		delay(20);
		System.out.println("\tPopulating closest variable");
		for (int i = 0; i < closest.length; i++) {
			for (int j = 0; j < closest.length; j++) {
				//Don't consider values in identical places (distance will always be zero)
				if(i==j) continue;
				for (int k = 0; k < closest[i].length; k++) {
					if(tempPointsList.get(i).distance(tempPointsList.get(j))<
							tempPointsList.get(i).distance(tempPointsList.get(closest[i][k]))||
							closest[i][k]==0) {
						for(int l = closest[i].length-1; l > k; l --){
							closest[i][l]=closest[i][l-1];
						}
						closest[i][k]=j;
						break;
					}
				}
			}
		}
		//Populate path (though this might change)
		GUIController.progress.set(0.5);
		delay(20);
		System.out.println("\tPopulating path");
		for (int i = 1; i < path.length; i++) {
			path[i]=closest[path[i-1]][0];
		}
		GUIController.progress.set(0.6);
		delay(20);
		System.out.println("\tCalculating path");
		//While a valid solution has not been found or all solutions are exhausted
		int index = 1;
		double progress = 0.0;
		while(true){
			boolean isGoodValue = true;
			//Skip this number if it's part of the list or it is a member of a line
			//that crosses any other line in the path
			if(path[index]==path[0]) isGoodValue=false;
			for(int i = 1; i < index-1; i ++){
				if(path[index]==path[i]){
					isGoodValue = false;
					break;
				}
				else if(lineIntersect(
						tempPointsList.get(path[index]).x,
						tempPointsList.get(path[index]).y,
						tempPointsList.get(path[index-1]).x,
						tempPointsList.get(path[index-1]).y,
						tempPointsList.get(path[i]).x,
						tempPointsList.get(path[i]).y,
						tempPointsList.get(path[i-1]).x,
						tempPointsList.get(path[i-1]).y)) {
					isGoodValue = false;
					break;
				}
			}
			//If it is a good value
			if(isGoodValue){
				index ++;
				//Update the progress bar
				if ((double)index/path.length>progress){
					progress = (double)index/path.length;
					GUIController.progress.set(progress);
					delay(20);
				}
				//We're done calculating a path if index is the size of path
				if(index == path.length){
					//Pack everything into the Points array and return
					System.out.println("Done calculating points");
					points.clear();
					for(int i:path){
						points.add(tempPointsList.get(i));
					}
					GUIController.progress.set(1.0);
					delay(20);
					System.out.println("Proceeding to printing");
					return;
				}
				//Otherwise start over at the new index
				subIndices[index]=0;
				path[index]=closest[path[index-1]][0];
			}
			//If it is not a good value
			else{
				//Try new numbers
				while(index>0){
					subIndices[index]++;
					if(subIndices[index]>=closest[path[index]].length){
						index--;
					}
					else {
						path[index]=closest[path[index-1]][subIndices[index]];
						break;
					}
				}
				//Update the progress bar if something significant happens
				if ((double)index/path.length<progress-0.04){
					progress = (double)index/path.length;
					GUIController.progress.set(progress);
					delay(20);
				}
				//If this code is reached, there are no other options
				if(index==0){
					//Return null
					System.err.println("No good option");
					points.clear();
					System.out.println("Proceeding to printing");
					GUIController.progress.set(0.0);
					delay(20);
					return;
				}
			}

		}
		 */
	}

	void delay(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean lineIntersect(double x1, double y1, double x2, double y2, 
			double x3, double y3, double x4,double y4) {
		double x=((x1*y2-y1*x2)*(x3-x4)-(x1-x2)*(x3*y4-y3*x4))/((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));
		double y=((x1*y2-y1*x2)*(y3-y4)-(y1-y2)*(x3*y4-y3*x4))/((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));
		if (x==Double.NaN||y==Double.NaN) return false;
		else {
			if (x1>=x2) {
				if (!(x2<=x&&x<=x1)) {return false;}
			} else {
				if (!(x1<=x&&x<=x2)) {return false;}
			}
			if (y1>=y2) {
				if (!(y2<=y&&y<=y1)) {return false;}
			} else {
				if (!(y1<=y&&y<=y2)) {return false;}
			}
			if (x3>=x4) {
				if (!(x4<=x&&x<=x3)) {return false;}
			} else {
				if (!(x3<=x&&x<=x4)) {return false;}
			}
			if (y3>=y4) {
				if (!(y4<=y&&y<=y3)) {return false;}
			} else {
				if (!(y3<=y&&y<=y4)) {return false;}
			}
		}
		return true;
	}

}
