package application;
public class Point{
	public int x, y;
	public Point(){}
	public Point(int newX, int newY){
		x=newX;
		y=newY;
	}
	public String toString(){
		return "("+x+","+y+")";
	}
	public double distance(Point point) {
		return (x-point.x)*(x-point.x)+(y-point.y)*(y-point.y);
	}
	public boolean equals(Point other){
		return other.x==x&&other.y==y;
	}
}
