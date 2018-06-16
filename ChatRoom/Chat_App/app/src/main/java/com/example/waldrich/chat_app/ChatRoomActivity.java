package com.example.waldrich.chat_app;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatRoomActivity extends AppCompatActivity {
    String userName;
    LinearLayout layout;
    ScrollView scroll;
    WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Intent intent = getIntent();
        userName = intent.getStringExtra(MainActivity.USER_NAME);

        String[] name = userName.split(" ");
        userName = "";
        for(String a: name){
            userName+=a;
        }
        final String webUserName = userName;
        userName += ": ";

        String roomName = intent.getStringExtra(MainActivity.ROOM_NAME);
        String[] room = roomName.split(" ");
        roomName = "";
        for(String a: room){
            roomName+=a;
        }
        final String webRoomName = roomName;
        roomName = "Current Room: " + roomName;

        TextView roomDisplay = findViewById(R.id.roomDisplay);
        roomDisplay.setText(roomName);
        TextView userDisplay = findViewById(R.id.userDisplay);
        userDisplay.setText(userName);
        layout = findViewById(R.id.insideLayout);
        scroll = findViewById(R.id.scroll);

        AsyncHttpClient.getDefaultInstance().websocket("http://10.0.2.2:8080/", "my-protocol", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                ChatRoomActivity.this.webSocket = webSocket;
                webSocket.send("{ \"type\": \"join\" , \"value\": \"" + webRoomName + "\" , \"name\": \"" + webUserName + "\" }");
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    public void onStringAvailable(String s) {
                        try {
                            JSONObject reader = new JSONObject(s);
                            String type = reader.getString("type");
                            if(type.equals("message")){
                                String message = reader.getString("name");
                                message += " " + reader.getString("value");
                                postMessage(message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        if(!(s.startsWith("@!")))
//                            postMessage(s);
                    }
                });
            }
        });
    }

    public void submitMessage (View view) {
        EditText messageInput = findViewById(R.id.message);
        String message = messageInput.getText().toString();
        messageInput.setText("");
        message = "{\"type\":\"message\", \"value\":\"" + message + "\" }";
        webSocket.send(message);
    }

    private void postMessage(String s){
        Handler handler = new Handler(ChatRoomActivity.this.getMainLooper());
        final String incomingMessage = s;
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView inputMessage = new TextView(ChatRoomActivity.this);
                inputMessage.setText(incomingMessage);
                if(incomingMessage.startsWith(userName))
                {
                    inputMessage.setTextColor(0xff99cc00);
                }
                else{
                    inputMessage.setTextColor(0xffffffff);
                }
                layout.addView(inputMessage);
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

}
