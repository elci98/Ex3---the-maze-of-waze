package gameClient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

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
	private static boolean KMLExporting = false;
	private static String KML_file_name;
	private static int g_number;
	public static final String jdbcUrl="jdbc:mysql://db-mysql-ams3-67328-do-user-4468260-0.db.ondigitalocean.com:25060/oop?useUnicode=yes&characterEncoding=UTF-8&useSSL=false";
	public static final String jdbcUser="student";
	public static final String jdbcUserPassword="OOP2020student";
	public static Connection connection;
	public static Statement statement;

	public autoGaming(int game_number)
	{
		Game_Server.login(311326052);
		game = Game_Server.getServer(game_number);
		g_number = game_number;
		//read data about the chosen game from game server
		dGraph = new DGraph();
		dGraph.init(game.getGraph());
		aGraph.init(dGraph);
		//scale x and y
		rx = findRx(dGraph);
		ry = findRy(dGraph);

		//this thread is in charge of the game
		drawer = new Thread(this, "ADrawer");
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
		if(JOptionPane.showConfirmDialog(null, "turn on KML exporting??", "The Maze of Waze", JOptionPane.YES_OPTION) == JOptionPane.YES_OPTION)
		{
			String input = JOptionPane.showInputDialog("please enter file name");
			if(input != null && input != "")
				startKMLExport(input);
		}
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
			message += "\trobot " + (i++) + " score: " + r.getMoney() +"\n";
		JOptionPane.showMessageDialog(null, message);
		System.exit(0);
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
		StdDraw.picture(0.5, 0.5, "./images/map.png");
		drawGraph(dGraph);
		drawRobots(game.move());
		drawFruits(game.getFruits());
		StdDraw.setPenColor(Color.BLUE);
		Font f=new Font("BOLD", Font.ITALIC, 18);
		StdDraw.setFont(f);
		StdDraw.text(rx.get_max()-0.002, ry.get_max()-0.0005,"time to end: "+ game.timeToEnd() / 1000);
		StdDraw.show();
	}

	/**
	 * This method determine the next step for each robot in the game,<br>
	 * considering the shortest path to the most valuable fruit.
	 * */
	private void placeRobots() 
	{
		Iterator<RobotG> itr = robots_List.iterator();
		while(itr.hasNext())
		{	
			RobotG r = itr.next();
			Fruit fDest = null;
			double maxSum = 0;
			node_data v = null;
			Iterator<Fruit> it = fruits_List.iterator();
			while(it.hasNext())
			{
				Fruit f = it.next();
				double sum =0;
				int dest = f.getEdge().getSrc();
				//				if(r.getSrcNode() == f.getEdge().getDest()) //avoid from steps in place
				//					continue;
				List<node_data> list = aGraph.shortestPath(r.getSrcNode(), dest);
				list.add(dGraph.getNode(f.getEdge().getDest()));
				//sum all edges weight on the way from robot to dest. 
				for(int i = 0;i<list.size()-1;i++)
					sum += dGraph.getEdge(list.get(i).getKey(), list.get(i+1).getKey()).getWeight();
				sum = f.getValue() / sum;
				if(sum > maxSum && !f.isDest())
				{
					maxSum = sum;
					// we disabled the option to check the shortestPath from vertex to itself, it guarantees that the 1 index will be occupied
					v = list.get(1); 
					fDest = f;
				}
			}
			if(fDest != null)
			{
				fDest.setDest(true);
				game.chooseNextEdge(r.getID(), v.getKey());
			}
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
		if(robots == null) return;
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
			StdDraw.setPenColor(242, 19, 227);
			StdDraw.point(r.getLocation().x(), r.getLocation().y());
			StdDraw.text(rx.get_max() - 0.002 - 0.0035*i, ry.get_max()-0.0005, 
					"\t  robot "+ (i++) + " score: " + (int)r.getMoney()+"   \t\t");
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

	/**
	 * Receives from the AWT thread(GUI Window) the order to create a new KML file.
	 * @param file_name 
	 * */
	public static void startKMLExport(String file_name) 
	{
		if(!file_name.endsWith(".kml") && !file_name.endsWith(".KML"))
			file_name += ".kml";
		KML_file_name = KML_Logger.createFile(file_name, g_number);
		KMLExporting = true;
	}

	/**
	 * @return true if we already writing to KML file.
	 * */
	public static boolean KMLexporting()
	{
		return KMLExporting;
	}
	/**
	 * opens a JFrame window to present details on requested ID.<br>
	 * the details received from series of queries to the DB server.
	 * */
	public static void showSQLTable()
	{
		Thread sqlThread = new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				int games=0, uID;
				boolean flag = false;
				int [] Score={125,436,0,713,0,570,0,0,0,480,0,1050,0,310,0,0,235,0,0,250,200,0,0,1000};
				int [] Moves={290,580,0,580,0,500,0,0,0,580,0,580,0,580,0,0,290,0,0,580,290,0,0,1140};
				//the data matrix will be displayed on the JTable
				Object[][] data = new Object[24][5];
				String[] columnNames = {"level", "best score", "best moves", "games played", "level ranking"};
				//get input from user
				String input = JOptionPane.showInputDialog("please enter user ID");
				if(input != null && input != "")
					uID = Integer.parseInt(input);
				else
					uID = 311326052; // my id as default
				//generate a query string
				String query = "SELECT * FROM Logs WHERE UserID =" + uID;
				//creating connection with the SQL server
				sqlConnect();
				try 
				{
					int bestScore = 0, lastLevel = 0; 
					//executing query to SQL server
					ResultSet rs = statement.executeQuery(query);
					if(rs.next())
						flag = true;
					rs.beforeFirst();
					while (rs.next())
					{
						int currentLevel = rs.getInt("levelID"); // parse level
						if(currentLevel < 0 || Score[currentLevel] == 0)
							continue;
						if(currentLevel > lastLevel)
						{
							if(Score[lastLevel] != 0)
								data[lastLevel][3] = games;
							lastLevel = currentLevel;
							bestScore = 0;
							games = 0;
						}
						if(currentLevel < lastLevel && (int)data[currentLevel][3] >0)//Handles the case of playing lower game
						{
							games = (int)data[currentLevel][3]-1;
							bestScore = (int)data[currentLevel][1];
						}
						games++;
						int currentScore = rs.getInt("score"); // parse score
						int currentMoves= rs.getInt("moves"); // parse moves
						if(currentScore >= bestScore && currentScore >= Score[currentLevel] && currentMoves <= Moves[currentLevel] || 
								currentScore >= bestScore && currentMoves <= Moves[currentLevel])
						{
							data[currentLevel][0] = currentLevel;
							data[currentLevel][1] = currentScore;
							data[currentLevel][2] = currentMoves;
							bestScore = currentScore;
						}
					}
					data[lastLevel][3] = games;
					if(flag)
					{
						int[] users = new int[24];
						for(int i = 0;i<24; i++)
						{
							if(Score[i] == 0 || data[i][2] == null)
								continue;
							int place = 0;
							query = "SELECT * FROM oop.Logs where levelID = "+i+" and score >= "+Score[i]+" and moves <= "+Moves[i];
							rs = statement.executeQuery(query);
							while (rs.next())
							{
								int userID = rs.getInt("userID");
								if(userID == 0)
									continue;
								users[i]++;
								if((int)data[i][1] > rs.getInt("score"))
								{
									place++;
								}
							}
							data[i][4] = place + " from "+ users[i];
						}
					}
					statement.close();
					rs.close();
					connection.close();
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
				if(flag)
				{
					JFrame frame = new JFrame("sql frame");
					JTable jt = new JTable(data, columnNames) {
						private static final long serialVersionUID = 1L;

						public boolean isCellEditable(int row, int column) {                
							return false;               
						};
					};
					//JFrame properties
					frame.setLayout(new BorderLayout());
					frame.add(jt.getTableHeader(), BorderLayout.PAGE_START);
					frame.add(jt, BorderLayout.CENTER);
					frame.setResizable(false);
					frame.setSize(400, 400);
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setAutoRequestFocus(true);
					frame.setVisible(true);
				}
			}

		},"sqlThread");
		sqlThread.start();

	}
	/**
	 * Auxiliary method to create an SQL connection.
	 * */
	private static void sqlConnect() 
	{
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcUserPassword);
			statement = connection.createStatement();
		} 
		catch (ClassNotFoundException | SQLException e) 
		{
			e.printStackTrace();
		}

	}

}
