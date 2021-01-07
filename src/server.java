import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
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

     //Status
    String REGISTER_HEAD = ":register";
    String LOGIN_HEAD = ":login";
    String LOGOUT_HEAD = ":logout";

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

    //HTML
    HTMLDocument htmlDoc;
    HTMLEditorKit htmlEdit;

    public DocumentController (JTextPane textPane, StyledDocument styleDoc) {
        this.messagePane = textPane;
        this.doc = styleDoc;
        this.htmlDoc = (HTMLDocument)messagePane.getDocument();
        this.htmlEdit = (HTMLEditorKit)messagePane.getEditorKit();
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

    public void insertMessageHTMLNewLine(String message) {
        try {
            this.htmlEdit.insertHTML(this.htmlDoc, this.htmlDoc.getLength(), message + "<br>", 0, 0, null);
            this.messagePane.setCaretPosition(this.htmlDoc.getLength());
        } catch (Exception e) {
            dialog.showDialog(e.toString());
        }
    }

    public void insertMessageHTMLCurrentLine(String message) {
        try {
            this.htmlEdit.insertHTML(this.htmlDoc, this.htmlDoc.getLength(), message, 0, 0, null);
            this.messagePane.setCaretPosition(this.htmlDoc.getLength());
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

    //message area controller
    DocumentController document;

    //Server
    svNet server_controller;
    Thread serverThread;

    public svGUI() {
        messageArea = new JTextPane();

        startBtn = new JButton("Start Server >");
        startBtn.setActionCommand("start");
        startBtn.setFont(new Font("Arial", Font.BOLD, 22));
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
        this.document = new DocumentController(this.messageArea, this.doc);
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
            this.startBtn.setForeground(Color.WHITE);
            this.startBtn.setBackground(Color.RED);
            this.startBtn.setText("Stop");

            this.document.insertMessageHTMLNewLine("<span style='color:red'><b>SERVER IS NOW ONLINE</b></span>");
        }
        if (cmd.equals("stop")) {
            server_controller.stop();
            serverThread.interrupt();
            server_controller.closeSV();

            this.startBtn.setForeground(Color.BLACK);
            this.startBtn.setBackground(null);
            this.startBtn.setText("Start Server >");
            this.startBtn.setActionCommand("start");

            this.document.insertMessageHTMLNewLine("<span style='color:red'><b>SERVER HAS GONE OFFLINE</b></span>");
            this.server_controller.saveAccount(); //Save account list


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
    private static String accountPath = "resources/account.txt";

    HashMap<String, String> Account = null;

    public svNet(JTextPane textPane, StyledDocument styleDoc) {
        readAccount(); //load user file
        try {
            this.running = true;
            this.server = new ServerSocket(0);
            this.serverPort = server.getLocalPort();
            this.document = new DocumentController(textPane, styleDoc);
            this.clients = new ArrayList<client_controller>();

            this.document.insertMessageHTMLNewLine("Server start on port: <span style='color:red'><b>" + this.serverPort + "</b></span>");
        } catch (Exception e) {
            //Error
            dialog.showDialog(e.toString());
        }
    }

    public void stop(){
        for (client_controller c : clients) {
            c.close_connection();
        }

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
                Socket clientcomming = server.accept();
                System.out.print(clientcomming.getLocalAddress() + " - " + clientcomming.getRemoteSocketAddress());

                client_controller c_client = new client_controller(clientcomming, this.document, this.clients, this.Account);

                this.clients.add(c_client);
                Thread t = new Thread(c_client);
                t.start();

                document.insertMessageHTMLNewLine("<span><b>" + clientcomming.getRemoteSocketAddress().toString() +"<b></span> Has join the server");
            } catch (Exception e) {
                dialog.showDialog(e.toString());
            }
        }
    }
}

class client_controller implements Runnable, GlobalConstants{
    //Socket handler
    Socket socket;
    BufferedReader reader; //read from client
    PrintWriter writer; //write to client
    String username;

    //GUI handler
    boolean running;
    Dialog dialog = new Dialog();
    DocumentController document;

    //Control itself and others (references)
    ArrayList<client_controller>  clients;
    HashMap<String, String> Account;

    public client_controller (Socket client, DocumentController docController, ArrayList<client_controller> c_client,HashMap<String, String> ListAccount) {
        try {
            this.username = null;
            this.running = true;
            this.socket = client;
            this.document = docController;
            this.clients = c_client;
            this.Account = ListAccount;
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
                System.out.println("Listen reader");
                String receiveMsg = reader.readLine();
                //String receiveMsg = (new Scanner(socket.getInputStream())).nextLine();
                //System.out.println(receiveMsg);

                String[] spliMsgs = receiveMsg.split(",", 2);

                String headMsg = spliMsgs[0];
                String contentMsg = null;

                if (spliMsgs.length > 1) {
                    contentMsg = spliMsgs[1];
                }

                if (!headMsg.equals(FILE_SEND_HEAD)) {
                    System.out.println(receiveMsg);
                }

                switch (headMsg) {
                    case REGISTER_HEAD: {
                        String[] splitMsg = contentMsg.split(",");
                        if (spliMsgs.length > 2) {
                            send(FAIL_HEAD + "," + REGISTER_HEAD);
                        } else {
                            clientRegister(splitMsg[0], splitMsg[1]);
                        }
                        break;
                    }
                    case LOGIN_HEAD: {
                        String[] splitMsg = contentMsg.split(",", 2);
                        clientLogin(splitMsg[0], splitMsg[1]);
                        break;
                    }
                    case LOGOUT_HEAD: {
                        clientLogout();
                        break;
                    }
                    case CREATE_CHAT_HEAD: {
                        createChat(contentMsg);
                        break;
                    }
                    case CHAT_HEAD: {
                        String[] splitMsgChat = contentMsg.split(",", 2);
                        chat(splitMsgChat[0], splitMsgChat[1]);
                        break;
                    }
                    case FILE_SEND_HEAD: {
                        String[] splitMsgFile = contentMsg.split(",", 2);
                        fileRouter(splitMsgFile[0], splitMsgFile[1]);
                        break;
                    }
                    case DISCONNECT_HEAD: {
                        this.running = false;

                        //Close connection
                        close_connection();
                        break;
                    }
                }
            }catch (Exception e) {
                //Error
                dialog.showDialog(e.toString());
                this.running = false;
            }
        }
    }

    boolean isUserNameExist(String username) {
        for (Map.Entry<String, String> entry : this.Account.entrySet()) {
            String name = entry.getKey();

            if (name.equals(username)) {
                return true;
            }
        }

        return false;
    }

    void clientRegister(String username, String password) {

        if (isUserNameExist(username)) {
            send(FAIL_HEAD + "," + REGISTER_HEAD); //send back status
        } else {
            this.Account.put(username, password);
            send(SUCCESS_HEAD + "," + REGISTER_HEAD); //send back status
            document.insertMessageHTMLNewLine("A username: <span style:'color:red'><b>" + username + "</b></span> has registered from <b>" + this.getIPLocalNameString() + "</b>");
        }
    }

    boolean isCorrectPassword(String username, String password) {
        for (Map.Entry<String, String> entry : this.Account.entrySet()) {
            String name = entry.getKey();

            if (name.equals(username)) {
                String psw = entry.getValue();
                return psw.equals(password);
            }
        }

        return false;
    }

    void clientLogin(String usernameLogin, String password) {
        if (isCorrectPassword(usernameLogin, password)) {
            this.username = usernameLogin;
            send(SUCCESS_HEAD + "," + LOGIN_HEAD);
            //Send list of user
        } else {
            send(FAIL_HEAD + "," + LOGIN_HEAD);
        }
    }

    void clientLogout() {
        this.username = null;
    }

    void createChat(String name) {
        for (client_controller c : clients) {
            if (c == this) {
                dialog.showDialog("Bằng nhau \n" + c.getIPLocalNameString() + " == " + this.getIPLocalNameString());
            }
        }
    }

    void joinChat(String name) {

    }

    void chat(String name, String message) {
        for (client_controller c : clients) {
            String cUsername = c.getUsername();
            if (!cUsername.isEmpty()) {
                if (name.equals(cUsername)) {
                    c.send(CHAT_HEAD + "," + this.username + "," + message);
                }
            }
        }
    }

    void fileRouter(String name, String file) {
        System.out.println("file from: " + name);
        for (client_controller c : clients) {
            String cUsername = c.getUsername();
            if (c.username != null && !cUsername.isEmpty()) {
                if (name.equals(cUsername)) {
                    c.send(FILE_SEND_HEAD + "," + this.username + "," + file);
                }
            }
        }
    }

    public void send(String message) {
        try {
            //System.out.println(message);
            writer.println(message);
            writer.flush();
//            PrintStream p = new PrintStream(socket.getOutputStream());
//            p.println(message);
        }catch (Exception e) {
            //Error
            dialog.showDialog(e.toString());
        }
    }

    public String getUserList() {
        return null;
    }

    public String getIPLocalNameString() {
        return this.socket.getLocalSocketAddress().toString();
    }

    public String getUsername() {
        return this.username;
    }

    public void close_connection() {
        try {
            this.document.insertMessageHTMLNewLine("<b>" + this.getIPLocalNameString() + "</b> DISCONNECTED!!");
            username = null;
            writer.close();
            reader.close();
            socket.close();
        } catch (Exception e) {
            //Error
            dialog.showDialog(e.toString());
        }
    }
}
