package gameClient;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;

import Server.Game_Server;
import Server.game_service;
import algorithms.Graph_Algo;
import dataStructure.DGraph;
import dataStructure.edge_data;
import dataStructure.graph;
import dataStructure.node_data;
import elements.Fruit;
import elements.RobotG;
import utils.Point3D;
import utils.Range;
import utils.StdDraw;

public class autoGaming implements Runnable
{
	private game_service game;
	private Thread drawer;
	private DGraph dGraph;
	private Graph_Algo aGraph = new Graph_Algo();
	private Range rx, ry;
	private List<Fruit> fruits_List;
	private List<RobotG> robots_List;
	private final static double epsilon = 0.0000001;
	private final static double epsilon2 = 0.00019;

	public autoGaming()
	{
		StdDraw.init();
		//open input window for game number
		while(true)
		{
			String temp = JOptionPane.showInputDialog(null, "please choose a game between 0-23");
			int game_number = temp != null ? Integer.parseInt(temp) : -1;
			if(game_number >= 0  && game_number <= 23 )
			{
				game = Game_Server.getServer(game_number);
				break;
			}
			else if(game_number == -1)
				System.exit(0);
			else 
			{
				JOptionPane.showMessageDialog(null, "invalid input, please choose between 0-23", "Error", JOptionPane.INFORMATION_MESSAGE);
				continue;
			}
		}
		//read data about the chosen game from game server
		dGraph = new DGraph();
		dGraph.init(game.getGraph());
		aGraph.init(dGraph);
		System.out.println(game.toString());
		//scale x and y
		rx = findRx(dGraph);
		ry = findRy(dGraph);

		//this thread is in charge of the game
		drawer = new Thread(this, "Drawer");
		drawer.start();
	}

	@Override
	public void run() 
	{
		drawGraph(dGraph);
		int robots_num = getRobotsNumber(game.toString());
		drawFruits(game.getFruits());
		placeRobots(robots_num);
		drawRobots(game.getRobots());
		if(JOptionPane.showConfirmDialog(null, "press YES to start the game", "TheMaze of Waze", JOptionPane.YES_OPTION) != JOptionPane.YES_OPTION)
			System.exit(0);
		game.startGame();
		StdDraw.setFont();
		StdDraw.setPenColor(Color.BLUE);
		StdDraw.text( rx.get_max()-0.002, ry.get_max()-0.0005,"time to end: "+game.timeToEnd()/1000);
		while(game.isRunning())
		{
			placeRobots();
			RefreshFrame();
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}



	/* ****************************************************
	 *      ************Auxiliary functions***********
	 *          *********************************        */

	/**
	 * this method in charge of repaint the frame over and over
	 * */
	private void RefreshFrame()
	{
		StdDraw.enableDoubleBuffering();
		StdDraw.clear();
		StdDraw.setScale();
		StdDraw.picture(0.5, 0.5, "map.png");
		drawGraph(dGraph);
		drawFruits(game.getFruits());
		drawRobots(game.move());
		StdDraw.setPenColor(Color.BLUE);
		Font f=new Font("BOLD", Font.ITALIC, 18);
		StdDraw.setFont(f);
		StdDraw.text(rx.get_max()-0.002, ry.get_max()-0.0005,"time to end: "+ game.timeToEnd() / 1000);
		StdDraw.show();
	}

	/**
	 * 
	 * */
	private void placeRobots() 
	{
		for(RobotG r : robots_List)
		{	
			double maxSum = 0;
			node_data v = null;
			for(Fruit f : fruits_List)
			{
				double sum =0;
				int dest = f.getEdge().getDest();
				if(dest != r.getSrcNode())
				{
					List<node_data> list = aGraph.shortestPath(r.getSrcNode(), dest);
					System.out.println(list);
					//sum all edges weight on the way from robot to dest. 
					for(int i =0;i<list.size()-1;i++)
						sum += dGraph.getEdge(list.get(i).getKey(), list.get(i+1).getKey()).getWeight();
					sum = f.getValue() / sum;
					if(sum > maxSum)
					{
						maxSum = sum;
						v = list.get(1); // index 0 is the src vertex so we take the next one
					}
				}
			}
			System.out.println("robot: "+r.getSrcNode() + " dest: "+v.getKey());
			game.chooseNextEdge(r.getID(), v.getKey());
		}
	}

	/**
	 * this method find the best starting place for each robot.<br>
	 * considering an edge that has fruit on, <br>
	 * calculating fruit`s value divided by the time to walk through this edge (from src to dest).<br>
	 * the first robot will be placed on the src that the edge going out from him got the best results.
	 * @param robots_num - number of robots to place
	 * */
	private void placeRobots(int robots_num) 
	{

		edge_data currentEdge;
		int vNumber = 0;
		double  valuePerSecond, lastResult = Double.MAX_VALUE;
		for(int i = 0;i<robots_num;i++)
		{
			double maxValuePerSecond = 0;
			for(Fruit f : fruits_List)
			{
				currentEdge = f.getEdge();
				valuePerSecond = f.getValue() / currentEdge.getWeight(); //calculate the value per second while "walking" on this edge.
				if(valuePerSecond > maxValuePerSecond && valuePerSecond < lastResult)
				{
					maxValuePerSecond = valuePerSecond;
					vNumber = currentEdge.getSrc();
				}
			}
			lastResult = maxValuePerSecond;
			game.addRobot(vNumber);
		}

	}

	/**
	 * this method iterate over all robots`s JSON string that given from server,
	 * <br> and fill robots_List with robots objects.
	 * when finished call method drawRobots() to draw the functions
	 * @param robots - List of JSON strings, each represent a single robot.
	 * */
	private void drawRobots(List<String> robots) 
	{
		robots_List = new ArrayList<>();
		Iterator<String> i = robots.iterator();
		while(i.hasNext())
		{
			RobotG r = getRobot(i.next());
			robots_List.add(r);
		} 
		drawRobots();
	}

	/**
	 * auxiliary function to parse JSON string representing a single robot,
	 * <br> using the parsed data it builds robot object
	 * @param JSONRobot - JSON string representing robot.
	 * @return new robot object with the data inside the JSON string
	 * */
	private RobotG getRobot(String JSONRobot) 
	{
		Point3D pos = null;
		int src=0, dest=0,ID=0;
		double money = 0;
		try 
		{
			org.json.JSONObject jo = new org.json.JSONObject(JSONRobot);
			org.json.JSONObject robot = (JSONObject) jo.get("Robot");
			src = robot.getInt("src");
			dest = robot.getInt("dest");
			ID = robot.getInt("id");
			money = robot.getDouble("value");
			pos = new Point3D(robot.getString("pos"));
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		return new RobotG(src, ID, pos, dest, money);
	}

	/**
	 * this method draw a small filled circle for each robot in the game <br>
	 * each filled circle represent current robot`s location
	 * */
	private void drawRobots() 
	{
		int i = 1;
		for(RobotG r : robots_List)
		{
			StdDraw.setPenRadius(0.040);
			StdDraw.setPenColor(Color.MAGENTA);
			StdDraw.point(r.getLocation().x(), r.getLocation().y());
			StdDraw.setPenColor(242, 19, 227);
			StdDraw.text(rx.get_max() - 0.002 - 0.0035*i, ry.get_max()-0.0005, 
					"robot "+ (i++) + " score: " + r.getMoney());

		}
	}


	/**
	 * this method iterate over all fruit`s JSON string that given from server,
	 * and fill fruits_List with fruits objects <br>
	 * * when finished call method drawFruits() to draw the fruits
	 * @param fruits - List of JSON strings, each represent fruit.
	 * */
	private void drawFruits(List<String> fruits) 
	{
		fruits_List = new ArrayList<>();
		Iterator<String> it = fruits.iterator();
		while(it.hasNext())
		{
			Fruit f = getFruit(it.next());
			fruits_List.add(f);
		}
		drawFruits();
	}

	/**
	 * places a small icon on location fruit`s coordinations
	 * <br>
	 * apple icon for fruit type 1 and banana icon for -1 fruit type
	 * */
	private void drawFruits() 
	{
		for(Fruit f : fruits_List)
		{
			String fruit_icon = f.getType() == 1 ? "./apple.png" : "./banana.png";
			StdDraw.picture(f.getLocation().x(), f.getLocation().y(), fruit_icon);
		}
	}

	/**
	 * auxiliary function to parse JSON string representing fruit,
	 * using the parsed data it builds fruit object
	 * @param JSONFruit - JSON string representing fruit.
	 * @return new fruit object with the data inside the JSON string
	 * */
	private Fruit getFruit(String JSONFruit) 
	{
		double value = 0;
		Point3D p=null;
		int type = 0;
		try 
		{
			org.json.JSONObject jo = new org.json.JSONObject(JSONFruit);
			org.json.JSONObject fruit = (JSONObject) jo.get("Fruit");
			value =  fruit.getDouble("value");
			p = new Point3D( (String)fruit.get("pos") );
			type = fruit.getInt("type");
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		return new Fruit(value, p, findEdge(p, type));
	}

	/**
	 * this method iterate over all vertices in current graph,
	 * trying to find the edge which the given fruit belongs to.
	 * @param Fruit : the location of the fruit we want to locate it`s edge.
	 * @param type : 1 for banana and -1 for apple (determined by edge direction).
	 * @return the edge which the fruit in the given location belongs to, null if there is no such one.
	 * */
	private edge_data findEdge(Point3D Fruit, int type)//determine whether a given fruit exist on current edge
	{
		for(node_data src: dGraph.getV())
		{
			if(dGraph.getE(src.getKey()) != null)
			{
				for(edge_data edge:dGraph.getE(src.getKey()))
				{
					node_data dest = dGraph.getNode(edge.getDest());
					double fruitToSrc = Fruit.distance2D(src.getLocation());
					double fruitToDest = Fruit.distance2D(dest.getLocation());
					double srcToDest = src.getLocation().distance2D( dest.getLocation());
					if(fruitToSrc + fruitToDest - srcToDest < epsilon)
					{
						if(type == 1 && src.getKey() < dest.getKey())
							return edge;
						if(type == -1 && src.getKey() > dest.getKey())
							return edge;
					}
				}
			}
		}
		return null;
	}

	/**
	 * parse from JSON how many robots are in the chosen game.
	 * @param - JSON string contains the robots number
	 * @return robots number in the current game
	 * */
	private static int getRobotsNumber(String s) 
	{
		try 
		{
			org.json.JSONObject jo = new org.json.JSONObject(s);
			org.json.JSONObject gs = (org.json.JSONObject)jo.get("GameServer");
			return gs.getInt("robots");
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * this method draw a given graph on GUI window using StdDraw lib
	 * @param g - the given graph
	 * */
	private void drawGraph(graph G)
	{
		StdDraw.setXscale(rx.get_min(),rx.get_max());
		StdDraw.setYscale(ry.get_min(),ry.get_max());
		StdDraw.setPenColor(Color.BLACK);
		for(node_data vertex:G.getV())
		{
			double x0=vertex.getLocation().x();
			double y0=vertex.getLocation().y();
			if(G.getE(vertex.getKey())!=null)
			{
				for(edge_data edge:G.getE(vertex.getKey()))
				{
					StdDraw.setPenRadius(0.0015);
					StdDraw.setPenColor(Color.orange);
					Font f=new Font("BOLD", Font.ITALIC, 18);
					StdDraw.setFont(f);
					double x1=G.getNode(edge.getDest()).getLocation().x();
					double y1=G.getNode(edge.getDest()).getLocation().y();

					//draw edges
					StdDraw.line(x0, y0, x1, y1);
					StdDraw.setPenRadius(0.02);

					//draw direction points
					StdDraw.setPenColor(Color.GREEN);
					StdDraw.point(x0*0.1+x1*0.9, y0*0.1+y1*0.9);

					//draw dest vertex
					StdDraw.setPenColor(Color.RED);
					StdDraw.point(x1, y1);

					//draw vertices weights
					StdDraw.setPenColor(Color.BLACK);
					StdDraw.text(x0,y0 + epsilon2, vertex.getKey()+"");

					//draw edges weight
					//					StdDraw.setPenColor(Color.BLACK);
					//					StdDraw.text((x0+x1)/2, (y0+y1)/2,edge.getWeight()+"");
				}
			}
			StdDraw.setPenRadius(0.02);
			StdDraw.setPenColor(Color.RED);
			StdDraw.point(x0, y0);
		}
	}

	/**
	 * iterate over all vertices in given graph to find min and max x values
	 * @param g - the given graph
	 * @return new Range object with min and max x values	
	 * */
	private Range findRx(graph g) 
	{
		double minX=0, maxX=0;
		boolean flag = true;
		for(node_data node :g.getV())
		{
			double x = node.getLocation().x();
			if(flag)
			{
				minX = x;
				maxX = x;
				flag = false;
			}

			if(x < minX) minX = x;
			if(x > maxX) maxX = x;
		}
		double diff = (maxX-minX);
		return new Range(minX - diff / 10, maxX + diff / 10);
	}

	/**
	 * iterate over all vertices in given graph to find min and max y values
	 * @param g - the given graph
	 * @return new Range object with min and max y values	
	 * */
	private Range findRy(graph g) 
	{
		double minY=0, maxY=0;
		boolean flag = true;
		for(node_data node :g.getV())
		{
			double y = node.getLocation().y();
			if(flag)
			{
				minY = y;
				maxY = y;
				flag = false;
			}
			if(y < minY) minY = y;
			if(y > maxY) maxY = y;
		}
		double diff = maxY - minY;
		return new Range(minY - diff / 10, maxY + diff / 10);
	}

	public static void main(String[] args) 
	{
		new autoGaming();
	}


}
