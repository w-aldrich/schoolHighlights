package myServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.SocketException;
import java.nio.channels.Pipe;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;

public class Response {
	private OutputStream serverOutput;
	private PrintWriter output;
	private File pathFile;
	private FileInputStream inputHtml;
	private HashMap<String, String> map;
	private Connection client;
	private Pipe pipe;

	/*
	 * Constructor for Server Response
	 * 
	 * @param the client Socket, and the entire client request This constructor will
	 * get the file that the user has requested If it is unable to create an output
	 * stream from that client, shuts client down If the client requests a file not
	 * in the resources folder shuts client down Creates the correct web page or a
	 * 404 web page. If the request is for a webSocket will do a handshake with the
	 * client and send to WebSocketHandler to handle any webSocket responses.
	 */
	public Response(Connection client, Request request, Pipe pipe) {
		this.pipe = pipe;
		this.client = client;
		// This map shows whether the request is a webSocket or not
		map = request.getMap();

		// if it is a webSocket do a handshake
		if (map.containsKey("Sec-WebSocket-Key")) {
			// create a handshake response
			handshakeResponse(client.getSocketChannel());
		}

		// if it is not a webSocket, it is a regular socket request. Handle as such!
		pathFile = request.getPathFile();
		// create an output stream and a print writer
		try {
			serverOutput = client.getSocketChannel().socket().getOutputStream();
			output = new PrintWriter(serverOutput);
			// Make sure nobody is trying to access files they shouldn't be. Close the
			// Client if so.
			if (!(pathFile.getCanonicalPath().contains("resources/"))) {
				System.out.println("Someone tried to get in from: " + client.getSocketChannel().getLocalAddress().toString());
				System.out.println("CanonicalPath");
				System.out.println(pathFile.getCanonicalPath().toString());
				System.out.println("Absolute Path");
				System.out.println(pathFile.getAbsolutePath().toString());
				try {
					client.getSocketChannel().close();
				} catch (IOException ex) {
					ex.printStackTrace();
					System.out.println("Closed Client. Client tried to access other folder.");
					return;
				}
			}
		} catch (IOException e) {
			if (e instanceof SocketException) {
				return;
			}
			e.printStackTrace();
		}

		// if the path does not exist, 404 error
		if (pathFile.exists()) {
			goodPath();
		}
		// if the path exists access the data
		else {
			f04Error();
		}

		// close all the scanners
		try {
			inputHtml.close();
			output.close();
			client.getSocketChannel().close();
		} catch (IOException e) {
			return;
		}
	}

	/*
	 * Creates a 404 Error web page Shows what file they were trying to access for
	 * reference
	 */
	private void f04Error() {
		System.out.println("404 Error");
		System.out.println(pathFile.getAbsolutePath().toString());

		pathFile = new File("resources/f04Error.html");

		// HEADER
		output.print("HTTP/1.1 404 NOT FOUND\r\n");
		output.print("Content-Length: " + pathFile.length() + "\r\n");
		output.print("\r\n");
		output.flush();

		try {
			// create a file input stream to read the data
			inputHtml = new FileInputStream(pathFile);
		} catch (IOException e) {
			return;
		}
		createWebpage();
	}

	/*
	 * If the client provides a good path, will create the correct web page
	 */
	private void goodPath() {
		// HEADER
		output.print("HTTP/1.1 200 OK\r\n");
		output.print("Content-Length: " + pathFile.length() + "\r\n");
		output.print("\r\n");
		output.flush();

		try {
			// create a file input stream to read the data
			inputHtml = new FileInputStream(pathFile);
		} catch (IOException e) {
			return;
		}

		createWebpage();

	}

	/*
	 * Creates any valid web page from resources folder.
	 */
	private void createWebpage() {
		// create an empty array to store the data
		byte[] htmlByte = new byte[1024];
		// create an int for the while loop to run
		int bufferSize = 0;

		// MUST DO THIS LOOP AT LEAST ONCE
		do {
			try {
				// set the int for the loop and copy 1024 bytes from the file
				bufferSize = inputHtml.read(htmlByte);
				if (bufferSize < 0)
					break;
				// output the copied material to the client
				serverOutput.write(htmlByte, 0, htmlByte.length);
			} catch (IOException e) {
				e.printStackTrace();
				bufferSize = 0;
			}

			// Do this loop while there are still bytes to copy over
		} while (bufferSize > 0);

		// flush this to update page
		output.flush();
	}

	/*
	 * Creates a valid handshake response for webSockets
	 * 
	 * @param Socket client This will decode the webSocket key and respond with a
	 * valid handshake. This will also print the header for the handshake and send
	 * any valid info to the WebSocketHandler class to handle webSocket events.
	 */
	private synchronized void handshakeResponse(SocketChannel clientSocket) {
		try {
			serverOutput = clientSocket.socket().getOutputStream();
			output = new PrintWriter(serverOutput);

			// sends to decodeHandShake to get valid handshake response
			String sha = decodeHandShake(
					(map.get("Sec-WebSocket-Key") + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes());

			// HEADER
			output.print("HTTP/1.1 101 Switching Protocols\r\n");
			output.print("Upgrade: websocket\r\n");
			output.print("Connection: Upgrade\r\n");
			output.print("Sec-WebSocket-Accept: " + sha + "\r\n");
			output.print("\r\n");
			output.flush();

			// Send the Socket client to the WebSocketHandler class
			new WebSocketHandler(client, pipe);

		} catch (IOException e) {
			System.out.println("BAD CLIENT STREAM");
			e.printStackTrace();
		}
	}

	/*
	 * Decodes handshake key
	 * 
	 * @param byte[] of the value of the handshake key + the magic string
	 * "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
	 * 
	 * @returns the decoded key string
	 */
	private String decodeHandShake(byte[] valueAsBytes) {
		MessageDigest md = null;
		try {
			// Tell the MessageDigest that this is a SHA-1
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		// returns the string decoded
		return Base64.getEncoder().encodeToString(md.digest(valueAsBytes));
	}

}
