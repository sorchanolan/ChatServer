package com.company.sorchanolan;

public class ClientJoinInstance {
  Client client;
  int joinId;

  public ClientJoinInstance() {
    this.client = new Client();
    this.joinId = -1;
  }

  public Client getClient() {
    return client;
  }

  public int getJoinId() {
    return joinId;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public void setJoinId(int joinId) {
    this.joinId = joinId;
  }
}
