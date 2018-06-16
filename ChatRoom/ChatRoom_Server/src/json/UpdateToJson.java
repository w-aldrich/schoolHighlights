package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UpdateToJson implements StringToJson {
	
	String type;
	String value;
	
	public UpdateToJson(String type, String message){
		this.type = type;
		value = message;
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
