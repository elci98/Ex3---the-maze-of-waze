package gameClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class KML_Logger
{
	public static void createFile(String file_name)
	{
		File f = new File(file_name);
		if(!f.exists())
		{
			try 
			{
				f.createNewFile();
				BufferedWriter buffer = new BufferedWriter(new FileWriter(f));
				//every KML file MUST start with those two lines
				buffer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"); 
				buffer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
				buffer.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	public static void generateKML(String file_name, double x, double y, String name, String description)throws FileNotFoundException
	{
		File f = new File(file_name);
		if(!f.exists())
			throw new FileNotFoundException("error this file does not exist!");
		try 
		{
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file_name,true));
			buffer.write("\r\n<Placemark>\r\n");
			buffer.write("<name>" + name + "</name>\r\n");
			buffer.write("<description>" + description + "</description>\r\n");
			buffer.write("<Point>\r\n" + "<coordinates>"+ x + ", " + y + ", 0</coordinates>\r\n</Point>\r\n");
			buffer.write("</Placemark>\r\n");
			buffer.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	private static void closeFile(String file_name) throws IOException 
	{
		File f = new File(file_name);
		if(!f.exists())
			throw new FileNotFoundException("error this file does not exist!");
		BufferedReader bf = new BufferedReader(new FileReader(f));
		String s = bf.readLine();
		if(!s.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
		{
			bf.close();
			throw new IOException("wrong file format ");
		}
		s = bf.readLine();
		if(!s.equals("<kml xmlns=\"http://www.opengis.net/kml/2.2\">"))
		{
			bf.close();
			throw new IOException("wrong file format ");
		}
		bf.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(file_name,true));
		bw.write("</kml>");
		bw.close();

	}
	//	 <Placemark>
	//	    <name>Simple placemark</name>
	//	    <description>Attached to the ground. Intelligently places itself 
	//	       at the height of the underlying terrain.</description>
	//	    <Point>
	//	      <coordinates>-122.0822035425683,37.42228990140251,0</coordinates>
	//	    </Point>
	//	  </Placemark>
	//	</kml>

	public static void main(String[] args) 
	{
		createFile("test.kml");
		try 
		{
			generateKML("test.kml", 35.187594216303474,32.10378225882353, "test point", "this is my first gnerating kml file try");
			closeFile("test.kml");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}



}
