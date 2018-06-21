package myServer;

import java.util.ArrayList;

import json.UpdateToJson;

public class Room {

	public String nameOfRoom;
	private ArrayList<Connection> listOfClients;
	private ArrayList<String> messageList;

	Room(String nameOfRoom, Connection client) {
		this.nameOfRoom = nameOfRoom;
		listOfClients = new ArrayList<>();
		messageList = new ArrayList<>();
		listOfClients.add(client);
		updateUsers();
	}

	public synchronized void postMessage(String message) {
		messageList.add(message);
		for (Connection client : listOfClients) {
			client.sendMessage(message);
		}
	}

	public void addUser(Connection client) {
		listOfClients.add(client);
		updateUsers();
	}

	public void removeUser(Connection client) {
		listOfClients.remove(client);
		updateUsers();
	}

	public void updateUsers() {
		String type = "updateUsers";
		String message = "";
		for (Connection client : listOfClients) {
			message += client.getName() + ": ";
		}
		UpdateToJson utj = new UpdateToJson(type, message);
		message = utj.toJson();
		postMessage(message);
	}

	public void refreshChat(Connection client) {
		for(String s: messageList) {
			client.sendMessage(s);
		}
	}


}
