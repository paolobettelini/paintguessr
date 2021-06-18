# paintguessr
Multiplayer JavaScript game.  

The client is written in HTML/CSS/JavaScript.  
The server is written in Java with Maven.

The server uses the [TooTallNate/Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) library.

# Setup
1) Clone the repository
  ```
  git clone https://github.com/PaoloBettelini/paintguessr/
  ```
2) Compile with Maven
  ```
  mvn package
  ```
3) Start the server  
  Start the jar file in the `server/target/` path.  
  Parameters: ip, port
  ```
  java -jar -Xmx1024m server.jar 0.0.0.0 3333
  ```
4) Setup the client  
  Modify the first line of the `client/client.js`
  ```js
  const server = new WebSocket('ws://192.168.1.2:3333');
  ```
