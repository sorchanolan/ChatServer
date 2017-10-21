package com.company.sorchanolan;

import org.json.*;

import java.io.*;
import java.net.*;
import java.util.Optional;

class ChatServer {
  private static int PORT = 6789;
  private static long idCounter = 1;

  public static void main(String argv[]) throws Exception {
    ServerSocket welcomeSocket = new ServerSocket(PORT);

    while (true) {
      System.out.println("Begin Client Comms");
      Socket conSocket = welcomeSocket.accept();

      BufferedReader inFromClient = new
          BufferedReader(new
          InputStreamReader(
          conSocket.getInputStream()));

      DataOutputStream outToClient = new
          DataOutputStream(conSocket.getOutputStream());

      String clientMessage = inFromClient.readLine();
      System.out.println(clientMessage);

      JoinRequest joinRequest = getJoinRequest(clientMessage);

      String joinResponse = createJoinResponse(joinRequest.getChatroomName()).toString();
      System.out.println(joinResponse);
      outToClient.writeBytes(joinResponse + "\n");
    }
  }

  private static synchronized String createID() {
    return String.valueOf(idCounter++);
  }

  private static JSONObject createJoinResponse(String chatroomName) {
    return new JSONObject()
        .put("JOINED_CHATROOM", chatroomName)
        .put("SERVER_IP", "localhost")
        .put("PORT", PORT)
        .put("ROOM_REF", createID())
        .put("JOIN_ID", createID());
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
