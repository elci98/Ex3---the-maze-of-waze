package elements;

import dataStructure.edge_data;
import utils.Point3D;

public class Fruit implements fruits
{
	private double value;
	private Point3D pos;
	private edge_data edge;
	private boolean isDest=false;
	
	public boolean isDest() {
		return isDest;
	}

	public void setDest(boolean isDest) {
		this.isDest = isDest;
	}

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
		if(this.edge.getSrc() < this.edge.getDest())
			return 1;
		return -1;
	}
	
	@Override
	public double getValue() 
	{
		return value;
	}

	public edge_data getEdge() 
	{
		return edge;
	}
	@Override
	public String toString()
	{

		return edge.getSrc()+"----"+value+"----->"+edge.getDest();
	}
}
