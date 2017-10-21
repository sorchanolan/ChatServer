package com.company.sorchanolan;

import org.json.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

class ChatServer {
  private static int PORT = 6789;
  private static int idCounter = 1;

  public static void main(String argv[]) throws Exception {
    ServerSocket welcomeSocket = new ServerSocket(PORT);
    List<Client> clients = new ArrayList<>();
    List<Chatroom> chatrooms = new ArrayList<>();
    Map<String, Integer> clientNameToJoinId = new HashMap<>();

    while (true) {
      System.out.println("Begin Comms");
      Socket conSocket = welcomeSocket.accept();

      BufferedReader inFromClient = new BufferedReader(new InputStreamReader(conSocket.getInputStream()));
      DataOutputStream outToClient = new DataOutputStream(conSocket.getOutputStream());

      String clientMessage = inFromClient.readLine();
      System.out.println(clientMessage);

      JoinRequest joinRequest = getJoinRequest(clientMessage);

      Client client = new Client(joinRequest.getClientName());
      if (!clients.contains(client)) {
        clients.add(client);
        clientNameToJoinId.put(client.getName(), createID());
      } else {
        System.out.println("Client " + client.getName() + " already in chat system");
      }

      Optional<Chatroom> maybeChatroom = chatrooms.stream().filter(chatroom -> chatroom.getName().equals(joinRequest.getChatroomName())).findFirst();
      if (!maybeChatroom.isPresent()) {
        Chatroom chatroom = new Chatroom();
        chatroom.setName(joinRequest.getChatroomName());
        chatroom.setRoomRef(createID());
        chatroom.setPort(PORT + 1);
        chatroom.setIpAddress("");
        chatroom.addClient(clientNameToJoinId.get(client.getName()));
        chatrooms.add(chatroom);
        System.out.println("Chatroom " + chatroom.getName() + "added");
      } else {
        maybeChatroom.get().addClient(clientNameToJoinId.get(client.getName()));
        System.out.println("Client " + client.getName() + " added to chatroom " + maybeChatroom.get().getName());
      }

      String joinResponse = createJoinResponse(joinRequest.getChatroomName(), clientNameToJoinId.get(client.getName()).toString()).toString();
      System.out.println(joinResponse);
      outToClient.writeBytes(joinResponse + "\n");
    }
  }

  private static synchronized int createID() {
    return idCounter++;
  }

  private static JSONObject createJoinResponse(String chatroomName, String joinId) {
    return new JSONObject()
        .put("JOINED_CHATROOM", chatroomName)
        .put("SERVER_IP", "localhost")
        .put("PORT", PORT)
        .put("ROOM_REF", createID())
        .put("JOIN_ID", joinId);
    }

    private static JoinRequest getJoinRequest(String input) {
    JoinRequest joinRequest = new JoinRequest();

      try {
        JSONObject json = new JSONObject(input);
        joinRequest.setChatroomName(json.getString("CHATROOM_NAME"));
        joinRequest.setClientName(json.getString("CLIENT_NAME"));
      } catch (Exception e) {
        System.out.println("Could not convert to JSON: " + e.getMessage());
      }

      return joinRequest;
    }
}
