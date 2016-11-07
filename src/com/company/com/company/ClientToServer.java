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

/**
 * The ClientToServer class handling interactions between our user and the central server.
 * Created by Blaze and others on 10/31/16.
 */
public class ClientToServer {

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
            BufferedReader inputS = new BufferedReader(new InputStreamReader(controlConnection.getInputStream()));

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

    public void quitServer(Socket givenSocket) {
        try {
            System.out.println("Thank you for using the program.");
            BufferedReader inputS = new BufferedReader(new InputStreamReader(givenSocket.getInputStream()));
            BufferedWriter outputS = new BufferedWriter(new OutputStreamWriter(givenSocket.getOutputStream()));
            outputS.write("QUIT\r\n"); //once quit is sent to the central server, our records of what we host must be
            //deleted and the server must say that we quit. no passing of username because then quit doesn't work on the peer
            //to peer servers
            outputS.close();
            inputS.close();
            givenSocket.close();
        } catch (Exception e) {
            System.out.println("Things happened");
        }
    }


    public Object[][] searchServer(String searchingFor, Socket serverSocket) {

        try {
            BufferedReader inputS = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            BufferedWriter outputS = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
            //sending stuff to the server
            outputS.write("SEARCH " + searchingFor + " \r\n");
            outputS.flush();

            String resultsSize = "";
            while(resultsSize.equals("")){
                resultsSize = inputS.readLine(); //read the size of the results array,
            }

            Object[][] ourData = new Object[Integer.parseInt(resultsSize)][3];

            String[] instanceData;
            for(int x = 0; x < Integer.parseInt(resultsSize); x++){
                instanceData = inputS.readLine().split(",");
                ourData[x][0] = instanceData[0]; //link speed
                ourData[x][1] = instanceData[1]; //host ip address
                ourData[x][2] = instanceData[2]; //file name
            }

            return ourData;

        } catch (Exception e) {
            System.out.println("Something happened with search");
            return null;
        }
    }

    public void sendServerMetaData(Socket serverSocket, String ipAddress, String usernameParam, String speed){
        try {
            DocumentBuilderFactory myDocFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder myDocBuild = myDocFact.newDocumentBuilder();

            File origMetaFile = new File("./data/meta.xml");
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

            File metaToSend = new File("./data/toServer.xml");

            //this stuff down here is the stuff that saves the file back to our disk, thus allowing us to resend it.
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(ourOrigFile);
            StreamResult result = new StreamResult(metaToSend);
            transformer.transform(source, result);

            System.out.println("new meta data file created");

            //file should now be created so send it out via the data output stream.
            FileInputStream ourFileToRead = new FileInputStream(metaToSend);
            BufferedInputStream dataFromFile = new BufferedInputStream(ourFileToRead);

            byte[] ourBytes = new byte[ (int) metaToSend.length()];

            System.out.println("about to read file");
            int bytesToSend = dataFromFile.read(ourBytes, 0, ourBytes.length);

            System.out.println("File read.");
            BufferedWriter comWriter = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
            BufferedReader comReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            System.out.println("about to write meta");

            comWriter.write("META " + bytesToSend, 0, ("META " + bytesToSend).length());
            comWriter.write("\r\n", 0, "\r\n".length());
            comWriter.flush();

            String ourResult = comReader.readLine();

            System.out.println("our result " + ourResult);

            if(ourResult.contains("PORT:")){

                String[] ourwords = ourResult.split(" ");
                int ourPort = Integer.parseInt(ourwords[1]);

                Socket fileSendingSocket = new Socket(serverSocket.getInetAddress(), ourPort);
                BufferedOutputStream dataOut = new BufferedOutputStream(fileSendingSocket.getOutputStream());

                dataOut.write(ourBytes, 0, ourBytes.length); //shouldn't need a flush thanks to the close, but doing it anyways.
                dataOut.flush();

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
}
