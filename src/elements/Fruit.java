package elements;

import Server.robot;
import dataStructure.edge_data;
import oop_utils.OOP_Point3D;
import utils.Point3D;

public class Fruit 
{
	private Point3D pos;
	private edge_data edge;
	private double value;
	
	public Fruit()
	{
		
	}
	public void setPos(Point3D pos) {
		this.pos = pos;
	}
	public void setEdge(edge_data edge) {
		this.edge = edge;
	}
	public edge_data getEdge() {
		return edge;
	}
	public Fruit(double v, Point3D p, edge_data e)
	{
		this.value = v;
		this.pos = p;
		this.edge = e;
	}
	public Point3D getLocation() 
	{
		return new Point3D(pos);
	}
	public int getType() 
	{
		if(this.edge.getSrc() < this.edge.getDest())
			return 1;
		return -1;
	}

	public double getValue() 
	{
		return value;
	}

	public double grap(robot r, double dist) 
	{
		double ans = 0.0;
        if (this.edge != null && r != null) {
            int d = r.getNextNode();
            if (this.edge.getDest() == d) 
            {
                OOP_Point3D rp = r.getLocation();
                Point3D p = new Point3D(rp);
                if (dist > p.distance2D(this.pos)) 
                {
                    ans = this.value;
                }
            }
        }
        return ans;
	}

}
