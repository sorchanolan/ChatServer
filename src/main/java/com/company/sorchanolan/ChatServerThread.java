package com.company.sorchanolan;

import org.json.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatServerThread extends Thread implements Runnable {
  private volatile boolean running = true;
  private Socket socket = null;
  private ChatServer server = null;
  private int PORT = -1;
  private BufferedReader inFromClient = null;
  private DataOutputStream outToClient = null;
  private List<ClientJoinInstance> clientJoinInstances = new ArrayList<>();
  private String clientName = null;

  public ChatServerThread(ChatServer server, Socket socket) {
    this.server = server;
    this.socket = socket;
    PORT = socket.getPort();
  }

  public void run() {
    System.out.println("Server Thread " + PORT + " running.");
    openComms();

    while (running) {
      try {
        String clientMessage = inFromClient.readLine();
        System.out.println(clientMessage);
        processRequest(clientMessage);
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }

  private void openComms() {
    try {
      inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      outToClient = new DataOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  private void processRequest(String clientMessage) {
    if (clientMessage.equals("KILL_SERVICE\n")) {
      System.exit(0);
    } else if (clientMessage.equals("HELO text\n")) {
      sendHeloMessage();
      return;
    }

    JSONObject requestMessage = new JSONObject(clientMessage);
    if (requestMessage.has("JOIN_CHATROOM")) {
      processJoinRequest(requestMessage);
    } else if (requestMessage.has("LEAVE_CHATROOM")) {
      processLeaveRequest(requestMessage);
    } else if (requestMessage.has("CHAT")) {
      processChatMessage(requestMessage);
    } else if (requestMessage.has("DISCONNECT")) {
      processDisconnectRequest(requestMessage);
    } else sendErrorMessage(3, "No applicable message request type found. Please retry with an allowed request.");
  }

  public void close() throws IOException {
    if (socket != null)
      socket.close();
    if (inFromClient != null)
      inFromClient.close();

    running = false;
  }

  private void sendHeloMessage() {
    String ipAddress;
    try {
      ipAddress = InetAddress.getLocalHost().toString();
    } catch (UnknownHostException e) {
      System.out.println(e);
      return;
    }
    String response = "HELO text\nIP:" + ipAddress + "\nPort:" + PORT + "\nStudentID:13317836\n";
    System.out.println(response);
    try {
      outToClient.writeBytes(response + "\n");
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  private void processDisconnectRequest(JSONObject disconnectRequest) {
    String clientNameToDisconnect = "";
    try {
      clientNameToDisconnect = disconnectRequest.getString("CLIENT_NAME");
    } catch (JSONException e) {
      System.out.println("Unable to process disconnect request, invalid message: " + e);
      sendErrorMessage(6, "Invalid disconnect request message. Please retry.");
      return;
    }

    if (!clientNameToDisconnect.equals(clientName)) {
      sendErrorMessage(6, "Incorrect client name to disconnect. Please retry.");
      return;
    }

    try {
      close();
    } catch (IOException e) {
      System.out.println("Could not properly close connection: " + e);
    }

  }

  private void processLeaveRequest(JSONObject leaveRequestMessage) {
    String response;
    int roomRef, joinId;
    try {
      roomRef = leaveRequestMessage.getInt("LEAVE_CHATROOM");
      joinId = leaveRequestMessage.getInt("JOIN_ID");
    } catch (JSONException e) {
      System.out.println("Unable to process leave request, invalid message: " + e);
      sendErrorMessage(1, "Invalid leave request message. Please retry.");
      return;
    }

    Optional<ClientJoinInstance> maybeClientJoinInstance = clientJoinInstances.stream()
        .filter(clientJoin -> clientJoin.getJoinId() == joinId)
        .findFirst();

    Optional<Chatroom> maybeChatroom;
    if (maybeClientJoinInstance.isPresent()) {
      synchronized(server.chatrooms) {
       maybeChatroom = server.chatrooms.stream()
          .filter(chatroom -> chatroom.getRoomRef() == roomRef)
          .findFirst();
      }

      if (maybeChatroom.isPresent()) {
        maybeChatroom.get().removeClientSocket(socket);
        response = new JSONObject()
            .put("LEFT_CHATROOM", roomRef)
            .put("JOIN_ID", joinId)
            .toString();
      } else {
        System.out.println("No chatroom with this reference found.");
        sendErrorMessage(5, "No chatroom with this reference found.");
        return;
      }
    } else {
      System.out.println("Unable to process leave request, client is not currently in chatroom " + roomRef);
      sendErrorMessage(4, "You are not currently in chatroom " + roomRef + " and therefore cannot leave it.");
      return;
    }

    System.out.println(response);
    try {
      outToClient.writeBytes(response + "\n");
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  private void processJoinRequest(JSONObject joinRequestMessage) {
    String chatroomName, response;
    try {
      clientName = joinRequestMessage.getString("CLIENT_NAME");
      chatroomName = joinRequestMessage.getString("JOIN_CHATROOM");
    } catch (JSONException e) {
      System.out.println("Unable to process join request, invalid message: " + e);
      sendErrorMessage(1, "Invalid join request message. Please retry.");
      return;
    }

    synchronized (server.clientNames) {
      if (!server.clientNames.contains(clientName)) {
        server.clientNames.add(clientName);
      } else {
        System.out.println("Client " + clientName + " already in chat system");
      }
    }

    int joinId = server.createID();
    int roomRef;

    Chatroom chatroom = new Chatroom();
    Optional<Chatroom> maybeChatroom = server.chatrooms.stream()
        .filter(_chatroom -> _chatroom.getName().equals(chatroomName))
        .findFirst();

    if (!maybeChatroom.isPresent()) {
      roomRef = server.createID();
      chatroom.setName(chatroomName);
      chatroom.setRoomRef(roomRef);
      chatroom.setPort(PORT);
      chatroom.setIpAddress("localhost");
      chatroom.addClientSocket(socket);
      server.chatrooms.add(chatroom);
      System.out.println("Chatroom " + chatroom.getName() + " added");
    } else {
      maybeChatroom.get().addClientSocket(socket);
      roomRef = maybeChatroom.get().getRoomRef();
    }

    clientJoinInstances.add(new ClientJoinInstance(clientName, joinId, roomRef));
    System.out.println("Client " + clientName + " added to chatroom " + chatroomName);

    response = new JSONObject()
        .put("JOINED_CHATROOM", chatroomName)
        .put("SERVER_IP", "localhost")
        .put("PORT", PORT)
        .put("ROOM_REF", roomRef)
        .put("JOIN_ID", joinId)
        .toString();

    System.out.println(response);
    try {
      outToClient.writeBytes(response + "\n");
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  private void processChatMessage(JSONObject chatMessage) {
    int roomRef, joinId;
    String message;
    try {
      clientName = chatMessage.getString("CLIENT_NAME");
      roomRef = chatMessage.getInt("CHAT");
      joinId = chatMessage.getInt("JOIN_ID");
      message = chatMessage.getString("MESSAGE");
    } catch (JSONException e) {
      System.out.println("Unable to process message request, invalid message: " + e);
      sendErrorMessage(5, "Invalid chat message request. Please retry.");
      return;
    }

    JSONObject chatMessageResponse = new JSONObject()
        .put("CHAT", roomRef)
        .put("CLIENT_NAME", clientName)
        .put("MESSAGE", message);

    Optional<Chatroom> maybeChatroom;
    synchronized (server.chatrooms) {
       maybeChatroom = server.chatrooms.stream()
          .filter(chatroom -> chatroom.getRoomRef() == roomRef)
          .findFirst();
    }

    if (maybeChatroom.isPresent()) {
      Chatroom chatroom = maybeChatroom.get();

      for (Socket clientSocketInChatroom : chatroom.getClientSockets()) {
        try {
          outToClient = new DataOutputStream(clientSocketInChatroom.getOutputStream());
          outToClient.writeBytes(chatMessageResponse.toString() + "\n\n");
        } catch (IOException e) {
          System.out.println("Could not connect to socket: " + e);
        }
      }
    }
  }

  private void sendErrorMessage(int errorCode, String errorMessage) {
    String errorResponse = new JSONObject()
        .put("ERROR_CODE", errorCode)
        .put("ERROR_MESSAGE", errorMessage)
        .toString();
    System.out.println(errorResponse);

    try {
      outToClient.writeBytes(errorResponse + "\n");
    } catch (IOException ie) {
      System.out.println(ie);
    }
  }
}
