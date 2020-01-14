package elements;

import utils.Point3D;

public interface robots 
{
	int getSrcNode();
	
	int getID();
	
	Point3D getLocation();
	
	int getNextNode();
	
	double getMoney();

}
