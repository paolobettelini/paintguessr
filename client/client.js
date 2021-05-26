var server = new WebSocket('ws://127.0.0.1:3333');
  
server.onopen = function(e) {
	console.log("Connection established");
};

server.onmessage = function(e) {
	console.log(e);
	document.body.innerHTML += "<p>" + e.data + "</p>";
};

server.onclose = function(e) {
	console.log("Connection closed", e);
};

server.onerror = function(e) {
	console.log("WebSocket Error: ", e);
};

function create() {
	server.send("create Paolo");
}

function join(token) {
	server.send("join " + token + " username");
}