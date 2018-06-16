package myServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import json.UpdateToJson;

public class Server {
	public ServerSocketChannel server;
	public Selector socketSelector;
	HashMap<String, Room> listOfRooms;

	/*
	 * This constructor creates a new server This server runs on port 8080
	 */
	public Server() {
		server = null;
		listOfRooms = new HashMap<>();

		try {
			// create the ServerSocketChannel to listen for the client on port 8080
			server = ServerSocketChannel.open().bind(new InetSocketAddress(8080));
			// this needs to be false to listen for events.
			server.configureBlocking(false);
			// create the Selector for the ServerSocketChannel
			socketSelector = Selector.open();
			server.register(socketSelector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.out.println("Resource already open, shut down current resource.");
			System.exit(1);
		}

		// create a pool for the threads
		final ExecutorService pool = Executors.newFixedThreadPool(100);

		// serve those in the pool
		while (true) {
			try {
				// waits for an event (We are waiting for accept)
				socketSelector.select();
				// creates a "list" (unordered) of all the events that happened
				Set<SelectionKey> keys = socketSelector.selectedKeys();
				// Allows us to go through all of the keys
				Iterator<SelectionKey> it = keys.iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					// if the event was an accept event remove the key and serve the event
					if (key.isAcceptable()) {
						it.remove();
						serve(pool, this);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/*
	 * This method handles the client requests, and the server output Will create a
	 * client socket, if the server cannot accept client, shuts client down Creates
	 * input stream from client, will shut client down if unable to create input
	 * stream
	 */
	public void serve(ExecutorService pool, Server serverServer) {
		final SocketChannel client;
		try {

			// the server socket accepts the client
			client = server.accept();

			// create a thread for the user, add it to the pool
			Connection connect = new Connection(client, serverServer);
			pool.execute(connect);

		} catch (IOException e) {
			System.out.println("Client accept failed. IO Exception.");
			e.printStackTrace();
		}
	}

	public Room getRoom(String roomName, Connection client) {
		if (listOfRooms.containsKey(roomName)) {
			listOfRooms.get(roomName).addUser(client);
			return listOfRooms.get(roomName);
		}
		Room room = new Room(roomName, client);
		listOfRooms.put(roomName, room);
		return room;
	}

	public void getRoomKeyRequest(Connection client) {
		Set<String> roomKeys = listOfRooms.keySet();
		for (String s : roomKeys) {
			UpdateToJson update = new UpdateToJson("roomUpdate", s);
			s = update.toJson();
			client.sendMessage(s);
		}
	}

	public void removeRoom(String roomName) {
		listOfRooms.remove(roomName);
	}

}
