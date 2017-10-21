package com.company.sorchanolan;

import org.json.*;

import java.io.*;
import java.net.*;

class ChatServer {
  private static int PORT = 6789;
  private static long idCounter = 0;

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

            String jsonMessage = inFromClient.readLine();
            System.out.println(jsonMessage);
            try {
              JSONObject json = new JSONObject(inFromClient.readLine());
              String chatroomName = json.getString("CHATROOM_NAME");
              String clientName = json.getString("CLIENT_NAME");
            } catch (Exception e) {
              System.out.println("Could not convert to json, " + e.getMessage());
            }

            outToClient.writeBytes(joinRequestMessage("").toString());
        }
    }

  private static synchronized String createID() {
    return String.valueOf(idCounter++);
  }

  private static JSONObject joinRequestMessage(String chatroomName) {
      return new JSONObject()
          .put("JOINED_CHATROOM", chatroomName)
          .put("SERVER_IP", "localhost")
          .put("PORT", PORT)
          .put("ROOM_REF", createID())
          .put("JOIN_ID", createID());
    }
}
