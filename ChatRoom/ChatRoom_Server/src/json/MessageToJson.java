package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessageToJson implements StringToJson {
	
	String type;
	String value;
	String name;
	
	
	public MessageToJson(String message, String name){
		type = "message";
		value = message;
		this.name = name + ":";
	}

	@Override
	public String toJson() {
		String message;
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		message = gson.toJson(this);
		return message;
	}

}
