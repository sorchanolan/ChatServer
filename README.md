# ChatServer
Sorcha Nolan  
13317836   
  
Maven is needed to run scripts. The code runs as per the brief locally in the IDE, but problems occurred when running the scripts due to a classpath error.   
   
ChatServer.java is the entry-point to the program. It opens communications on the specified port, and spins up a new ChatServerThread for each client that tries to connect to the port. The ChatServerThread reads the input message from the client as a JSON object, identifies what type of message it is and processes this message accordingly.  

**KILL SERVICE:**
If this message is received, the system shuts down.   

**HELO text:** 
This message sends information about the IP address, port number and my student number to the client.  

**JOIN CHATROOM:**
The join request method first adds the client to the synchronised list of clients currently in the system, if they are not already in it. It then references the synchronised list of currently open chatrooms. If the requested chatroom does not exist, it will be created and the client will be added into it. If it does exist, the client is added. The response is then sent to the client, and a ClientJoinInstance is stored for the client which maps the JoinId to the RoomRef. An error message is sent back to the client initially in the case of a malformed request message.  

**CHAT MESSAGE:**
This method checks for a correctly formed request message, sending an error message back if not. It then forms the chat reponse to send to all members of the chatroom, and finds the specified chatroom in the sychronised list of currently open rooms. The socket connections of each member of the chatroom is stored with each room, and so the message is sent to every socket connection in that list.   

**LEAVE CHATROOM:**
The leave chatroom method first finds the ClientJoinInstance related to the particular leave request. If this is present, the chatroom object is found and the client's socket connection is removed from the list of clients within it. If this is successful, the leave response is sent to the client. An error response is sent in the case of a malformed request, if the requested chatroom does not exist, or if the client wanting to leave the chatroom is not currently in that chatroom.  

**DISCONNECT:**
A successful disconnect request closes the socket connection for the particular client, and stops the thread running. Error messages are sent in the case of a malformed request, or if the client name wishing to disconnect does not match the name of the client being processed in the particular thread.
