package Tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import elements.RobotG;
import utils.Point3D;

@TestInstance(Lifecycle.PER_CLASS) // allows to declare BeforeAll params as non-static
class RobotGTest 
{
	final int SIZE = 10;
	RobotG[] robot = new RobotG[SIZE];
	Point3D[] point = new Point3D[SIZE];

	@BeforeAll
	void init()
	{
		for (int i = 0; i < robot.length; i++) 
		{
			point[i] = new Point3D(i, 2*i+1);
			robot[i] = new RobotG(i, i+2, point[i], i*i, 8*i);
		}
	}

	@Test
	void testRobotG() 
	{
		for (int i = 0; i < robot.length; i++) 
		{
			assertTrue(robot[i] != null);
		}
	}

	@Test
	void testGetSrcNode() 
	{
		for (int i = 0; i < robot.length; i++) 
		{
			assertEquals(i, robot[i].getSrcNode());
		}
	}

	@Test
	void testGetID() 
	{
		for (int i = 0; i < robot.length; i++) 
		{
			assertEquals(i+2, robot[i].getID());
		}
	}

	@Test
	void testGetLocation() 
	{
		for (int i = 0; i < robot.length; i++) 
		{
			assertEquals(point[i], robot[i].getLocation());
		}
	}

	@Test
	void testGetNextNode() 
	{
		for (int i = 0; i < robot.length; i++) 
		{
			assertEquals(i*i, robot[i].getNextNode());
		}
	}

	@Test
	void testGetMoney() 
	{
		for (int i = 0; i < robot.length; i++) 
		{
			assertEquals(8*i, robot[i].getMoney());
		}	
	}

}
