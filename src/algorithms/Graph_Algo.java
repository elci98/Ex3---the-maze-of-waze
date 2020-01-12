package algorithms;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import dataStructure.*;
import elements.nodeData;
/**
 * This empty class represents the set of graph-theory algorithms
 * which should be implemented as part of Ex2 - Do edit this class.
 * @author nuni hagever
 *
 */
public class Graph_Algo implements graph_algorithms
{
	private graph dGraph;

	public Graph_Algo()
	{

	}

	public Graph_Algo(graph g) 
	{
		dGraph=g;
	}

	@Override
	public void init(graph g) 
	{
		dGraph=g;
	}

	/**
	 * initialize graph from given serializable file
	 * @param file_name
	 * */
	@Override
	public void init(String file_name) 
	{
		try 
		{
			FileInputStream f=new FileInputStream(file_name);
			ObjectInputStream obj= new ObjectInputStream(f);
			dGraph=(graph) obj.readObject();
			obj.close();
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * save the current graph to Serialziable file
	 * @param file_name
	 * */
	@Override
	public void save(String file_name) 
	{
		try 
		{
			FileOutputStream f=new FileOutputStream(file_name);
			ObjectOutputStream obj=new ObjectOutputStream(f);
			obj.writeObject(dGraph);
			obj.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * this method checks whether the graph is strongly connected
	 * @return true if the graph is strongly connected, false otherwise
	 * 
	 * */
	@Override
	public boolean isConnected() 
	{
		for (node_data node : dGraph.getV())
		{
			node.setTag(-1);
		}
		for (node_data node : dGraph.getV())
		{
			node.setTag(1);
			isConnected_Recursive(dGraph.getE(node.getKey()));
			if(!checkTag())
				return false;
		}
		return true;
	}

	/**
	 * Dijkstra algorithm for finding the shortest path from vertex src to vertex dest
	 * @param src
	 * @param dest
	 * 
	 * */
	@Override
	public double shortestPathDist(int src, int dest)
	{
		if(dGraph.getNode(src)==null || dGraph.getNode(dest)==null)return -1;
		node_data current;
		PriorityQueue<node_data> q=new PriorityQueue<>(dGraph.nodeSize(),new Vertex_Comperator());
		initGraph(src);
		q.addAll(dGraph.getV());
		while(!q.isEmpty())
		{
			current=q.remove();
			if(dGraph.getNode(current.getKey())!=null)
			{
				Collection<edge_data> map=dGraph.getE(current.getKey());
				for(edge_data edge : map)//iterate over all edges going out from current vertex
				{
					node_data dst=dGraph.getNode(edge.getDest());
					if(dst.getInfo().equals("FALSE")) //we skip dst vertex if visited already 
					{
						if(current.getWeight()+edge.getWeight()<dst.getWeight())
						{
							q.remove(dst); //we remove the element from queue 
							dst.setWeight(current.getWeight()+edge.getWeight());
							dst.setTag(current.getKey());//set dst predcessor to be current vertex
							//after element's weight update we push it back to the queue so the descending order will maintained.
							q.add(dst);
						}
					}
				}
			}
			current.setInfo("TRUE");
		}
		return dGraph.getNode(dest).getWeight();
	}

	/**
	 * this method uses shortestPathDist method and return a list contains all vertices
	 * we`ve past through on the shortest path from src to dest
	 * 
	 * */
	@Override
	public List<node_data> shortestPath(int src, int dest) 
	{
		if(shortestPathDist(src,dest)==Double.MAX_VALUE)
		{
			System.out.println("there are no edges going out from vertex src");
			return null;
		}
		if(shortestPathDist(src,dest)==-1)
		{
			System.out.println("invalid input");
			return null;
		}
		List<node_data> ans=new ArrayList<>();
		node_data runner=dGraph.getNode(dest);
		while(runner.getKey()!=src)//make us stop after adding drc vertex to the List
		{
			ans.add(new nodeData(runner.getLocation(),runner.getKey(),runner.getWeight()));
			runner=dGraph.getNode(runner.getTag());
		}
		ans.add(dGraph.getNode(src));
		Collections.reverse(ans);
		return ans;
	}

	/**
	 * computes a relatively short path which visit each node in the targets List.
	 * Note: this is NOT the classical traveling salesman problem, 
	 * as you can visit a node more than once, and there is no need to return to source node - 
	 * just a path going over all nodes in the list. 
	 * @param targets
	 * @return
	 */
	@Override
	public List<node_data> TSP(List<Integer> targets) 
	{
		graph subGraph=subGraph(targets); //buils a sub graph containing only relevant verticex and edges.
		if(subGraph==null)return new ArrayList<node_data>();
		Graph_Algo temp=new Graph_Algo(subGraph);
		if(!temp.isConnected())return null;//check if the sub graph is connected.
		HashSet<Integer> bool=new HashSet<Integer>();
		List<node_data> TSP = new LinkedList<node_data>();
		Iterator<Integer> i = targets.iterator();
		int src=i.next();
		TSP.add(0,dGraph.getNode(src));
		while(i.hasNext()) 
		{
			int dest=i.next();
			if(!bool.contains(dest))
			{
				List<node_data> nodePath = shortestPath(src,dest);
				nodePath.remove(0);//avoid duplicates
				TSP.addAll(nodePath);
				//store vertices keys in HashSet to skip the option of trying to visit a vertex we already visited.
				for(node_data n:nodePath)
					bool.add(n.getKey());
				src=dest;
			}
		}
		return TSP;
	}

	/**
	 * this method return a deep copy of current graph.
	 * */
	@Override
	public graph copy() 
	{
		HashMap<Integer,node_data> Nodes=new HashMap<>();
		int EdgesSize=0;
		HashMap<Integer,HashMap<Integer,edge_data>> srcMap=new HashMap<>();

		for(node_data vertex :dGraph.getV()) //iterate over all vertices
		{
			HashMap<Integer,edge_data> map=new HashMap<>();
			int key = vertex.getKey();
			Nodes.put(key, vertex); // input current vertex to Nodes HashMap
			Collection<edge_data> edges=dGraph.getE(key);
			if(edges!=null)
			{
				for(edge_data edge : edges) //iterate over all edges related to vertex 
				{
					map.put(edge.getDest(), edge);
					EdgesSize++;
				}
				srcMap.put(key, map);//input current vertex to srcMap ,i.e current vertex is source
			}
		}

		graph g=new DGraph(Nodes,EdgesSize,srcMap);
		return g;
	}

	@Override
	public String toString()
	{
		return dGraph.toString();
	}
	//====================================Auxiliary methods============================	

	@Override
	public boolean equals(Object o)
	{
		return this.toString().equals(o.toString());
	}
	/**
	 * this method creates sub graph from the current one. 
	 * including only vertices that in targets list,
	 * and edges which their src vertex and dest vertex are in targets list.
	 * 
	 * */
	private graph subGraph(List<Integer> targets) 
	{
		for(Integer key :targets) //make sure that all vertices keys in targets are all part of the full graph
		{
			if(dGraph.getNode(key)==null)
				return null;
		}
		HashMap<Integer,node_data> Nodes=new HashMap<>();
		int EdgesSize=0;
		HashMap<Integer,HashMap<Integer,edge_data>> srcMap=new HashMap<>();

		for(node_data vertex :dGraph.getV()) //iterate over all vertices in the graph
		{
			int key = vertex.getKey();
			if(targets.contains(key)) //if current src vertex exists in subGraph vertices list
			{
				Nodes.put(key, vertex); // input current vertex to Nodes HashMap
				HashMap<Integer,edge_data> map=new HashMap<>();
				Collection<edge_data> edges=dGraph.getE(key);
				if(edges!=null)
				{
					for(edge_data edge : edges) //iterate over all edges related to vertex 
					{
						//add the current edge to new subGraph <==> src vertex and dest vertex exists in subGraph vertices list
						if(targets.contains(edge.getDest()) && targets.contains(edge.getSrc())) 
						{
							map.put(edge.getDest(), edge);
							EdgesSize++;
						}
					}
					srcMap.put(key, map);//input current vertex to srcMap ,i.e current vertex is source
				}
			}
		}
		graph g=new DGraph(Nodes,EdgesSize,srcMap);
		return g;
	}

	/**
	 * Auxiliary recursive function for isConnected method
	 * */
	private void isConnected_Recursive(Collection<edge_data> edge)
	{
		if(edge!=null)
			for(edge_data e : edge)
			{
				if(dGraph.getNode(e.getDest()).getTag() == -1 )
				{
					dGraph.getNode(e.getDest()).setTag(1);
					isConnected_Recursive(dGraph.getE(e.getDest()));
				}
			}
		return;
	}

	private boolean checkTag()
	{
		for(node_data node :  dGraph.getV())
		{ 
			if(node.getTag() == -1)
				return false;
			else
				node.setTag(-1);
		}
		return true;
	}

	/**
	 * Auxiliary method for Dijkstra algorithm
	 * set all vertices Tag to -1
	 * set all vertices Info to FALSE
	 * set all vertices weight to infinity except src vertex(the starting vertex)
	 * @param src
	 * 
	 * */ 
	private void initGraph(int src)
	{
		for(node_data v : dGraph.getV()) 
		{
			v.setTag(-1);//Tag contains the predecessor`s id
			v.setInfo("FALSE");//info contains boolean visited or not
			if(v.getKey()==src)
				v.setWeight(0);//set src vertex`s weight to 0
			else
				v.setWeight(Double.MAX_VALUE);//setting all Nodes weight to infinity
		}
	}
	//==========================Inner=Class=========================

	/**
	 * private inner class for comperator
	 * @method compare - compares between two vertices by weight
	 * 		
	 * */
	private class Vertex_Comperator implements Comparator<node_data> 
	{
		public Vertex_Comperator()
		{

		}
		@Override
		public int compare(node_data v1,node_data v2)
		{
			if(v2.getWeight()<v1.getWeight())
				return 1;
			else return -1;
		}
	}
}