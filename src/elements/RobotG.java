package elements;

import utils.Point3D;

public class RobotG implements robots {

	private int SrcNode;
	private int ID;
	private Point3D pos;
	private int NextNode;
	private double Money;
	
	public RobotG(int srcNode, int ID, Point3D pos, int nextNode, double Money) 
	{
		this.SrcNode = srcNode;
		this.ID = ID;
		this.pos = pos;
		this.NextNode = nextNode;
		this.Money = Money;
	}

	@Override
	public int getSrcNode() 
	{
		return SrcNode;
	}

	@Override
	public int getID() 
	{
		return ID;
	}

	@Override
	public Point3D getLocation() 
	{
		return pos;
	}

	@Override
	public int getNextNode() 
	{
		return NextNode;
	}
	
	@Override
	public double getMoney() 
	{
		return Money;
	}

}
