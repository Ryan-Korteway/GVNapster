package com.company;

import javafx.beans.property.adapter.JavaBeanObjectProperty;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Blaze on 10/24/16.
 */
public class NapsterView implements ActionListener{

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

    JTable serverResults;

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

    JScrollPane resultsScroll;

    TableModel ourModel;

    Socket localSocket;

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
        userIP = new JLabel("Enter Your IP address:");

        serverIP = new JTextField(); //JTextFields for single lines of code, JTextArea's for multi lines of text.
        portNum = new JTextField();
        usernameBox = new JTextField();
        userIPBox = new JTextField();

        connectButton = new JButton("Connect");
        quitButton = new JButton("Disconnect");

        String[] ourOptions = {"Ethernet", "Wi-Fi", "T-1", "T-3"};
        linkSelector = new JComboBox<String>(ourOptions); //random warning about an unchecked call

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

        ourModel = new TableModel();
        serverResults = new JTable(ourModel);

        resultsScroll = new JScrollPane(serverResults);

        searchButton.addActionListener(this);

        searchPanel.add(keywordLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        searchPanel.add(resultsScroll, BorderLayout.SOUTH); //scroll panel needed to show headers.

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
        ourFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //might need to be something else.
    }
    @Override
    public void actionPerformed(ActionEvent e) {

        //actions performed need to check for the connect and disconnect buttons being pushed to set up
        //and tear down the connection/socket to the central/Database server
        //part of the checking options for connect will be to make sure all of the fields from the connect panel are
        //filled in, all users need to have a username, all connections need a valid port number, etc.

        //with the go button we need to check if its a connect, RETR, or quit command and each of these three
        //depend on a locally created socket that is directed at the peers server after getting the IP
        //of said peer from the searchResults JTable.
        //connect and quit set up the local socket and then retr can be used with it to get lots of stuff from the
        //peer's server.

        //the go button is used to send the keyword for the search term to the central server with the globally created/available
        //socket. there is a search server method that is in the client class that just needs the socket of the server to
        //direct the query at and the string that is being searched for, aka the keyword from the searchField JTextField.

        //all of these will need proper boolean flags to make sure you are not trying to quit before you connect, connect
        //before quitting the last server etc. no retrieving before connecting to a peer server, no quitting a peer server before
        //connecting etc.

            if(e.getSource() == connectButton) {
                serverIPString = serverIP.getText();
                portNumString = portNum.getText();
                userNameString = usernameBox.getText();
                speedString = linkSelector.getSelectedItem().toString();
                String userIPaddress = userIPBox.getText();

                if (!serverIPString.equals("") && !portNumString.equals("") && !userNameString.equals("") && !userIPaddress.equals("")) {
                    System.out.println("IP: " + serverIPString + " Port: " + portNumString + " Name: " + userNameString);
                    int portNumInt = Integer.parseInt(portNumString);
                    try {
                        centralServer = ourClientToServer.connect(serverIPString, portNumInt, userIPaddress, userNameString, speedString);
                    } catch (Exception e1) {
                        System.out.println("ClientToServer Unknown Host Exception.");
                        JOptionPane.showMessageDialog(null, e1.getMessage()); //replacing print lines with pop ups.
                        e1.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields");
                }
            }

            if(e.getSource() == quitButton){
                try {
                    ourClientToServer.quitServer(centralServer);
                }catch(Exception E){
                    JOptionPane.showMessageDialog(null, "Could not disconnect, are you connected to a server?");
                }
            }

            if(e.getSource() == searchButton){
                try {
                    if(centralServer != null) {
                        String keyword = searchField.getText();
                        Object[][] ourResults = ourClientToServer.searchServer(keyword, centralServer);
                        if(ourResults == null){
                            JOptionPane.showMessageDialog(null, "Results were null. An error has occurred.");
                        }
                        else {
                            ourModel.setOurData(ourResults);
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

                        //TODO update the if statement for central server stuff and undo the host exception catch block being commented out.
                        if (!userNameString.equals("")){ //TODO && (centralServer != null)) {
                            ourClientToPeer.getFile(command[1], localSocket);
                            ourClientToPeer.getFileMetaData(command[1], localSocket);
                            //TODO uncomment the line below once our central server is ready/involved
                            //TODO ourClientToServer.sendServerMetaData(centralServer, InetAddress.getLocalHost().getHostName(), userNameString, speedString);
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
                    }// catch (UnknownHostException e1) {
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