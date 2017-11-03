package com.company.sorchanolan;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatServerThread extends Thread implements Runnable {
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
    System.out.println("Begin Comms");
    openComms();
    String response = "";

    while (true) {
      try {
        String clientMessage = inFromClient.readLine();
        System.out.println(clientMessage);
        response = processRequest(clientMessage);
      } catch (IOException e) {
        System.out.println(e);
      }

      System.out.println(response);
      try {
        outToClient.writeBytes(response + "\n");
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

  private String processRequest(String clientMessage) {
    return getResponse(clientMessage);
  }

  private String getResponse(String clientMessage) {
    JSONObject requestMessage = new JSONObject(clientMessage);
    if (requestMessage.has("JOIN_CHATROOM")) {
      return processJoinRequest(requestMessage);
    } else if (requestMessage.has("LEAVE_CHATROOM")) {
      return  processLeaveRequest(requestMessage);
    } else if (requestMessage.has("CHAT")) {
      return  processChatMessage(requestMessage);
    } else if (requestMessage.has("DISCONNECT")) {
      return "";
    } else return getErrorMessage(3, "No applicable message request type found. Please retry with an allowed request.");
  }

  public void close() throws IOException {
    if (socket != null)
      socket.close();
    if (inFromClient != null)
      inFromClient.close();
  }

  private String processLeaveRequest(JSONObject leaveRequestMessage) {
    int roomRef, joinId;
    try {
      roomRef = leaveRequestMessage.getInt("LEAVE_CHATROOM");
      joinId = leaveRequestMessage.getInt("JOIN_ID");
    } catch (JSONException e) {
      System.out.println("Unable to process leave request, invalid message: " + e);
      return getErrorMessage(1, "Invalid leave request message. Please retry.");
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
        return new JSONObject()
            .put("LEFT_CHATROOM", roomRef)
            .put("JOIN_ID", joinId)
            .toString();
      } else {
        System.out.println("No chatroom with this reference found.");
        return getErrorMessage(5, "No chatroom with this reference found.");
      }
    } else {
      System.out.println("Unable to process leave request, client is not currently in chatroom " + roomRef);
      return getErrorMessage(4, "You are not currently in chatroom " + roomRef + " and therefore cannot leave it.");
    }
  }

  private String processJoinRequest(JSONObject joinRequestMessage) {
    String chatroomName;
    try {
      clientName = joinRequestMessage.getString("CLIENT_NAME");
      chatroomName = joinRequestMessage.getString("JOIN_CHATROOM");
    } catch (JSONException e) {
      System.out.println("Unable to process join request, invalid message: " + e);
      return getErrorMessage(1, "Invalid join request message. Please retry.");
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

    return new JSONObject()
        .put("JOINED_CHATROOM", chatroomName)
        .put("SERVER_IP", "localhost")
        .put("PORT", PORT)
        .put("ROOM_REF", roomRef)
        .put("JOIN_ID", joinId)
        .toString();
  }

  private String processChatMessage(JSONObject chatMessage) {
    int roomRef, joinId;
    String message;
    try {
      clientName = chatMessage.getString("CLIENT_NAME");
      roomRef = chatMessage.getInt("CHAT");
      joinId = chatMessage.getInt("JOIN_ID");
      message = chatMessage.getString("MESSAGE");
    } catch (JSONException e) {
      System.out.println("Unable to process message request, invalid message: " + e);
      return getErrorMessage(5, "Invalid chat message request. Please retry.");
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
          outToClient.writeBytes(chatMessageResponse.toString());
        } catch (IOException e) {
          System.out.println("Could not connect to socket: " + e);
        }
      }
    }

    return chatMessageResponse.toString();
  }

  private String getErrorMessage(int errorCode, String errorMessage) {
    return new JSONObject()
        .put("ERROR_CODE", errorCode)
        .put("ERROR_MESSAGE", errorMessage)
        .toString();
  }
}
