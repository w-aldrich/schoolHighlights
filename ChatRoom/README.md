# ChatRoom

Created a multithreaded web server that implements HTTP and Web socket protocols on top of TCP sockets. Utilizes JSON formatting for communication between server and both web client and Android client.

## Chat_App

This is a Android app that will connect to the server and run a ChatRoom. Created utilizing AndroidStudio and *Java* as its language. Utilizes JSON formatting to communicate with the server. See below for a visual demo of the Chat_App client.

## Visual Demo of Chat_App Client

The login screen for the Chat_App is very similar to the login screen for the web client.

<img src="https://github.com/w-aldrich/schoolHighlights/tree/master/ChatRoom/DemoPictures/AndroidLogin.png"/>

Once in the correct room, will change the text color according to whether you sent the message, or somebody else did.

<img src="https://github.com/w-aldrich/schoolHighlights/tree/master/ChatRoom/DemoPictures/AndroidText.png"/>

The Chat_App can communicate with the web client as seen above. The following image is the same conversation from the web clients point of view.

<img src="https://github.com/w-aldrich/schoolHighlights/tree/master/ChatRoom/DemoPictures/WebToAndroid.png"/>

## ChatRoom_Server

***Server***

This is a multithreaded server utilizing a Fixed Thread Pool (size 100) to serve its clients. The server was created in *Java* as its language, utilizing the Eclipse IDE. Uses pipes to communicate to all clients through JSON formatting. Uses the gson library for its JSON formatting. The easiest way to run this server is through the Eclipse IDE. The server will shut down any bad requests from clients, and remove them from the server allowing for someone else to join the ChatRoom. There can be multiple chatrooms going at a time. When a client connects either through the Chat_App or through the web interface, all of the open rooms will be shown.

***Web Client***
Created using Atom IDE for the *Http, CSS, and JavaScript*. Uses JSON formatting to communicate with the server. Once the Server is running, to connect enter `localhost:8080` into your browser. The login screen should appear. See below for a visual demo of logging into the Web Client for this ChatRoom.


## Visual Demo of Web Client

The first user who is trying to enter a room will see the following screen.

<img src="https://github.com/w-aldrich/schoolHighlights/tree/master/ChatRoom/DemoPictures/FirstUserLogin.png"/>

Once the user is logged into a room, they will see a blank room.

<img src="https://github.com/w-aldrich/schoolHighlights/tree/master/ChatRoom/DemoPictures/FirstUserBlankRoom.png"/>

Once a second (or anyone else from this point forward) connects to the server, they will see what rooms are available to join. They can join any of the rooms listed, or create a new room.

<img src="https://github.com/w-aldrich/schoolHighlights/tree/master/ChatRoom/DemoPictures/SecondUserLogin.png"/>

Once they are in the room, they can refresh their chat and see all of the previous messages in that room. They will be able to see all of the current users in the room as well.

<img src="https://github.com/w-aldrich/schoolHighlights/tree/master/ChatRoom/DemoPictures/ChatRoomExample.png"/>
