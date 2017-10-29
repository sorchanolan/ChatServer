package com.company.sorchanolan;

import java.io.*;
import java.net.*;
import java.util.*;

class ChatServer implements Runnable {
  private static int PORT = 6789;
  private static String IP_ADDRESS = "";
  private Thread thread = null;
  private ServerSocket welcomeSocket = null;
  private ChatServerThread client = null;
  public List<String> clientNames = new ArrayList<>();
  public List<Chatroom> chatrooms = new ArrayList<>();
  public Map<String, Integer> clientNameToJoinId = new HashMap<>();

  public ChatServer(int port) {
    try {
      welcomeSocket = new ServerSocket(port);
    } catch (IOException e) {
      System.out.println(e);
    }

    if (thread == null)
    {
      thread = new Thread(this);
      thread.start();
    }
  }

  public static void main(String argv[]) throws Exception {
    new ChatServer(PORT);
  }

  @Override
  public void run() {
    while (thread != null) {
      try {
        Socket conSocket = welcomeSocket.accept();
        client = new ChatServerThread(this, conSocket);
        client.start();
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}
