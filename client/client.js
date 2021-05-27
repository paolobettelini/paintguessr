var server = new WebSocket('ws://127.0.0.1:3333');
  
server.onopen = function(e) {
	console.log("Connection established");
};

var ciao;

server.onmessage = function(e) {
	// Send drawing instructions
	e.data.arrayBuffer().then(buffer => drawLineBuf(buffer))
};


server.onclose = function(e) {
	console.log("Connection closed", e);
};

server.onerror = function(e) {
	console.log("WebSocket Error: ", e);
};

function sendToServer(data) {
	server.send(data);
}

function create() {
	server.send("create Paolo");
}

function join(token) {
	server.send("join " + token + " username");
}