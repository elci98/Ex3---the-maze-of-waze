package elements;

import java.io.Serializable;

import dataStructure.edge_data;
import dataStructure.node_data;

public class edgeData implements edge_data,Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private nodeData _src,_dest;
	private int _tag;
	private double _weight;
	private String _info;
	public edgeData(node_data src, node_data dest, double weight)
	{
		_src=(nodeData) src;
		_dest=(nodeData) dest;
		_weight=weight;
	}
	@Override
	public int getSrc() 
	{
		return _src.getKey();
	}

	@Override
	public int getDest() 
	{
		return _dest.getKey();
	}

	@Override
	public double getWeight() 
	{
		return _weight;
	}

	@Override
	public String getInfo() 
	{
		return _info;
	}

	@Override
	public void setInfo(String s) 
	{
		_info=s;
	}

	@Override
	public int getTag() 
	{
		return _tag;
	}

	@Override
	public void setTag(int t) 
	{
		if(t>0)
			_tag=t;
		else
			System.out.println("invalid inserted tag");
	}
	public String toString()
	{
		return _src+"-----"+(int)_weight+"---->"+_dest;
	}

}
