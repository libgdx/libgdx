
package com.badlogic.gdx.math;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RectangleTest {

	private Rectangle rectangle;
	private Rectangle rectangle2;

	@Before
	public void setup () {
		rectangle = new Rectangle(1, 2, 3, 4);
		rectangle2 = new Rectangle(1, 1, 1, 1);
	}

	@Test
	public void givenARectangle_whenGettingItsSides_returnCorrectValues () throws Exception {
		// GIVEN default rectangle on 1,2 with width 3 and height 4

		// EXPECT
		assertEquals("Wrong bottom value", 2, rectangle.getBottom(), 0);
		assertEquals("Wrong top value", 6, rectangle.getTop(), 0);
		assertEquals("Wrong left value", 1, rectangle.getLeft(), 0);
		assertEquals("Wrong right value", 4, rectangle.getRight(), 0);
	}

	@Test
	public void givenARectangle_whenSettingItsSides_setCorrectValues () throws Exception {
		// GIVEN default rectangle on 1,2 with width 3 and height 4

		// WHEN
		rectangle.setLeft(5);

		// THEN
		assertEquals("Wrong left value", 5, rectangle.getLeft(), 0);
		assertEquals("Wrong X value", 5, rectangle.x, 0);
		assertEquals("Moved Y coordinate when not necessary", 2, rectangle.y, 0);

		// WHEN
		rectangle.setRight(5);

		// THEN
		assertEquals("Wrong right value", 5, rectangle.getRight(), 0);
		assertEquals("Wrong X value", 2, rectangle.x, 0);
		assertEquals("Moved Y coordinate when not necessary", 2, rectangle.y, 0);

		// WHEN
		rectangle.setTop(5);

		// THEN
		assertEquals("Wrong top value", 5, rectangle.getTop(), 0);
		assertEquals("Moved X coordinate when not necessary", 2, rectangle.x, 0);
		assertEquals("Wrong Y value", 1, rectangle.y, 0);

		// WHEN
		rectangle.setBottom(5);

		// THEN
		assertEquals("Wrong bottom value", 5, rectangle.getBottom(), 0);
		assertEquals("Moved X coordinate when not necessary", 2, rectangle.x, 0);
		assertEquals("Wrong Y value", 5, rectangle.y, 0);
	}

	@Test
	public void givenARectangle_whenGettingCenterWithoutVector_returnCenterCorrectly () throws Exception {
		// GIVEN default rectangle on 1,2 with width 3 and height 4

		// WHEN
		Vector2 center = rectangle.getCenter();

		// THEN
		assertEquals("Wrong X value", 2.5, center.x, 0);
		assertEquals("Wrong Y value", 4, center.y, 0);

		// GIVEN default rectangle on 1,1 with width 1 and height 1

		// WHEN
		Vector2 center2 = rectangle2.getCenter();

		// THEN
		assertEquals("Wrong X value", 1.5, center2.x, 0);
		assertEquals("Wrong Y value", 1.5, center2.y, 0);
	}

	@Test
	public void givenARectangle_whenSettingCenter_setCorrectValues () throws Exception {
		// GIVEN default rectangle on 1,2 with width 3 and height 4

		// WHEN
		rectangle.setCenter(5, 5);

		// THEN
		assertEquals("Wrong X value", 3.5, rectangle.x, 0);
		assertEquals("Wrong Y value", 3, rectangle.y, 0);

		// GIVEN default rectangle on 1,1 with width 1 and height 1

		// WHEN
		rectangle2.setCenter(new Vector2(5, 5));

		// THEN
		assertEquals("Wrong X value", 4.5, rectangle2.x, 0);
		assertEquals("Wrong Y value", 4.5, rectangle2.y, 0);
	}

	@Test
	public void testToString () {
		assertEquals("[5.0,-4.1,0.03,-0.02]", new Rectangle(5f, -4.1f, 0.03f, -0.02f).toString());
	}

	@Test
	public void testFromString () {
		assertEquals(new Rectangle(5f, -4.1f, 0.03f, -0.02f), new Rectangle().fromString("[5.0,-4.1,0.03,-0.02]"));
	}
}
