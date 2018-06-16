package com.example.waldrich.chat_app;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.parser.JSONObjectParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    public static final String USER_NAME = "com.example.waldrich.chat_app.name";
    public static final String ROOM_NAME = "com.example.waldrich.chat_app.room";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AsyncHttpClient.getDefaultInstance().websocket("http://10.0.2.2:8080/chatLogin.html", "my-protocol", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                webSocket.send("{\"type\":\"roomRequest\" }");
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    public void onStringAvailable(String s) {
                        try {
                            JSONObject reader = new JSONObject(s);
                            String type = reader.getString("type");
                            if(type.equals("roomUpdate")){
                                TextView open = findViewById(R.id.addRoom);
                                String value = reader.getString("value");
                                value = open.getText() + value;
                                open.setText(value + "\n");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void connectToChatRoom(View view){
        Intent intent = new Intent(this, ChatRoomActivity.class);
        EditText name = findViewById(R.id.enterName);
        EditText room = findViewById(R.id.enterRoom);
        String userName = name.getText().toString();
        String roomName = room.getText().toString();
        intent.putExtra(USER_NAME, userName);
        intent.putExtra(ROOM_NAME, roomName);
        startActivity(intent);
    }
}
