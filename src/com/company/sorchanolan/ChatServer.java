package com.company.sorchanolan;

import java.io.*;
import java.net.*;

class ChatServer {

    public static void main(String argv[]) throws Exception {
        String clientSentence, capitalisedSentence;
        ServerSocket welcomeSocket = new ServerSocket(6789);

        while (true) {
            System.out.println("Begin");
            Socket conSocket = welcomeSocket.accept();
            BufferedReader inFromClient = new
                    BufferedReader(new
                    InputStreamReader(
                    conSocket.getInputStream()));
            DataOutputStream outToClient = new
                    DataOutputStream(conSocket.getOutputStream());
            clientSentence = inFromClient.readLine();
            capitalisedSentence = clientSentence.toUpperCase() + '\n';
            outToClient.writeBytes(capitalisedSentence);
        }
    }
}
