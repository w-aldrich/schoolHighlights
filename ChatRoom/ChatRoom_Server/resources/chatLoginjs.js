"using strict";

let body = document.getElementsByTagName('body')[0];

//input field for the name
let name = document.getElementById('name');
//input field for the room name
let room = document.getElementById('room');
//submit button for the user name and room name
let button = document.getElementById('submit');


let url = "ws://" + location.host;
let mySocket = new WebSocket(url);
mySocket.onopen = onOpen;

function onOpen()
{
  let roomrequest = {type: "roomRequest"}
  mySocket.send(JSON.stringify(roomrequest));
  //mySocket.send('@!All Room Request');
  button.addEventListener('click', sendrequest);
}

function sendrequest()
{
  let xhr = new XMLHttpRequest();
  xhr.open('GET', 'chatRoom.html');
  xhr.addEventListener('load', callback);
  xhr.addEventListener('error', errorcallback);
  xhr.send();
  let splitter = room.value.split(" ");
  room.value = '';
  for(let i = 0; i < splitter.length; i++){
    room.value += splitter[i];
  }

  splitter = name.value.split(" ");
  name.value = '';
  for(let i = 0; i < splitter.length; i++){
    name.value += splitter[i];
  }
  let joinRequest = {type: "join", value: room.value, name: name.value};

  //mySocket.send("Join: " + room.value + " " + name.value);
  mySocket.send(JSON.stringify(joinRequest));
}

function callback()
{
  body.innerHTML = this.responseText;
  newhtml();
}

function errorcallback()
{
  console.log("error in call back" + this);
}

function newhtml()
{
  let roomTitle = document.getElementById('roomTitle');
  let txt = room.value;
  roomTitle.innerHTML += txt;

  let namedisplay = document.getElementById('nameTitle');
  let nametxt = name.value;
  namedisplay.innerHTML += nametxt;

  var chatBox = document.getElementById('chatBox');
  var input = document.getElementById('input');
  let inputbutton = document.getElementById('inputSubmit');
  inputbutton.addEventListener('click', sendWS);

  let updateUsers = document.getElementById('updateUsers');
  updateUsers.addEventListener('click', updateUserRequest);
  let refreshChat = document.getElementById('refreshChat');
  refreshChat.addEventListener('click', refreshChatRequest);

}

function updateUserRequest(){
  let updateRequest = {type: "userUpdate"};
  mySocket.send(JSON.stringify(updateRequest));
}

function refreshChatRequest(){
  let refreshChat = {type: "refreshChat"};
  mySocket.send(JSON.stringify(refreshChat));
  chatBox.removeChild(document.getElementById('refreshChat'));
}

function sendWS()
{
  let inputtxt = {type: "message", value: input.value};
  mySocket.send(JSON.stringify(inputtxt));
}

mySocket.onmessage = function(event){
  let para = JSON.parse(event.data);
  let type = para.type;
  console.log(type);

  if (type == 'message'){
      let paratxt = para.value;
      paratxt = para.name + " " + para.value;
      let newParagraph = document.createElement('p');
      let p = document.createTextNode(paratxt);
      newParagraph.appendChild(p);
      let b = document.createElement('br');

      chatBox.appendChild(p);
      chatBox.appendChild(b);

      input.value = "";
      chatBox.scrollTop = chatBox.scrollHeight;
  } else if(type == 'updateUsers'){
      let paratxt = 'Current Users In Room: ';
      paratxt += para.value;
      let users = document.getElementById('users');
      if(users != null){
        users.innerText = "";
        let usertxt = document.createTextNode(paratxt);
        users.appendChild(usertxt);
      }
  } else if (type == 'roomUpdate'){
      paratxt = para.value;
      let addOptiontxt = document.createTextNode(paratxt + ": ");
      let allRooms = document.getElementById('allRooms');
      allRooms.appendChild(addOptiontxt);
  } else if (type == 'refreshChat') {

  }
}


mySocket.onclose = function(event){
  console.log("Closed webSocket");
}
