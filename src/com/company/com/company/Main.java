package com.company;

public class Main {

    public static void main(String[] args) {
        ftp_server localServer = new ftp_server();
        Thread forServer = new Thread(localServer);
        forServer.start();

        ClientToPeer ourClientToPeer = new ClientToPeer();
        ClientToServer ourClientToServer = new ClientToServer();

        NapsterView ourView = new NapsterView(ourClientToPeer, ourClientToServer);
    }

}
