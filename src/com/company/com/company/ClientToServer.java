package com.company;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * The ClientToServer class handling interactions between our user and the central server.
 * Created by Blaze and others on 10/31/16.
 */
public class ClientToServer {

    BufferedReader inputS;

    BufferedWriter outputS;

    public ClientToServer() {
        //Our constructor, not sure what it needs honestly.

    }

    Socket connect(String ipAddr, int port) {
        //connect to the server in question here, and then in the view, have the view send the central server the username and
        //link speed stuff later.
        try {
            Socket listenerConnection = new Socket(ipAddr, port);
            BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(listenerConnection.getInputStream()));
            String connectPort = inputFromServer.readLine();
            System.out.println(connectPort);
            listenerConnection.close(); //might need to murphy proof this by sending out a reply to the server that we got through.

            String[] ourResults = connectPort.split(" ");

            int intConnectPort = Integer.parseInt(ourResults[1]);
            System.out.println("our connectPort " + intConnectPort);
            Socket controlConnection = new Socket(ipAddr, intConnectPort);
            System.out.println("Socket made");

            inputS = new BufferedReader(new InputStreamReader(controlConnection.getInputStream()));
            outputS = new BufferedWriter(new OutputStreamWriter(controlConnection.getOutputStream()));

            System.out.println("about to read line");
            String response = inputS.readLine();

            if (response.contains("Line ready")) {
                System.out.println("Line is ready.");
                return controlConnection;
            } else {
                return null;
            }

        } catch (Exception e) {
            System.out.println("Connection Exception. " + e.toString());
            return null;
        }

    }

    public void quitServer(Socket givenSocket, String userName, String ipAddress) {
        try {
            System.out.println("Thank you for using the program.");
            //BufferedReader inputS = new BufferedReader(new InputStreamReader(givenSocket.getInputStream()));
            //BufferedWriter outputS = new BufferedWriter(new OutputStreamWriter(givenSocket.getOutputStream()));
            outputS.write("QUIT " + userName + " " + ipAddress + " \r\n"); //once quit is sent to the central server, our records of what we host must be
            //deleted and the server must say that we quit. no passing of username because then quit doesn't work on the peer
            //to peer servers
            outputS.close();
            inputS.close();
            givenSocket.close();
        } catch (Exception e) {
            System.out.println("Things happened");
        }
    }


    public String[] searchServer(String searchingFor, Socket serverSocket) {

        try {
            //BufferedReader inputS = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            //BufferedWriter outputS = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
            //sending stuff to the server

            System.out.println("about to read first line for search");
            String response = inputS.readLine();
            System.out.println("response " + response);
            if(response.contains("Line ready")) {
                outputS.write("SEARCH " + searchingFor + " \r\n");
                outputS.flush();
                System.out.println("Search sent.");
            }

            Object[][] ourData;
            String resultsSize = "";

            while(true) {
                try {
                    resultsSize = inputS.readLine(); //read the size of the results array,

                    System.out.println("reply read. " + resultsSize);
                    ourData = new Object[(Integer.parseInt(resultsSize)) + 1][3];
                    break;
                } catch( NumberFormatException N){
                    JOptionPane.showMessageDialog(null, "bad read");
                }
            }

            System.out.println(ourData.length);
            ourData[0][0] = "Search term: " + searchingFor;
            ourData[0][1] = "Number of Results:";
            ourData[0][2] = resultsSize;

            System.out.println("got past the parse.");

            String[] instanceData = new String[Integer.parseInt(resultsSize)+1];
            instanceData[0] = "Search term: " + searchingFor + " Number of Results: " + resultsSize;

            for(int x = 1; x <= (Integer.parseInt(resultsSize)); x++){
                String serverLine = inputS.readLine();
                String[] ourLineParts = serverLine.split(",");
                System.out.println("our line here " + serverLine);

                instanceData[x] = ourLineParts[0] + " " + ourLineParts[1] + " " + ourLineParts[2] + "\r\n";
            }

            System.out.println("About to return instance");
            return instanceData;

        } catch (Exception e) {
            System.out.println("Something happened with search: " + e.toString());
            //e.printStackTrace();
            return null;
        }
    }

    public void sendServerMetaData(Socket serverSocket, String ipAddress, String usernameParam, String speed){
        try {
            DocumentBuilderFactory myDocFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder myDocBuild = myDocFact.newDocumentBuilder();

            File origMetaFile = new File("data/meta.xml");
            Document ourOrigFile = myDocBuild.parse(origMetaFile);

            System.out.println("meta data parsed");

            Node connectionNode = ourOrigFile.createElement("connection");

            Element ipAddr = ourOrigFile.createElement("address");
            ipAddr.appendChild(ourOrigFile.createTextNode(ipAddress));

            Element uName = ourOrigFile.createElement("username");
            uName.appendChild(ourOrigFile.createTextNode(usernameParam));

            Element linkSpeed = ourOrigFile.createElement("speed");
            linkSpeed.appendChild(ourOrigFile.createTextNode(speed));

            connectionNode.appendChild(ipAddr);
            connectionNode.appendChild(uName);
            connectionNode.appendChild(linkSpeed);

            ourOrigFile.getFirstChild().appendChild(connectionNode); //need first child so we are not trying to append to the
            //unchangeable root of the xml file.

            System.out.println("meta data updated with user info");

            File metaToSend = new File("data/toServer.xml");

            //this stuff down here is the stuff that saves the file back to our disk, thus allowing us to resend it.
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(ourOrigFile);
            StreamResult result = new StreamResult(metaToSend);
            transformer.transform(source, result);

            System.out.println("new meta data file created");

            BufferedWriter comWriter = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
            BufferedReader comReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            System.out.println("about to write meta command.");

            comWriter.write("META", 0, "META".length());
            comWriter.write("\r\n", 0, "\r\n".length());
            comWriter.flush();

            String ourResult = comReader.readLine();

            System.out.println("our result " + ourResult);

            if(ourResult.contains("PORT:")){

                String[] ourwords = ourResult.split(" ");
                int ourPort = Integer.parseInt(ourwords[1]);

                System.out.println(ourPort);
                Socket fileSendingSocket = new Socket(serverSocket.getInetAddress(), ourPort);
                BufferedWriter dataOut = new BufferedWriter(new OutputStreamWriter(fileSendingSocket.getOutputStream()));

                //TODO need a while loop to go through every line of the xml file and send that across as lines of strings. dont forget new lines
                //need to read in a line from our file, and then write it out to our server.
                    try {
                        ArrayList<String> ourFile = getFile("toServer.xml");

                        for (String lineContents : ourFile) {
                            dataOut.write(lineContents);
                            dataOut.flush();
                        }

                        dataOut.write("EOF" + "\r\n");
                        dataOut.flush();

                    } catch (Exception e) {
                        //response for a file not found error that would then be written out and the downloading of the file wouldn't happen on the
                        //client end.
                        System.out.println();
                    }

                System.out.println("Write done and flushed.");

                dataOut.close();
                fileSendingSocket.close();

                Files.deleteIfExists(metaToSend.toPath());
            }
            else{
                JOptionPane.showMessageDialog(null, "We were unable to create a socket to send the metadata file out through.");
            }

        } catch (IOException e) {
            System.out.println("Whoops in metadata opening");
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            System.out.println("Whoops in parser config");
            e.printStackTrace();
        } catch (SAXException e) {
            System.out.println("Whoops in SAX stuff");
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            System.out.println("Whoops in transformer configuration.");
            e.printStackTrace();
        } catch (TransformerException e) {
            System.out.println("Whoops in transformer transforming");
            e.printStackTrace();
        }
    }

    private ArrayList<String> getFile(String fileName) throws Exception {
        ArrayList<String> export = new ArrayList<String>();
        File targetFile = new File("data/" + fileName);
        BufferedReader fileReader = new BufferedReader(new FileReader(targetFile));
        String curLine;
        while ((curLine = fileReader.readLine()) != null) {
            export.add(curLine+"\r\n");
        }
        fileReader.close();
        return export;

    }

}
