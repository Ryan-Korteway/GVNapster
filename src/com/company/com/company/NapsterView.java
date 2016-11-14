package com.company;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;


/** NapsterView class for the generation of our gui.
 * Created by Blaze on 10/24/16.
 */
public class NapsterView implements ActionListener{

    private JTextArea textResults;

    // start of the overall variables and the connect panel variables.

    Socket centralServer; //our globally accessible socket to the centralized server.

    ClientToPeer ourClientToPeer;

    ClientToServer ourClientToServer;

    JFrame ourFrame;

    JPanel overPanel;

    JPanel connectPanel;

    JPanel searchPanel;

    JPanel functionPanel;

    JLabel serverHostname;

    JLabel serverPort;

    JLabel username;

    JLabel speedLabel;

    JLabel userIP;

    JTextField serverIP;

    JTextField portNum;

    JTextField usernameBox;

    JTextField userIPBox;

    JButton connectButton;

    JButton quitButton;

    JComboBox linkSelector;

    // start of the search panel variables

    //JLabel searchLabel;

    JLabel keywordLabel;

    JTextField searchField;

    JButton searchButton;

    // start of the function panel variables.

    //JLabel ftpLabel;

    JLabel commandLabel;

    JTextField commandField;

    JButton goButton;

    JTextArea programOutput;

    //miscellaneous add on variables.

    private String serverIPString, portNumString, userNameString;

    String speedString;

    Boolean connected = false;

    Socket localSocket;

    String userIPaddress;

    public NapsterView(ClientToPeer passedClientToPeer, ClientToServer passedClientToServer){

        ourClientToPeer = passedClientToPeer;

        ourClientToServer = passedClientToServer;

        ourFrame = new JFrame();

        overPanel = new JPanel(new BorderLayout());

        connectPanel = new JPanel(new GridLayout(3,5));
        connectPanel.setBorder(new TitledBorder(new LineBorder(Color.gray, 3), "Connection"));
        searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(new TitledBorder(new LineBorder(Color.gray, 3), "Search"));
        functionPanel = new JPanel(new BorderLayout());
        functionPanel.setBorder(new TitledBorder(new LineBorder(Color.gray, 3), "FTP"));

        //connect panel items
        serverHostname = new JLabel("Server Hostname: ");
        serverPort = new JLabel("Port: ");
        username = new JLabel("Username: ");
        speedLabel = new JLabel("Speed/kind of link: ");
        userIP = new JLabel("User IP address:");

        serverIP = new JTextField(); //JTextFields for single lines of code, JTextArea's for multi lines of text.
        portNum = new JTextField();
        usernameBox = new JTextField();
        userIPBox = new JTextField();

        connectButton = new JButton("Connect");
        quitButton = new JButton("Disconnect");

        String[] ourOptions = {"Ethernet", "Wi-Fi", "T-1", "T-3"};
        linkSelector = new JComboBox<>(ourOptions); //random warning about an unchecked call

        connectButton.addActionListener(this);
        quitButton.addActionListener(this);
        linkSelector.addActionListener(this);

        connectPanel.add(serverHostname);
        connectPanel.add(serverIP);

        connectPanel.add(serverPort);
        connectPanel.add(portNum);

        connectPanel.add(connectButton);

        connectPanel.add(username);
        connectPanel.add(usernameBox);

        connectPanel.add(speedLabel);
        connectPanel.add(linkSelector);

        connectPanel.add(quitButton);

        connectPanel.add(userIP);

        connectPanel.add(userIPBox);

        connectPanel.add(new JLabel("  "));

        connectPanel.add(new JLabel("  "));

        connectPanel.add(new JLabel("  "));

        //search panel items

        keywordLabel = new JLabel("Keyword: ");

        searchField = new JTextField();

        searchButton = new JButton("Search");

        textResults = new JTextArea("Link Speed      User IP address     File Name");
        textResults.setEditable(false);

        searchButton.addActionListener(this);

        searchPanel.add(keywordLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        searchPanel.add(textResults, BorderLayout.SOUTH);

        //function panel items

        commandLabel = new JLabel("Enter Command: ");

        commandField = new JTextField();

        goButton = new JButton("Go");

        programOutput = new JTextArea();

        goButton.addActionListener(this);

        functionPanel.add(commandLabel, BorderLayout.WEST);
        functionPanel.add(commandField, BorderLayout.CENTER);
        functionPanel.add(goButton, BorderLayout.EAST);
        functionPanel.add(programOutput, BorderLayout.SOUTH);

        //putting everything together.
        overPanel.add(connectPanel, BorderLayout.NORTH);
        overPanel.add(searchPanel, BorderLayout.CENTER);
        overPanel.add(functionPanel, BorderLayout.SOUTH);

        ourFrame.add(overPanel);
        ourFrame.pack();
        ourFrame.setVisible(true);
        ourFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    @Override
    public void actionPerformed(ActionEvent e) {

            if(e.getSource() == connectButton) {
                serverIPString = serverIP.getText();
                portNumString = portNum.getText();
                userNameString = usernameBox.getText();
                speedString = linkSelector.getSelectedItem().toString();
                userIPaddress = userIPBox.getText();

                if (!serverIPString.equals("") && !portNumString.equals("") && !userNameString.equals("") && !userIPaddress.equals("")) {
                    System.out.println("IP: " + serverIPString + " Port: " + portNumString + " Name: " + userNameString);
                    int portNumInt = Integer.parseInt(portNumString);
                    try {
                        centralServer = ourClientToServer.connect(serverIPString, portNumInt);
                        System.out.println("about to send meta data");
                        ourClientToServer.sendServerMetaData(centralServer, userIPaddress, userNameString, speedString);
                    } catch (Exception e1) {
                        System.out.println("ClientToServer Unknown Host Exception.");
                        JOptionPane.showMessageDialog(null, "Server is null, maybe it is down?"); //replacing print lines with pop ups.
                        e1.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields");
                }
            }

            if(e.getSource() == quitButton){
                try {
                    if(usernameBox.getText() != "" && userIPBox.getText() != "") {
                        ourClientToServer.quitServer(centralServer, usernameBox.getText(), userIPBox.getText());
                    } else{
                        JOptionPane.showMessageDialog(null, "Need to have a username and address to tell the server who is quitting.");
                    }
                }catch(Exception E){
                    JOptionPane.showMessageDialog(null, "Could not disconnect, are you connected to a server?");
                }
            }

            if(e.getSource() == searchButton){
                try {
                    if(centralServer != null) {
                        String keyword = searchField.getText();
                        String[] ourResults = ourClientToServer.searchServer(keyword, centralServer);
                        if(ourResults == null){
                            JOptionPane.showMessageDialog(null, "Results were null. An error has occurred.");
                        }
                        else {
                            textResults.setText("");
                            textResults.append("Link Speed      User IP address     File Name\r\n");

                            System.out.println("about to for loop");
                            for(int x = 0; x < ourResults.length; x++)
                            {
                                textResults.append(ourResults[x] + "\r\n");
                            }
                            System.out.println("After for Loop ");
                            searchPanel.repaint();
                            ourFrame.repaint();
                        }
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Server is null, who do we try to search?");
                    }
                }catch(Exception ex){
                    JOptionPane.showMessageDialog(null, "something went wrong with search, are you connected to a server?");
                }
            }

            if(e.getSource() == goButton){
                programOutput.append( commandField.getText() + "\r\n");
                //need boolean flags for connect, retr, and quit order of ops
                String[] command = (commandField.getText().split(" "));
                //JOptionPane.showMessageDialog(null, connected.toString() + " " + command[0]);
                if(!connected && (command[0].toLowerCase()).equals("connect")){
                    try {
                        localSocket = ourClientToPeer.connect(command[1], 3715);
                        localSocket.getLocalPort();
                        connected = true;
                        programOutput.append("Connected to the server.\n");

                    }catch(ArrayIndexOutOfBoundsException arInEx){
                        JOptionPane.showMessageDialog(null, "Please provide a valid IP address before trying to connect to a server.");
                    }catch (NullPointerException neo){
                        System.out.println("null socket returned.");
                    }
                }
                else if(connected && (command[0].toLowerCase()).equals("connect")){
                    JOptionPane.showMessageDialog(null, "Need to disconnect from the current server before you can connect to a new one.");
                }
                else if(connected && (command[0].toLowerCase()).equals("retr")){
                    try {
                        userNameString = usernameBox.getText();
                        speedString = linkSelector.getSelectedItem().toString();

                        if (!userNameString.equals("") && (centralServer != null)) {
                            ourClientToPeer.getFile(command[1], localSocket);
                            JOptionPane.showMessageDialog(null, "About to get meta data.");
                            ourClientToPeer.getFileMetaData(command[1], localSocket);
                            ourClientToServer.sendServerMetaData(centralServer, userIPaddress, userNameString, speedString);
                            programOutput.append("File retrieved.\n");
                        }
                        else{
                            JOptionPane.showMessageDialog(null, "Please fill in all fields. " +
                                    "\nNeed a username for the central server to know that your \n" +
                                    "particular meta data file has been updated. You also  must be connected to a central server.");
                        }
                    }catch(ArrayIndexOutOfBoundsException arInEx){
                        JOptionPane.showMessageDialog(null, "Please provide a valid file name before trying to retrieve a file.");
                    }catch(NullPointerException ne){
                        JOptionPane.showMessageDialog(null, "local or server socket was null, shouldn't be happening.");
                    } // catch (UnknownHostException e1) {
//                        e1.printStackTrace();
//                    }
                }
                else if(!connected && (command[0].toLowerCase()).equals("retr")){
                    JOptionPane.showMessageDialog(null, "Need to connect to a server before you can retrieve a file.");
                }
                else if(connected && (command[0].toLowerCase()).equals("quit")){
                    try {
                        ourClientToPeer.quitServer(localSocket);
                        connected = false;
                        programOutput.append("Disconnected from the server.\n");
                    }catch(Exception ex){
                        JOptionPane.showMessageDialog(null, "A quit exception has occurred.");
                    }
                }
                else if(!connected && (command[0].toLowerCase()).equals("quit")){
                    JOptionPane.showMessageDialog(null, "Need to connect to a server before you can quit a server.");
                }
                else{
                    JOptionPane.showMessageDialog(null, "Command not recognized.\n Valid commands are connect, " +
                            "retr followed by a file name,\n or quit. Certain commands cannot be performed without first connecting or disconnecting from the current server. ");
                }
            }

    }

}
/*
the commented out code grave yard that might still have some useful things to teach us.
JOptionPane.showMessageDialog(null, "should repaint.");
                Object[][] newData = new Object[][]{{"new test", "new test", "new test"}};
                ourModel.setOurData(newData);
 */