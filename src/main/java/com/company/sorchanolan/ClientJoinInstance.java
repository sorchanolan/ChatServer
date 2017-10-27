package com.company.sorchanolan;

public class ClientJoinInstance {
  private Client client;
  private int joinId;
  private int roomRef;

  public ClientJoinInstance() {
    this.client = new Client();
    this.joinId = -1;
    this.roomRef = -1;
  }

  public ClientJoinInstance(Client client, int joinId, int roomRef) {
    this.client = client;
    this.joinId = joinId;
    this.roomRef = roomRef;
  }

  public Client getClient() {
    return client;
  }

  public int getJoinId() {
    return joinId;
  }

  public int getRoomRef() {
    return roomRef;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public void setJoinId(int joinId) {
    this.joinId = joinId;
  }

  public void setRoomRef(int roomRef) {
    this.roomRef = roomRef;
  }
}
