package Tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import dataStructure.edge_data;
import elements.Fruit;
import elements.edgeData;
import elements.nodeData;
import utils.Point3D;

@TestInstance(Lifecycle.PER_CLASS) // allows to declare BeforeAll params as non-static
class FruitTest 
{
	final int SIZE = 10;
	Point3D[] point = new Point3D[SIZE];
	Point3D p,p1;
	edge_data[] edge = new edge_data[SIZE]; 
	Fruit[] fruit = new Fruit[SIZE];
	
	@BeforeAll
	void init()
	{
		for (int i = 0; i < fruit.length; i++) 
		{
			p = new Point3D(i,i+1,i+2);
			p1 = new Point3D(3*1,i+1,1+2.5);
			edge[i] = new edgeData(new nodeData(p,i+1,1), new nodeData(p1,2*i+1,0), 3*i);
			point[i] = new Point3D(i, 2*i); 
			fruit[i] = new Fruit(i, point[i],edge[i]);
		}
		
	}

	@Test
	void testFruit() 
	{
		for (int i = 0; i < fruit.length; i++) 
		{
			assertTrue(fruit[i] != null);
		}
	}

	@Test
	void testGetLocation() 
	{
		for (int i = 0; i < fruit.length; i++) 
		{
			double x = fruit[i].getLocation().x();
			double y = fruit[i].getLocation().y();
			assertEquals(i, x);
			assertEquals(2*i, y);
		}
	}

	@Test
	void testGetType() 
	{
		for (int i = 0; i < edge.length; i++) 
		{
			int src = edge[i].getSrc();
			int dest = edge[i].getDest();
			int type = src < dest ? 1 : -1;
			assertEquals(type, fruit[i].getType());
		}
	}

	@Test
	void testGetValue() 
	{
		for (int i = 0; i < fruit.length; i++) 
		{
			assertEquals(i, fruit[i].getValue());
		}
	}

	@Test
	void testGetEdge() 
	{
		for (int i = 0; i < fruit.length; i++) 
		{
			assertEquals(edge[i], fruit[i].getEdge());
		}
	}

}
