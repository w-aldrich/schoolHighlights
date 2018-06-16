package myServer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import json.MessageToJson;

public class Message{

	private String type;
	private String value;
	private String name;
	private static Connection client;

	// Message constructor. Sets The type of message, the value and the name of the Client.
	Message(String s, Connection client){
		Message.client = client;
		//Create a parser to get the elements from the JSON string
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(s);
		//create a JSON object from the information that was just parsed
		JsonObject message = element.getAsJsonObject();
		//all JSON messages from HTML or Android have a type field
		type = message.get("type").getAsString();
		//If they have a value field get that information
		if(message.has("value")) {
			value = message.get("value").getAsString();
			if(value.length() > 1000) {
				value = value.substring(0, 1000);
			}
		}
		//If they have a name field get that information
		if(message.has("name"))
			name = message.get("name").getAsString();
		jsonInformation();
	}

	private void jsonInformation() {
		//If it is a join request set the room and the client name
		if(type.equals("join")) {
			client.setName(name);
			Room room = client.getServer().getRoom(value, client);
			client.setRoom(room);
		}
		//if it is a room request, send back all appropriate rooms
		else if (type.equals("roomRequest")) {
			client.getServer().getRoomKeyRequest(client);
		}
		//if it is a user update request, send back all current users
		else if (type.equals("userUpdate")) {
			client.getRoom().updateUsers();
		}
		//if it is a refresh chat request, send back all of the messages from that room
		else if(type.equals("refreshChat")) {
			client.getRoom().refreshChat(client);
		}
		//if it is a message, format it propperly for JSON and send it to everyone
		else if(type.equals("message")) {
			MessageToJson mtj = new MessageToJson(value, client.getName());
			String message = mtj.toJson();
			client.getRoom().postMessage(message);
		}
	}

}
