package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/**
 * The ftp request java file made by Tensei Nguyen and company
 */
final class FtpRequest implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    BufferedWriter os;

    // constructor
    public FtpRequest(Socket socket) throws Exception {
        this.socket = socket; //TODO this is the clients control socket.
    }

    // runnable run
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {
        String commandExport = "";

        // output stream
        os = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // input stream
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //get an open socket here, send it over to the client,
        // then use it to make the data server socket both in get file and get meta file.
        int usersPort = 0;
        while (usersPort == 0) {
            usersPort = getOpenPort();
        }

        // Send Welcome Response to ClientToPeer
        response(os, "Response: 220 Welcome to JFTP." + CRLF);

        // command loop
        //TODO reference: https://en.wikipedia.org/wiki/List_of_FTP_server_return_codes
        String commandRequest;
        while(true) {
            String commandLine = br.readLine();

            if (commandLine == null) {
                continue;
            } else {
                System.out.println("command line results " + commandLine);

                String[] clientCommand = commandLine.split(" ");//tokens.nextToken();

                if (clientCommand.length == 1) {
                    commandExport = clientCommand[0];
                } else {
                    String clientArg = clientCommand[1];//tokens.nextToken();
                    commandExport = clientCommand[0] + " " + clientArg;
                }


                if (clientCommand[0].equals("RETR")) {
                    //response(os, "Response: 202 RETR not implemented." + CRLF);
                    boolean report = false;
                    ServerSocket dataSock = new ServerSocket(usersPort);

                    response(os, "Response: 225 Data Connection Open.," + usersPort + "," + CRLF);
                    while (true) {
                        Socket dataConn = dataSock.accept();

                        // Create Data Handler
                        DataRequest dataHandler = new DataRequest(dataConn, commandExport);

                        // Data handler Thread
                        Thread dThread = new Thread(dataHandler);

                        // run
                        dThread.start();
                        dThread.join();
                        report = dataHandler.getError();
                        if (!report) {
                            response(os, "Response: 226 Closing data connection." + CRLF);
                        } else {
                            response(os, "Response: 426 Something broke." + CRLF);
                        }

                        dataSock.close(); //added to close things
                        break; //added to get us back to listening for other instructions.
                    }
                    response(os, "Response: 226 Closing data connection." + CRLF); //pretty sure never gets called but leaving alone

                }

                if (clientCommand[0].equals("RETR_M")) {
                    //response(os, "Response: 202 RETR not implemented." + CRLF);
                    boolean report = false;
                    ServerSocket dataSock = new ServerSocket(usersPort); //TODO the socket needs to have the same port number as the getfile if statement.

                    response(os, "Response: 225 Data Connection Open.," + usersPort + "," + CRLF);
                    while (true) {
                        Socket dataConn = dataSock.accept();

                        // Create Data Handler
                        DataRequest dataHandler = new DataRequest(dataConn, commandExport);

                        // Data handler Thread
                        Thread dThread = new Thread(dataHandler);

                        // run
                        dThread.start();
                        dThread.join();
                        report = dataHandler.getError();
                        if (!report) {
                            response(os, "Response: 226 Closing data connection." + CRLF);
                        } else {
                            response(os, "Response: 426 Something broke." + CRLF);
                        }

                        dataSock.close(); //added to close things
                        break; //added to get us back to listening for other instructions.
                    }
                    response(os, "Response: 226 Closing data connection." + CRLF); //pretty sure never gets called but leaving alone

                }

                if (clientCommand[0].equals("QUIT")) {
                    response(os, "Response: 221 Closing connection." + CRLF);
                    break;
                }

            }

        }
        //TODO RELOCATED THE closing stuff.
        os.close();
        br.close();
        socket.close();
    }

    private void response(BufferedWriter os, String res) throws Exception {
        System.out.println(res);
        os.write(res, 0, res.length());
        os.write("\r\n", 0, "\r\n".length());
        os.flush();
    }

    private ArrayList<String> getDirectory() {
        ArrayList<String> listContents = new ArrayList<String>();

        File dir = new File("./data"); //one dot for in IDE, two for CLI usage
        File[] dirList = dir.listFiles();

        for (File file : dirList) {
            listContents.add(file.getName());
        }

        return listContents;
    }

    private int getOpenPort(){
        Random nextRandom = new Random();
        try{
            int ourPort = nextRandom.nextInt(16299)+49200;
            ServerSocket tester = new ServerSocket(ourPort); //49200 - 65500
            System.out.println("generated our port " + ourPort);
            tester.close();
            return ourPort;
        }
        catch(Exception e){
            return 0;
        }
    }


}
