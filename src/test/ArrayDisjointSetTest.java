package test;

import static org.junit.jupiter.api.Assertions.*;
import sheepRecog.ArrayDisjointSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArrayDisjointSetTest {
	ArrayDisjointSet ads;

	@BeforeEach
	void setUp() throws Exception {
		ads = new ArrayDisjointSet(100);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	final void testArrayDisjointSetIntInt() {
		int[] testArray = {0,0,1,1,2,2,3,3,4,4}; 
		ads = new ArrayDisjointSet(10, 5);
		for(int i = 0; i<ads.getSize();i++)
			assertEquals(testArray[i], ads.get(i));
	}

	@Test
	final void testArrayDisjointSetIntArray() {
		int[] testArray = {0,0,1,1,2,2,3,3,4,4};
		ads = new ArrayDisjointSet(testArray);
		for(int i=0; i<ads.getSize();i++)
			assertEquals(testArray[i], ads.get(i));
	}

	@Test
	final void testArrayDisjointSetInt() {
		ads = new ArrayDisjointSet(10);
		for(int i=0;i<ads.getSize();i++)
			assertEquals(i, ads.get(i));
	}

	@Test
	final void testFind() {
		assertEquals(ads.find(10), 10);
		ads.union(10, 11);
		assertEquals(ads.find(10), 11);
		ads.union(11, 15);
		assertEquals(ads.find(10), 15);
	}

	@Test
	final void testUnion() {
		ads.union(10, 11);
		assertEquals(11, ads.get(10));
		ads.union(11, 15);
		assertEquals(15, ads.get(15));
		assertEquals(15, ads.get(11));
		assertEquals(11, ads.get(10));
		assertEquals(15, ads.find(10));
	}

}
