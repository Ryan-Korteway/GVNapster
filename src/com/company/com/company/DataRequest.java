package com.company;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * The data request java file made by Tensei Nguyen and company
 */

public class DataRequest implements Runnable{
    final static String CRLF = "\r\n";
    Socket socket;
    String command;
    private volatile boolean error;
    private volatile boolean complete = false;

    public DataRequest(Socket socket, String command) throws Exception {
        this.socket = socket;
        this.command = command;
    }

    public void run(){
        try {
            processRequest();
        }
        catch (Exception e)
        {
            error = true;
        }
    }

    private void processRequest() throws Exception {
        // output stream
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        DataInputStream  is = new DataInputStream(socket.getInputStream());


        if (command.contains("RETR_M")) {
            String fileName = command.substring(7);
            try {
                //parse the file, if the name node value matches the file name, then write it out.
                DocumentBuilderFactory myDBF = DocumentBuilderFactory.newInstance();
                DocumentBuilder myDB = myDBF.newDocumentBuilder();
                File serverXML = new File("./data/meta.xml");

                Document docXML = myDB.parse(serverXML);

                Element elementXML = docXML.getDocumentElement();

                NodeList ourNodes = elementXML.getChildNodes();

                for(int x = 0; x < ourNodes.getLength(); x++){
                    if(ourNodes.item(x).getNodeType() == Node.ELEMENT_NODE){
                        Element here = (Element) ourNodes.item(x);
                        if(here.getNodeName().contains("file")){
                            String nodeFileName = here.getElementsByTagName("name").item(0).getTextContent();
                            if(nodeFileName.equals(fileName)){
                                String results = here.getElementsByTagName("desc").item(0).getTextContent();
                                os.writeUTF(results + "\r\n"); //todo Might not need the new line here.
                                os.flush();
                            }
                        }
                    }
                }

                os.writeUTF("EOF" + CRLF);
                os.flush();
                error = false;
            } catch (Exception e) {
                //response for a file not found error that would then be written out and the downloading of the file wouldn't happen on the
                //client end.
                throw e;
            }

        } else if (command.contains("RETR")) {
                String fileName = command.substring(5); //gets just the file name for our method to use. will need to be updated
                //to properly handle the RETR_M commands.
                try {
                    ArrayList<String> ourFile = getFile(fileName);

                    for (String lineContents : ourFile) {
                        os.writeUTF(lineContents);
                        os.flush();
                    }

                    os.writeUTF("EOF" + CRLF);
                    os.flush();
                    error = false;
                } catch (Exception e) {
                    //response for a file not found error that would then be written out and the downloading of the file wouldn't happen on the
                    //client end.
                    throw e;
                }
            }

            JOptionPane.showMessageDialog(null, "Streams and sockets stuff closed.");
            is.close();
            os.close(); //TODO commented out because it seems like the FTP request should close those things once quit is sent and the user disconnects.
            socket.close(); //TODO gonna trace it and try to figure it out later, see if it really should be doing it or not.
                                //TODO this will be an issue or not for sure once i try to retrieve multiple files in a row from one peer.

        //TODO uncommented it again to save for the testing phase, if it works like this in project 1 then dont potentially break it in part two trying to fix non issues.
    }

    private ArrayList<String> getFile(String fileName) throws Exception {
        ArrayList<String> export = new ArrayList<String>();
        File targetFile = new File("./data/" + fileName);
        BufferedReader fileReader = new BufferedReader(new FileReader(targetFile));
        String curLine;
        while ((curLine = fileReader.readLine()) != null) {
            export.add(curLine);
        }
        fileReader.close();
        return export;

        }

    public boolean getError(){
        return error;
    }
}
