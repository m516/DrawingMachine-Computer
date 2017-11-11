/**
 * 
 */
package application;

/**
 * @author mm44928
 *
 */
public interface Printable {
	public void initialize(PrintGenerator imagePreview, double printWidth) throws Exception;
	public void print() throws Exception;
}
