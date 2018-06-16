package myServer;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Request 
{
	private File pathFile;
	private HashMap<String, String> map;
	
	/*
	 * Constructor for Client requests
	 * @param a client socket, and a client inputStream
	 * This constructor will get the path that the client has requested
	 * If the client has no such element in their header request, redirects the client to the index page
	 */
	public Request(InputStream clientInput) throws BadRequestException
	{			
		@SuppressWarnings("resource") // gets rid of warning for not closing scanner
		Scanner clientScanner = new Scanner(clientInput);
		map = new HashMap<>();
		String[] pathData = null;

		if(!clientScanner.hasNextLine())
			throw new BadRequestException("No next Line");
		
		// split the first line
		pathData = clientScanner.nextLine().split(" ");
		if (pathData.length != 3)
			throw new BadRequestException("Request is not length three. It is length: " + pathData.length);
		else if (!(pathData[0].equals("GET")))
			throw new BadRequestException("Request does not start with GET");
		else if (!(pathData[2].equals("HTTP/1.1")))
			throw new BadRequestException("Request does not end with HTTP/1.1");
		
		
		// create a string from the path
		String path = pathData[1];

		// if it is a default path make it go to the index
		if (path.equals("/"))
			path = "chatLogin.html";

		if(path.length() > 20) {
			path = path.substring(0, 20);
		}
		
		// add resources to the path name for the file
		pathFile = new File("resources/" + path);
				
		while (true) {
			String[] nextLine = clientScanner.nextLine().split(": ");
			if(nextLine[0].equals(""))
				break;
			map.put(nextLine[0], nextLine[1]);
		}

	}
	
	/*
	 * Returns the File that the client has requested.
	 */
	public File getPathFile()
	{
		return pathFile;
	}
	
	public HashMap<String, String> getMap()
	{
		return map;
	}
}
