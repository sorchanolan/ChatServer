package com.company.sorchanolan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chatroom {
  private String name;
  private String ipAddress;
  private int port;
  private int roomRef;
  private Map<Integer, String> messages;
  private List<Integer> clients;

  public Chatroom() {
    this.name = "";
    this.ipAddress = "";
    this.port = -1;
    this.roomRef = -1;
    this.messages = new HashMap<>();
    this.clients = new ArrayList<>();
  }

  //Getters

  public String getName() {
    return name;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public int getPort() {
    return port;
  }

  public int getRoomRef() {
    return roomRef;
  }

  public List<Integer> getClients() {
    return clients;
  }

  public Map<Integer, String> getMessages() {
    return messages;
  }

  //Setters

  public void setName(String name) {
    this.name = name;
  }

  public void setClients(List<Integer> clients) {
    this.clients = clients;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public void setMessages(Map<Integer, String> messages) {
    this.messages = messages;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setRoomRef(int roomRef) {
    this.roomRef = roomRef;
  }

  public void addMessage(int joinId, String message) {
    messages.put(joinId, message);
  }

  public void addClient(int joinId) {
    clients.add(joinId);
  }

  public boolean removeClient(int joinId) {
    return clients.remove(Integer.valueOf(joinId));
  }
}
