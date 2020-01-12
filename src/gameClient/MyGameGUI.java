package gameClient;

import javax.swing.JOptionPane;

import Server.Game_Server;
import Server.game_service;
import elements.RobotG;
import utils.StdDraw;

public class MyGameGUI implements Runnable 
{
	Thread drawer;
	public MyGameGUI()
	{
		drawer = new Thread(this, "drawer");
		drawer.start();
	}

	game_service game;
	@Override
	public void run() 
	{
		String temp = JOptionPane.showInputDialog(null, "please choose a game between 0-23");
		int game_number = temp != null ? Integer.parseInt(temp) : -1;
		if(game_number >= 0  && game_number <= 23 )
			game = Game_Server.getServer(game_number);
		else if(game_number == -1)
			System.exit(0);
		else 
		{
			JOptionPane.showMessageDialog(null, "invalid input, please choose between 0-23", "Error", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		}
		StdDraw.init(game);

		while(game.isRunning())
		{
			StdDraw.RefreshFrame(game.timeToEnd());
		}
		String message = "";
		int i = 1;
		for(RobotG r : StdDraw.robots_List)
			message += "robot " + (i++) + " score: " + r.getMoney() +"\n";
		JOptionPane.showMessageDialog(null, message);
		System.exit(0);
	}

	public void start()
	{
		
		
	}


	public static void main(String[] args)
	{
		new MyGameGUI();
	}
}
