package com.company.sorchanolan;

public class ClientJoinInstance {
  private String clientName;
  private int joinId;
  private int roomRef;

  public ClientJoinInstance() {
    this.clientName = "";
    this.joinId = -1;
    this.roomRef = -1;
  }

  public ClientJoinInstance(String clientName, int joinId, int roomRef) {
    this.clientName = clientName;
    this.joinId = joinId;
    this.roomRef = roomRef;
  }

  public String getClientName() {
    return clientName;
  }

  public int getJoinId() {
    return joinId;
  }

  public int getRoomRef() {
    return roomRef;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public void setJoinId(int joinId) {
    this.joinId = joinId;
  }

  public void setRoomRef(int roomRef) {
    this.roomRef = roomRef;
  }
}
