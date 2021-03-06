package gameClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;


public class KML_Logger
{
	private static int time;
	
	/**
	 * method to create a new KML file which starts with KML file format.
	 * @param file_name - recive from user the desired file name.
	 * @param game_number - in order to determine the game length we need to now if game number is even number or odd number.
	 * @return the final file name
	 * */
	public static String createFile(String file_name, int game_number)
	{
		time = game_number % 2 == 1? 60000 : 30000;
		File f = new File(file_name);
		if(!f.exists())
		{
			System.out.println("KML file saved as: "+file_name);
			String out = "";
			try 
			{
				f.createNewFile();
				BufferedWriter bw = new BufferedWriter(new PrintWriter(new FileWriter(f)));
				//every KML file MUST start with those two lines
				out += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"; 
				out += "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\r\n";
				out += "<Document>\r\n";
				//style for Apple
				out += "<Style id=\"AppleIcon\">\r\n";
				out += "<Icon>\r\n";
				out += "<href>http://maps.google.com/mapfiles/kml/shapes/firedept.png</href>\r\n";
				out += "</Icon>\r\n";	
				out += "</Style>\r\n";
				//style for Banana
				out += "<Style id=\"BananaIcon\">\r\n";
				out += "<Icon>\r\n";
				out += "<href>http://maps.google.com/mapfiles/kml/shapes/convenience.png</href>\r\n";
				out += "</Icon>\r\n";	
				out += "</Style>\r\n";
				//style for Robot
				out += "<Style id=\"RobotIcon\">\r\n";
				out += "<Icon>\r\n";
				out += "<href>http://maps.google.com/mapfiles/kml/shapes/cabs.png</href>\r\n";
				out += "</Icon>\r\n";	
				out += "</Style>\r\n\r\n";
				bw.write(out);
				bw.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			return file_name;
		}
		else
		{
			int i = file_name.indexOf(".");
			System.out.println("please check your file name to avoid exceptions");
			return createFile(file_name.substring(0, i) + i + ".kml",game_number);
		}
	}
	
	/**
	 * this method writes to existing KML file.
	 * @param file_name - the KML file to write to.
	 * @param x - x value of the object.
	 * @param y - y value of the object.
	 * @param objType - the written object type: in order to give specific icon to every type of object.
	 * @param timeToEnd - helps to give the correct timeSpan for this object.
	 * @throws FileNotFoundException - if file_name does not exists
	 * */
	public static void write(String file_name, double x, double y, String objType, long TimeToEnd)throws FileNotFoundException
	{
		long  t = (time - TimeToEnd)/1000;
		String out = "";
		File f = new File(file_name);
		if(!f.exists())
			throw new FileNotFoundException("error this file does not exist!");
		try 
		{
			BufferedWriter bw = new BufferedWriter(new PrintWriter(new FileWriter(f, true)));
			out += "<Placemark>\r\n";
			out += "<TimeSpan>\r\n<begin>" + t + "</begin>\r\n<end>" + (t+0.1) + "</end>\r\n</TimeSpan>\r\n";
			switch(objType)
			{
			case "Banana":
			{
				out += "<styleUrl>#BananaIcon</styleUrl>\r\n";
				break;
			}
			case "Apple":
			{
				out += "<styleUrl>#AppleIcon</styleUrl>\r\n";
				break;
			}
			case "Robot":
			{
				out += "<styleUrl>#RobotIcon</styleUrl>\r\n";
				break;
			}
			}
			out += "<TimeStamp>\r\n<when>"+ LocalDateTime.now() +"Z</when>\r\n</TimeStamp>\r\n";
			out += "<Point>\r\n";
			out += "<coordinates>"+ x + ", " + y + ", 0</coordinates>\r\n</Point>\r\n";
			out += "</Placemark>\r\n\r\n";
			bw.write(out);
			bw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * closes file_name properly as KML file.
	 * @param file_name - the KML file to close.
	 * @throws IOException if file does not start with the proper KML file deceleration.
	 * */
	public static void closeFile(String file_name) throws IOException 
	{
		File f = new File(file_name);
		if(!f.exists())
			throw new FileNotFoundException("error this file does not exist!");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String s = br.readLine();
		if(!s.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
		{
			br.close();
			throw new IOException("wrong file format ");
		}
		s = br.readLine();
		if(!s.equals("<kml xmlns=\"http://www.opengis.net/kml/2.2\">"))
		{
			br.close();
			throw new IOException("wrong file format ");
		}
		s = br.readLine();
		if(!s.equals("<Document>"))
		{
			br.close();
			throw new IOException("wrong file format ");
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new PrintWriter(new FileWriter(f, true)));
		String out = "";
		out += "</Document>\r\n";
		//		out += "</Folder>\r\n";
		out += "</kml>";
		bw.write(out);
		bw.close();
		f.setWritable(false);
	}
}
