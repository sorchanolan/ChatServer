package com.company.sorchanolan;

import org.json.JSONObject;

public class Client {

  private String clientName;

  public Client(String name) {
    this.clientName = name;
  }

  public JSONObject joinChatroomMessage(String chatroomName) {
    return new JSONObject()
        .put("CHATROOM_NAME", chatroomName)
        .put("CLIENT_IP", 0)
        .put("PORT", 0)
        .put("CLIENT_NAME", clientName);
  }
}
