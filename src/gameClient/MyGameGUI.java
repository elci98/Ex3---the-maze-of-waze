package gameClient;

import java.awt.Color;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;

import Server.Game_Server;
import Server.game_service;
import dataStructure.DGraph;
import dataStructure.edge_data;
import dataStructure.graph;
import dataStructure.node_data;
import elements.Fruit;
import elements.RobotG;
import utils.Point3D;
import utils.Range;
import utils.StdDraw;

public class MyGameGUI implements Runnable
{
	private game_service game;
	private Thread drawer;
	private DGraph dGraph;
	private Range rx, ry;
	private List<Fruit> fruits_List;
	private List<RobotG> robots_List;
	private static double userX = 0;
	private static double userY = 0;
	private final static double epsilon = 0.0000001;
	private final static double epsilon2 = 0.00019;
	private static int game_number;
	private static boolean KMLExporting = false;
	private static String KML_file_name;

	public MyGameGUI()
	{
		StdDraw.init();
		//open input window for game number
		while(true)
		{
			String input = JOptionPane.showInputDialog(null, "please choose a game between 0-23");
			if(input!=null && !input.equals(""))
				game_number =  Integer.parseInt(input);
			else 
				System.exit(0);
			if(game_number >= 0  && game_number <= 23 || game_number == -1 || game_number == -31 || game_number == -331)
			{
				break;
			}
			else 
			{
				JOptionPane.showMessageDialog(null, "invalid input, please choose between 0-23", "Error", JOptionPane.INFORMATION_MESSAGE);
				continue;
			}
		}
		String [] choose = {"auto Game", "Manual Game"};
		String gameMode =(String) JOptionPane.showInputDialog(null, "Please  choose  game  mode", "The Maze of Waze", JOptionPane.INFORMATION_MESSAGE,  null, choose, choose[0]);
		if(gameMode == null || gameMode == "")
			System.exit(0);
		if((gameMode.equals("Manual Game")))
		{
			game = Game_Server.getServer(game_number);
			//read data about the chosen game from game server
			dGraph = new DGraph();
			dGraph.init(game.getGraph());
			//scale x and y
			rx = findRx(dGraph);
			ry = findRy(dGraph);

			//this thread is in charge of the game
			drawer = new Thread(this, "MGDrawer");
			drawer.start();
		}
		else if((gameMode.equals("auto Game")))
			new autoGaming(game_number);
	}
	@Override
	public void run() 
	{
		drawGraph(dGraph);
		int robots_num = getRobotsNumber(game.toString());
		askRobotsLocation(robots_num); //ask from user to place the robots and add the chosen vertex to game
		drawRobots(game.getRobots());
		if(JOptionPane.showConfirmDialog(null, "turn on KML exporting??", "The Maze of Waze", JOptionPane.YES_OPTION) == JOptionPane.YES_OPTION)
		{
			String input = JOptionPane.showInputDialog("please enter file name");
			if(input != null && input != "")
				startKMLExport(input);
		}
		game.startGame();
		System.out.println(game.timeToEnd());
		StdDraw.setFont();
		StdDraw.setPenColor(Color.BLUE);
		StdDraw.text( rx.get_max()-0.002, ry.get_max()-0.0005,"time to end: "+game.timeToEnd()/1000);
		double oldUserX = userX;
		double oldUserY = userY;
		while(game.isRunning())
		{

			if(userX != oldUserX && userY != oldUserY)
			{
				node_data dest = searchForDest(userX, userY);
				RobotG r = dest != null ? searchForRobot(dest) : null;
				if(r!= null && dest != null)
					game.chooseNextEdge(r.getID(), dest.getKey());
				oldUserX = userX;
				oldUserY = userY;
			}
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
		if(KMLExporting)
		{	
			try
			{
				KML_Logger.closeFile(KML_file_name);
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		String message = "";
		int i = 1;
		for(RobotG r : robots_List)
			message += "robot " + (i++) + " score: " + r.getMoney() +"\n";
		JOptionPane.showMessageDialog(null, message);
		System.exit(0);

	}






	/* **************************************************
	 *               Auxiliary functions
	 ********************************************************/

	/**
	 * searches for the closest robot to dest vertex.
	 * @param dest - the vertex the user wants to move some robot to.
	 * @return the closest robot to dest which has edge from its position to dest position.
	 * */
	private RobotG searchForRobot(node_data dest) 
	{

		List<RobotG> robots = new ArrayList<>();
		for(RobotG r : robots_List)
		{
			if(dGraph.getEdge(r.getSrcNode(), dest.getKey()) != null)
				robots.add(r);
		}
		if(robots.isEmpty()) return null;
		if(robots.size() == 1 )
			return robots.get(0);
		else
		{
			RobotG closest =robots.get(0); 
			for(RobotG r : robots)
			{
				double oldWeight = dGraph.getEdge(closest.getSrcNode(), dest.getKey()).getWeight();
				double newWeight = dGraph.getEdge(r.getSrcNode(), dest.getKey()).getWeight();
				if(newWeight < oldWeight)
					closest = r;
			}
			return closest;
		}
	}

	/**
	 * iterate over all vertices to find the closest to user`s click.
	 * @return the closest vertex to user mouse click.
	 * */
	private node_data searchForDest(double mouseX, double mouseY) 
	{
		Point3D p = new Point3D(mouseX, mouseY);
		node_data closest = null;
		boolean first = true;
		for(node_data v: dGraph.getV())
		{
			if(first)
			{
				closest = v;
				first = false;
			}
			else
			{
				double oldDiff = (double)Math.round(closest.getLocation().distance2D(p)* 10000000000000000d) / 10000000000000000d;
				double newDiff = (double)Math.round(v.getLocation().distance2D(p)* 10000000000000000d) / 10000000000000000d;
				if(newDiff < oldDiff && newDiff < 0.002)
					closest = v;
			}
		}
		if(closest.getLocation().distance2D(p) < 0.002)
			return closest;
		return null;
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
				}
			}
			StdDraw.setPenRadius(0.02);
			StdDraw.setPenColor(Color.RED);
			StdDraw.point(x0, y0);
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
			if(KMLExporting)
			{
				try 
				{
					KML_Logger.write(KML_file_name, r.getLocation().x(), r.getLocation().y(), "Robot", game.timeToEnd());
				} 
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}
			}
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
	 * and fill fruits_List with fruits objects.
	 * when finished call method drawFruits() to draw the fruits
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
			if(KMLExporting)
			{
				String objType = f.getType() == 1 ? "Apple" : "Banana";
				try 
				{
					KML_Logger.write(KML_file_name, f.getLocation().x(), f.getLocation().y(), objType, game.timeToEnd());
				} 
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}
			}
		} 
		drawFruits();
	}

	/**
	 * auxiliary function to parse JSON string representing a singlefruit,
	 * <br> using the parsed data it builds fruit object
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
	 * places a small icon on location fruit`s coordinations
	 * <br>
	 * apple icon for fruit type 1 and banana icon for -1 fruit type
	 * */
	private void drawFruits() 
	{
		for(Fruit f : fruits_List)
		{
			String fruit_icon = f.getType() == 1 ? "./images/apple.png" : "./images/banana.png";
			StdDraw.picture(f.getLocation().x(), f.getLocation().y(), fruit_icon);
		}
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
						if(type == -1 && src.getKey() > dest.getKey() )
							return edge;
						if(type == 1 && src.getKey() < dest.getKey())
							return edge;
					}
				}
			}
		}
		return null;
	}

	/**
	 * parse from JSON how many robots are in the chosen game
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
	 * opens a JOptionPane window to receive from user the starting location of each robot.
	 * */
	private void askRobotsLocation(int numberOfRobots) 
	{
		String s = "";
		while (numberOfRobots > 0)
		{
			s = JOptionPane.showInputDialog(null, "please choose starting vertex for robot number "+numberOfRobots);
			if(s == null || s == "")
				System.exit(0);
			if(dGraph.getNode(Integer.parseInt(s))== null)continue;
			node_data v = dGraph.getNode(Integer.parseInt(s));
			game.addRobot(v.getKey());
			numberOfRobots--;
		}
	}
	/**
	 * this method in charge of repaint the frame over and over
	 * */
	private void RefreshFrame()
	{
		StdDraw.enableDoubleBuffering();
		StdDraw.clear();
		StdDraw.setScale();
		StdDraw.picture(0.5, 0.5, "./images/map.png");
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
	/**
	 * when mouse click is released the AWT thread call this function,<br>
	 * in order to update the new coordinations of user mouse click.
	 * */
	public static void updateXY(double mouseX, double mouseY) 
	{
		userX = mouseX;
		userY = mouseY;
	}

	/**
	 * Receives from the AWT thread(GUI Window) the order to create a new KML file.
	 * @param file_name 
	 * */
	public static void startKMLExport(String file_name) 
	{
		if(!file_name.endsWith(".kml") && !file_name.endsWith(".KML"))
			file_name += ".kml";
		KML_file_name = KML_Logger.createFile(file_name, game_number);
		KMLExporting = true;
	}

	/**
	 * @return true if we already writing to KML file.
	 * */
	public static boolean KMLexporting()
	{
		return KMLExporting;
	}
	public static void main(String[] args) 
	{
		new MyGameGUI();
	}
}