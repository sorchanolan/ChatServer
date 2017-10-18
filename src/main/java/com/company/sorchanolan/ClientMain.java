package com.company.sorchanolan;

import java.io.*;
import java.net.*;

class ClientMain {
  private static final String CLIENT_NAME = "Client 1";

  public static void main(String argv[]) throws Exception {
    Client client = new Client(CLIENT_NAME);

    System.out.println("Chatroom Name:");
    BufferedReader chatroomName = new BufferedReader(new InputStreamReader(System.in));
    Socket clientSocket = new Socket("localhost", 6789);
    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    String output = client.joinChatroomMessage(chatroomName.readLine()).toString();
    outToServer.writeBytes(output);
    String response = inFromServer.readLine();

    System.out.println("FROM SERVER:\n" + response);

    inFromServer.close();
    outToServer.close();
    chatroomName.close();
    clientSocket.close();
  }
}