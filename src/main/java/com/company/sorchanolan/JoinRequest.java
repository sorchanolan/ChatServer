package com.company.sorchanolan;

public class JoinRequest {
  private String chatroomName;
  private String clientName;

  public JoinRequest() {
    chatroomName = "";
    clientName = "";
  }

  public JoinRequest(String chatroomName, String clientName) {
    this.chatroomName = chatroomName;
    this.clientName = clientName;
  }

  public String getChatroomName() {
    return chatroomName;
  }

  public String getClientName() {
    return clientName;
  }

  public void setChatroomName(String chatroomName) {
    this.chatroomName = chatroomName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }
}
