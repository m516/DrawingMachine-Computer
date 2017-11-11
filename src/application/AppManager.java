package application;
/**
 * A static class that creates and initializes vital components for the application
 * @author mm44928
 *
 */
public class AppManager{
	private AppManager(){
	}
	/**
	 * Initializes the application and its user interface
	 */
	public static void init(){
		System.out.println("AppManager Initialized!");
		//Launch GUI
		GUI.run();
	}
	public static void main(String[] args) {
		AppManager.init();
	}
}
