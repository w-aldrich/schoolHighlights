package myServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.SocketChannel;

public class Connection implements Runnable{
	private SocketChannel clientSocket;
	private String name;
	private Server server;
	private Room room;
	private Pipe pipe;


	// Constructor for the connection class. Will set up client Socket, server and pipe
	public Connection(SocketChannel client, Server server) {
		this.clientSocket = client;
		this.server = server;
		try {
			pipe = Pipe.open();
		} catch (IOException e) {
			System.out.println("Pipe cannot open");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			//create input stream to listen to
			InputStream clientInput = clientSocket.socket().getInputStream();
			//get the client request
			Request request = new Request(clientInput);
			//create an appropriate response
			new Response(this, request, pipe);
		} catch (BadRequestException e) {
			System.out.println("Bad request, moving on to next client");
			e.printStackTrace();
			try {
				clientSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Closed Client. Bad Request");
			}
			return;
		} catch (IOException e) {
			try {
				clientSocket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				System.out.println("Closed Client. Bad Input Stream");
			}
		}
	}

	//Send message to Client 
	public synchronized void sendMessage(String message) {
		try {
			OutputStream pipeStream = Channels.newOutputStream(pipe.sink());
			ObjectOutputStream output = new ObjectOutputStream(pipeStream);
			output.writeObject(message);
			output.flush();
		} catch (IOException e1) {
			System.out.println("Connection Send Message shit went down");
			e1.printStackTrace();
		}
	}

	public SocketChannel getSocketChannel() {
		return clientSocket;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Server getServer() {
		return server;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}
}
