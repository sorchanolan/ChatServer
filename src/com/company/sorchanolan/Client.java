package com.company.sorchanolan;

import java.io.*;
import java.net.*;

class Client {

    public static void main(String argv[]) throws Exception {
        String sentence, modifiedSentence;

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = new Socket("localhost", 6789);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        sentence = inFromUser.readLine();
        outToServer.writeBytes(sentence + '\n');
        modifiedSentence = inFromServer.readLine();

        System.out.println("FROM SERVER: " + modifiedSentence);

        inFromServer.close();
        outToServer.close();
        inFromUser.close();
        clientSocket.close();
    }
}