package elements;

import dataStructure.edge_data;
import utils.Point3D;

public class Fruit implements fruits
{
	private double value;
	private Point3D pos;
	private edge_data edge;
	
	public Fruit(double value, Point3D pos, edge_data edge) 
	{
		this.value = value;
		this.pos = pos;
		this.edge = edge;
	}
	
	@Override
	public Point3D getLocation() 
	{
		return new Point3D(pos);
	}
	
	@Override
	public int getType() 
	{
		if(this.edge.getDest() < this.edge.getSrc())
			return -1;
		return 1;
	}
	
	@Override
	public double getValue() 
	{
		return value;
	}

}
