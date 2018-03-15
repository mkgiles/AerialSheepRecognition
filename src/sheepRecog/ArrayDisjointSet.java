/**
 * 
 */
package sheepRecog;

/**
 * The Class ArrayDisjointSet.
 *
 * @author Conor James Giles
 */
public class ArrayDisjointSet {
	
	/** The internal array. */
	private int[] array;
	
	/**
	 * Instantiates a new array of disjoint sets.
	 *
	 * @param size
	 *            the size
	 * @param number
	 *            the number
	 */
	public ArrayDisjointSet(int size, int number){
		array = new int[size];
		for (int i=0;i<size;i++)
			array[i] = i/(size/number);
	}
	
	/**
	 * Instantiates a new array of disjoint sets.
	 *
	 * @param array
	 *            the array
	 */
	public ArrayDisjointSet(int[] array){
		this.array = array;
	}
	
	/**
	 * Instantiates a new array of disjoint sets.
	 *
	 * @param size
	 *            the size
	 */
	public ArrayDisjointSet(int size) {
		this(size, size);
	}
	
	/**
	 * Find the root of a disjoint set's tree.
	 *
	 * @param index
	 *            the index
	 * @return the int
	 */
	public int find(int index) {
		if(index > array.length)
			return -1;
		return array[index]==index?index:(array[index]=find(array[index]));
	}
	
	/**
	 * Union two disjoint set's trees with QuickUnion.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 */
	public void union(int a, int b) {
		array[find(a)]=array[find(b)];
	}
	
	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public int getSize() {
		return array.length;
	}
	
	/**
	 * Sets the value of an element.
	 *
	 * @param index
	 *            the index
	 * @param value
	 *            the value
	 */
	public void set(int index, int value) {
		array[index] = value;
	}
	
	/**
	 * Gets the value of an element.
	 *
	 * @param index
	 *            the index
	 * @return the int
	 */
	public int get(int index) {
		return array[index];
	}
}
