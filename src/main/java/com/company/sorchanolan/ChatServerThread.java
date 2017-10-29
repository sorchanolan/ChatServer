package com.company.sorchanolan;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

public class ChatServerThread extends Thread {
  private Socket socket = null;
  private ChatServer server = null;
  private int PORT = -1;
  private BufferedReader inFromClient = null;
  private DataOutputStream outToClient = null;
  private int idCounter = 1;

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
    MessageType messageType = checkMessageType(clientMessage);

    JoinRequest joinRequest = getJoinRequest(clientMessage);
    if (messageType.equals(MessageType.LEAVE)) {
      ClientJoinInstance clientJoinInstance = getClientJoinInstance(clientMessage);
      Optional<Chatroom> maybeChatroom = server.chatrooms.stream()
          .filter(chatroom -> chatroom.getRoomRef() == clientJoinInstance.getRoomRef())
          .findFirst();

      if (maybeChatroom.isPresent()) {
        maybeChatroom.get().removeClient(clientJoinInstance.getJoinId());
      } else {
        System.out.println("No room reference found.");
      }
    }

    Client client = new Client(joinRequest.getClientName());
    if (!server.clients.contains(client)) {
      server.clients.add(client);
      server.clientNameToJoinId.put(client.getName(), createID());
    } else {
      System.out.println("Client " + client.getName() + " already in chat system");
    }

    Optional<Chatroom> maybeChatroom = server.chatrooms.stream().filter(chatroom -> chatroom.getName().equals(joinRequest.getChatroomName())).findFirst();
    if (!maybeChatroom.isPresent()) {
      Chatroom chatroom = new Chatroom();
      chatroom.setName(joinRequest.getChatroomName());
      chatroom.setRoomRef(createID());
      chatroom.setPort(PORT + 1);
      chatroom.setIpAddress("");
      chatroom.addClient(server.clientNameToJoinId.get(client.getName()));
      server.chatrooms.add(chatroom);
      System.out.println("Chatroom " + chatroom.getName() + " added");
    } else {
      maybeChatroom.get().addClient(server.clientNameToJoinId.get(client.getName()));
      System.out.println("Client " + client.getName() + " added to chatroom " + maybeChatroom.get().getName());
    }

    return createJoinResponse(joinRequest.getChatroomName(), server.clientNameToJoinId.get(client.getName()).toString()).toString();
  }

  public void close() throws IOException {
    if (socket != null)
      socket.close();
    if (inFromClient != null)
      inFromClient.close();
  }

  private synchronized int createID() {
    return idCounter++;
  }

  private JSONObject createJoinResponse(String chatroomName, String joinId) {
    return new JSONObject()
        .put("JOINED_CHATROOM", chatroomName)
        .put("SERVER_IP", "localhost")
        .put("PORT", PORT)
        .put("ROOM_REF", createID())
        .put("JOIN_ID", joinId);
  }

  private JoinRequest getJoinRequest(String input) {
    JoinRequest joinRequest = new JoinRequest();

    try {
      JSONObject json = new JSONObject(input);
      joinRequest.setChatroomName(json.getString("JOIN_CHATROOM"));
      joinRequest.setClientName(json.getString("CLIENT_NAME"));
    } catch (Exception e) {
      System.out.println("Could not convert to JSON: " + e.getMessage());
    }

    return joinRequest;
  }

  private ClientJoinInstance getClientJoinInstance(String input) {
    ClientJoinInstance clientJoinInstance = new ClientJoinInstance();

    try {
      JSONObject json = new JSONObject(input);
      Client client = new Client(json.getString("CLIENT_NAME"));
      clientJoinInstance.setClient(client);
      clientJoinInstance.setJoinId(json.getInt("JOIN_ID"));
      clientJoinInstance.setRoomRef(json.getInt("ROOM_REF"));
    } catch (Exception e) {
      System.out.println("Could not convert to JSON: " + e.getMessage());
    }

    return clientJoinInstance;
  }

  private MessageType checkMessageType(String input) {
    JSONObject jsonObject = new JSONObject(input);
    if (jsonObject.has("JOIN_CHATROOM")) {
      return MessageType.JOIN;
    } else if (jsonObject.has("LEAVE_CHATROOM")) {
      return MessageType.LEAVE;
    } else if (jsonObject.has("CHAT")) {
      return MessageType.MESSAGE;
    } else if (jsonObject.has("DISCONNECT")) {
      return MessageType.DISCONNECT;
    } else return MessageType.ERROR;
  }
}
