import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.*;

interface GlobalConstants
{
    //Disconnect msg
    String DISCONNECT_HEAD = ":disconnect";

    //Status msg
     String SUCCESS_HEAD = ":success";
     String FAIL_HEAD = ":fail";

    //Chat
    String CREATE_CHAT_HEAD = ":createchat";
    String JOIN_CHAT_HEAD = ":joinchat";
    String CHAT_HEAD = ":chat";
    String LEAVE_CHAT_HEAD = ":leavechat";

    //File
    String UPLOAD_HEAD = ":upload";  //client upload
    String DOWNLOAD_REQUEST_HEAD = ":download"; //request from client
    String FILE_SEND_HEAD = ":file"; //server send file back
}

public class server {
    public static void main(String[]args) {
        svGUI gui = new svGUI();
        gui.CreateGUI();
    }
}

class Dialog {
    public void showDialog(String message) {
        JOptionPane.showMessageDialog(null, message);
    }
}

class DocumentController {
    JTextPane messagePane;
    StyledDocument doc;

    Dialog dialog = new Dialog();

    public DocumentController (JTextPane textPane, StyledDocument styleDoc) {
        this.messagePane = textPane;
        this.doc = styleDoc;
    }

    public void insertMessage(String message) {
        try {
            this.doc.insertString(this.doc.getLength(), message + "\n", null);
        } catch (Exception e) {
            dialog.showDialog(e.toString());
        }
    }

    public void insertMessageCurrentLine(String message) {
        try {
            this.doc.insertString(this.doc.getLength(), message, null);
        } catch (Exception e) {
            dialog.showDialog(e.toString());
        }
    }
}

class svGUI implements ActionListener {
    JTextPane messageArea;
    StyledDocument doc;

    JButton startBtn;
    JFrame frame;
    Dialog dialog;

    //Server
    svNet server_controller;
    Thread serverThread;

    public svGUI() {
        messageArea = new JTextPane();

        startBtn = new JButton("Start Server >");
        startBtn.setActionCommand("start");
        startBtn.addActionListener(this);

        dialog = new Dialog();
    }

    public void CreateGUI() {
        this.frame = new JFrame("SERVER - 18127006");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setPreferredSize(new Dimension(900, 700));

        addComponentToPane(this.frame.getContentPane());

        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
    }

    void addComponentToPane(Container pane) {
        pane.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        //messageArea.setBounds(0, 0, 200,200);
        messageArea.setBorder(BorderFactory.createLineBorder(Color.red, 1));

        //Add message area
        messageArea.setEditable(false);
        messageArea.setContentType("text/html");
        doc = messageArea.getStyledDocument();
        JScrollPane p = new JScrollPane(messageArea);
        p.setPreferredSize(new Dimension(800, 500));
        c.gridx = c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(10, 10, 10, 10);
        pane.add(p, c);

        //Add button area
        c.gridy = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.ipadx = c.ipady = 30;
        c.insets = new Insets(0, 0, 30, 0);
        pane.add(startBtn, c);
    }

    void addMessage(String message) {
        try {

        } catch (Exception e) {
            dialog.showDialog(e.toString());
        }
    }

    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();

        if (cmd.equals("start")) {
            server_controller = new svNet(this.messageArea, this.doc);
            serverThread = new Thread(server_controller);
            serverThread.start();

            this.startBtn.setActionCommand("stop");
        }
        if (cmd.equals("stop")) {
            serverThread.interrupt();
            server_controller.stop();
            server_controller.closeSV();

            this.startBtn.setActionCommand("start");
        }
    }
}

class svNet implements Runnable {
    //Running
    public boolean running = false;

    //Doc
    DocumentController document;

    //Server Controller
    ServerSocket server;
    int serverPort;

    ArrayList<client_controller> clients;

    Dialog dialog = new Dialog();

    ArrayList<client> Clients;
    private static String accountPath = "resources/account.txt";

    HashMap<String, String> Account = null;

    public svNet(JTextPane textPane, StyledDocument styleDoc) {
        readAccount();
        try {
            this.running = true;
            this.server = new ServerSocket(0);
            this.serverPort = server.getLocalPort();
            this.document = new DocumentController(textPane, styleDoc);

            this.document.insertMessage("Server start on port: " + this.serverPort);
        } catch (Exception e) {
            //Error
            dialog.showDialog(e.toString());
        }
    }

    public void stop(){
        this.running = false;
    }

    void readAccount() { //Read list account
        HashMap<String, String> tempAcc = new HashMap<String, String>();
        //HashMap<String, String> tempUserGroups = new HashMap<String, String>();

        FileReader fileReader;
        BufferedReader Reader;
        File file = new File(this.accountPath);

        if (file.exists()) {
            try {
                fileReader = new FileReader(this.accountPath);
                Reader = new BufferedReader(fileReader);

                String line = null;
                while ((line = Reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        String[] splitLine = line.split(","); //username,password
                        tempAcc.put(splitLine[0], splitLine[1]);
                    }
                }

                fileReader.close();
                Reader.close();
            } catch (Exception e) {
                //Error
                dialog.showDialog(e.toString());
            }
        }

        this.Account = tempAcc;
        //this.userGroups = tempUserGroups;
    }

    void saveAccount() {
        try {
            FileWriter fr = new FileWriter(this.accountPath, false);
            BufferedWriter writer = new BufferedWriter(fr);

            this.Account.forEach((username, password) -> {
                try {
                    writer.write(username + "," + password);
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            writer.close();
        } catch(IOException e){
            //Error
            dialog.showDialog(e.toString());
        }
    }

    int getServerPort() {
        return serverPort;
    }

    void closeSV() {
        try {
            this.server.close();
        } catch (Exception e) {
            //Error
            dialog.showDialog(e.toString());
        }

    }

    public void run() {
        while (this.running) {
            try {
                Socket client_comming = server.accept();

                client_controller client = new client_controller(client_comming, this.document);
                Thread t = new Thread(client);
                t.start();

                document.insertMessage(client_comming.getLocalSocketAddress().toString());
            } catch (Exception e) {
                dialog.showDialog(e.toString());
            }
        }
    }
}

class client_controller implements Runnable, GlobalConstants{
    Socket socket;
    BufferedReader reader; //read from client
    PrintWriter writer; //write to client
    boolean running;
    Dialog dialog = new Dialog();

    DocumentController document;

    public client_controller (Socket client, DocumentController docController) {
        try {
            this.running = true;
            this.socket = client;
            this.document = docController;
            this.writer = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(client.getOutputStream())
                    ));
            this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        }catch (Exception e) {
            //Error
            dialog.showDialog(e.toString());
        }
    }

    public void run() {
        while (this.running) {
            try {
                String receiveMsg = reader.readLine();

                String[] spliMsgs = receiveMsg.split(",", 2);

                String headMsg = spliMsgs[0];
                String contentMsg = null;

                if (spliMsgs.length > 1) {
                    contentMsg = spliMsgs[1];
                }

                switch (headMsg) {
                    case CREATE_CHAT_HEAD: {
                        createChat(contentMsg);
                        break;
                    }
                    case JOIN_CHAT_HEAD: {
                        joinChat(contentMsg);
                        break;
                    }
                    case CHAT_HEAD: {
                        String[] splitMsgChat = contentMsg.split(",", 2);
                        chat(splitMsgChat[0], splitMsgChat[1]);
                        break;
                    }
                    case LEAVE_CHAT_HEAD: {

                        break;
                    }
                    case UPLOAD_HEAD: {

                        break;
                    }
                    case DOWNLOAD_REQUEST_HEAD: {

                        break;
                    }
                    case DISCONNECT_HEAD: {
                        this.running = false;

                        //Close connection
                        writer.close();
                        reader.close();
                        socket.close();
                        break;
                    }
                }
            }catch (Exception e) {
                //Error
                dialog.showDialog(e.toString());
            }
        }
    }

    void createChat(String name) {
        
    }

    void joinChat(String name) {

    }

    void chat(String groupName, String message) {

    }

    public void send(String message) {
        try {
            writer.println(message);
        }catch (Exception e) {
            //Error
            dialog.showDialog(e.toString());
        }
    }
}
