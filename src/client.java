import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.net.*;

public class client {
    public static void main(String[]args) {
        System.out.println("Hello World");
        clientGUI x = new clientGUI();
        x.createMainFrame();
    }
}

interface ClientGlobalConstants {
    //Disconnect msg
    String DISCONNECT_HEAD = ":disconnect";

    //Status msg
    String SUCCESS_HEAD = ":success";
    String FAIL_HEAD = ":fail";

    //Status
    String REGISTER_HEAD = ":register";
    String LOGIN_HEAD = ":login";
    String LOGOUT_HEAD = ":logout";
    String LIST_USER_ONLINE = ":listonline";

    //Chat
    String CHAT_HEAD = ":chat";

    //File
    String FILE_SEND_HEAD = ":file"; //server send file back

    //// GUI define
    String DISCONNECT_CMD = "disconnect";
    String CONNECT_CMD = "connect";
    String LOGOUT_CMD = "logout";
    String LOGIN_CMD = "login";
    String REGISTER_CMD = "register";

    //main frame
    int MAIN_FRAME_WIDTH = 300;
    int MAIN_FRAME_HEIGHT = 800;
}

class clientGUI implements ClientGlobalConstants, ActionListener {
    ImageIcon icon = new ImageIcon("resources/images/smile.png");

    //GUI define
    JFrame mainFrame;

    //Chating List page
    Container ChatListPane;
    ArrayList<UserChatFrame> uChatFrames;
    JScrollPane chatScrollUserList;
    JPanel listBtnPane;
    ArrayList<JButton> listUserChooseBtn;
    JButton logoutBtn;

    //Login page
    Container OpeningPane;
    JTabbedPane logregTabPane;
    Container logPane;
    JTextField logName;
    JPasswordField logPsw;
    JButton logBtn;

    Container regPane;
    JTextField regName;
    JPasswordField regPsw;
    JButton regBtn;

    Container svConnectionPane;
    JTextField svIP;
    JTextField svPort;
    JButton svBtn;

    Container logregTabContainer;

    //Socket
    boolean isConnected;
    Socket client;
    PrintWriter socketWriter;
    BufferedReader socketReader;
    String myUserName;
    Thread readThread;

    //Action Event
    public void actionPerformed(ActionEvent ae) { // for user chating page
        String cmd = ae.getActionCommand();

        switch (cmd) {
            case LOGIN_CMD: {
                loginToServer();
                break;
            }
            case REGISTER_CMD: {
                registerToServer();
                break;
            }
            case CONNECT_CMD: {
                String ip = this.svIP.getText();
                String portString = this.svPort.getText();

                try {
                    ConnectToServer(ip, Integer.parseInt(portString));
                } catch (Exception e) {
                    showDialogMessage(e.toString());
                }

                break;
            }
            case DISCONNECT_CMD: {
                Disconnect();
                this.svBtn.setText("Connect");
                this.svBtn.setForeground(Color.BLACK);
                this.svBtn.setBackground(null);
                this.svBtn.setActionCommand(CONNECT_CMD);
                break;
            }
        }
    }

    void loginToServer() {
        if (this.isConnected) {
            String name = this.logName.getText();
            char[] pswChars = this.logPsw.getPassword();
            String psw = new String(pswChars);
            myUserName = name;
            sendMsgToServer(LOGIN_HEAD + "," + name + "," + psw);
        }
    }

    void registerToServer() {
        if (this.isConnected) {
            String name = this.regName.getText();
            char[] pswChars = this.regPsw.getPassword();
            String psw = new String(pswChars);

            sendMsgToServer(REGISTER_HEAD + "," + name + "," + psw);
        }
    }

    public clientGUI(){
        //Chating List page
        this.ChatListPane = new Container();
        this.uChatFrames = new ArrayList<UserChatFrame>();
        this.listUserChooseBtn = new ArrayList<JButton>();
        this.listBtnPane = new JPanel();
        this.listBtnPane.setLayout(new BoxLayout(listBtnPane, BoxLayout.Y_AXIS));
        this.chatScrollUserList = new JScrollPane(this.listBtnPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.logoutBtn = new JButton("Logout");

        //Login page
        this.OpeningPane = new Container();
        this.logregTabPane = new JTabbedPane();
        this.logPane = new Container();
        this.logName = new JTextField();
        this.logPsw = new JPasswordField();
        this.logBtn = new JButton("Login");
        this.logBtn.setActionCommand(LOGIN_CMD);
        this.logBtn.addActionListener(this);

        this.regPane = new Container();
        this.regName = new JTextField();
        this.regPsw = new JPasswordField();
        this.regBtn = new JButton("Register");
        this.regBtn.setActionCommand(REGISTER_CMD);
        this.regBtn.addActionListener(this);

        this.svConnectionPane = new Container();
        this.svIP = new JTextField();
        this.svPort = new JTextField();
        this.svBtn = new JButton("Connect");
        this.svBtn.setActionCommand(CONNECT_CMD);
        this.svBtn.addActionListener(this);

        this.logregTabContainer = new Container();

        //Socket
        this.isConnected = false;
        this.client = null;
        this.socketWriter = null;
        this.socketReader = null;
        this.myUserName = null;
    }

    void showDialogMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    void combineComponentsLoginPage() {
        OpeningPane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        //Icon
        c.gridx = c.gridy = 0;
        c.insets = new Insets(10, 10, 10, 10);
        c.weightx = c.weighty = 0.0;
        JLabel iconLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(150, 150, Image.SCALE_DEFAULT)));
        OpeningPane.add(iconLabel, c);

        //Login & register tabs
        ////Login page
        JLabel logNameLabel = new JLabel("Username");
        JLabel logPswLabel = new JLabel("Password");

        GridLayout LoginGridLayout = new GridLayout(5, 1, 5,10);
        logPane.setLayout(LoginGridLayout);

        logNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        logPane.add(logNameLabel);

        logName.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        logPane.add(logName);

        logPswLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        logPane.add(logPswLabel);

        logPsw.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        logPane.add(logPsw);

        //logBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        logPane.add(logBtn);

        ////Register page
        JLabel regNameLabel = new JLabel("Username");
        JLabel regPswLabel = new JLabel("Password");

        GridLayout reginGridLayout = new GridLayout(5, 1, 5,10);
        regPane.setLayout(reginGridLayout);

        regNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        regPane.add(regNameLabel);

        regName.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        regPane.add(regName);

        regPswLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        regPane.add(regPswLabel);

        regPsw.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        regPane.add(regPsw);

        //regBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        regPane.add(regBtn);

        logregTabPane.addTab("Login", null, logPane, "Login by registerd account");
        logregTabPane.addTab("Register", null, regPane, "Regiter a new account");
        logregTabPane.setPreferredSize(new Dimension(250, 250));
        c.gridy = 1;
        OpeningPane.add(logregTabPane, c);

        //SV pane
        JLabel svConnectLabel = new JLabel("---  Connect to Server  ---", SwingConstants.CENTER);
        JLabel svNameLabel = new JLabel("IP");
        JLabel svPortLabel = new JLabel("Port");

        svConnectionPane.setLayout(new GridLayout(6, 1, 5, 5));

        svConnectionPane.add(svConnectLabel);

        svNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        svConnectionPane.add(svNameLabel);
        svConnectionPane.add(svIP);

        svPortLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        svConnectionPane.add(svPortLabel);
        svConnectionPane.add(svPort);

        svConnectionPane.add(svBtn);
        svConnectionPane.setBounds(1, 1, 20, 20);

        c.gridy = 2;
        svConnectionPane.setPreferredSize(new Dimension(250, 250));
        OpeningPane.add(svConnectionPane, c);
    }

    void combineComponentsChatPage() {
        ChatListPane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = c.gridy = 0;
        c.insets = new Insets(10, 10, 10,  10);
        c.weightx = c.weighty = 0.0;

        ChatListPane.add(logoutBtn, c);
        logoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMsgToServer(LOGOUT_HEAD);
            }
        });

        c.gridy = 1;
        JLabel ChatListLabel = new JLabel("User online list");
        ChatListPane.add(ChatListLabel, c);

        c.gridy = 2;
        chatScrollUserList.setPreferredSize(new Dimension(MAIN_FRAME_WIDTH - 50, MAIN_FRAME_HEIGHT - 200));

        ChatListPane.add(chatScrollUserList, c);
    }

    void setLogregTabPaneEnable(boolean value) {
        this.logregTabContainer.setEnabled(value);
    }

    void showOpeningPane() {
        this.OpeningPane.setVisible(true);
    }

    void hideOpeningPane() {
        this.OpeningPane.setVisible(false);
    }

    void removeCurrentPane() {
        this.mainFrame.getContentPane().removeAll();
    }

    void addOpenningPane() {
        this.mainFrame.getContentPane().add(this.OpeningPane);
        this.mainFrame.revalidate();
        this.mainFrame.repaint();
    }

    void addChatListPane() {
        this.mainFrame.getContentPane().add(this.ChatListPane);
        this.mainFrame.revalidate();
        this.mainFrame.repaint();
    }

    void ConnectToServer(String ip, int port) {
        try {
            this.client = new Socket(ip, port);
            this.socketWriter = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(this.client.getOutputStream())
                    ), true
            );
            this.socketReader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));

            this.readThread = new Thread(new clientReader());
            this.readThread.start();

            this.isConnected = true;
            this.svBtn.setForeground(Color.WHITE);
            this.svBtn.setBackground(Color.RED);
            this.svBtn.setText("Disconnect");
            this.svBtn.setActionCommand(DISCONNECT_CMD);
        } catch (Exception e) {
            showDialogMessage(e.toString());
        }
    }

    void Disconnect () {
        this.isConnected = false;
        //Send disconnect
        sendMsgToServer(DISCONNECT_HEAD);
        //Close connection
        try {
            this.client.close();
            this.socketWriter.close();
            this.socketReader.close();
        } catch (Exception e) {
            showDialogMessage(e.toString());
        }
    }

    public void createMainFrame() {
        mainFrame = new JFrame("Main frame");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
//            @Override
//            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
//                System.exit(0);
//            }
//        });

        mainFrame.setMinimumSize(new Dimension(MAIN_FRAME_WIDTH, MAIN_FRAME_HEIGHT));
        mainFrame.setPreferredSize(new Dimension(MAIN_FRAME_WIDTH, MAIN_FRAME_HEIGHT));
        mainFrame.setResizable(false);

        //Add to main frame
        combineComponentsLoginPage();
        combineComponentsChatPage();

        addOpenningPane();
        //addChatListPane();
//        JButton btn = new JButton("Click me");
//        btn.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                Random random = new Random();
//                int randomInt = random.nextInt(100);
//
//                UserChatFrame ucf = new UserChatFrame(String.valueOf(randomInt));
//                ucf.CreateFrame();
//            }
//        });
//
//        pane.add(btn, BorderLayout.CENTER);

        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    class UserChatFrame implements ActionListener {
        JFrame chatFrame;
        JLabel chatWithLabel;

        JTextPane msgPane;
        HTMLDocument htmlDoc;
        HTMLEditorKit htmlEdit;
        JScrollPane scrollMsgPane;

        JTextArea inputField;
        JScrollPane scrollInputField;

        JButton sendBtn;
        JButton attachFile;
        String username; //the user this client is chat with

        public UserChatFrame (String chatWithUser) {
            this.username = chatWithUser;

            this.attachFile = new JButton("Attach File" );
            this.attachFile.setActionCommand("attach");

            this.sendBtn = new JButton("Send");
            this.sendBtn.setActionCommand("send");

            this.inputField = new JTextArea();
            this.scrollInputField = new JScrollPane(this.inputField);

            this.msgPane = new JTextPane();
            this.msgPane.setEditable(false);
            this.msgPane.setContentType("text/html");
            this.scrollMsgPane = new JScrollPane(this.msgPane);
            this.htmlDoc = (HTMLDocument)msgPane.getDocument();
            this.htmlEdit = (HTMLEditorKit)msgPane.getEditorKit();

            this.chatWithLabel = new JLabel(chatWithUser);

            CreateFrame();
        }

        public void actionPerformed(ActionEvent ae) {
            //System.out.println(ae.getActionCommand());
            String cmd = ae.getActionCommand();
            if (cmd.equals("send")) {
                String message = this.inputField.getText();
                addChatMessageFromMySelf(message);
                this.inputField.setText("");

                if (!message.isBlank()) {
                    sendMsgToServer(CHAT_HEAD + "," + this.username + "," + message.trim());
                }
            } else {
                if (cmd.equals("attach")) {
                    try {
                        JFileChooser jfc = new JFileChooser();
                        int ans = jfc.showOpenDialog(chatFrame);
                        if (ans == JFileChooser.APPROVE_OPTION) {
                            File file = jfc.getSelectedFile();
                            byte[] fileBytes = Files.readAllBytes(Paths.get(file.getPath()));
                            String fileString64 = Base64.getEncoder().encodeToString(fileBytes);
                            sendMsgToServer(FILE_SEND_HEAD + "," + this.username + "," + file.getName() + "," + fileString64);
                            //This is where a real application would open the file.
                            addChatMessageFromMySelf("Sent file <span style='color:red'><i>'" + file.getName() + "</i></span>");
                        }
                    }catch (Exception e) {
                        showDialogMessage(e.toString());
                    }
                }
            }
        }

        public void CreateFrame() {
            this.chatFrame = new JFrame(this.username);
            this.chatFrame.setDefaultCloseOperation(chatFrame.getDefaultCloseOperation());

            this.chatFrame.setResizable(true);
            this.chatFrame.setMinimumSize(new Dimension(800, 750));

            //Add component to frame
            Container pane = this.chatFrame.getContentPane();

            pane.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            ////Label
            c.gridx = c.gridy = 0;
            c.insets = new Insets(5, 0, 5, 0);
            c.weightx = c.weighty = 1.0;

            this.chatWithLabel.setForeground(Color.red);
            this.chatWithLabel.setFont(new Font("Arial", Font.BOLD, 22));
            pane.add(this.chatWithLabel, c);

            ////Message field
            c.gridy = 1;
            c.gridwidth = 3; //Width
            c.insets = new Insets(0, 10, 10, 10);
            this.scrollMsgPane.setPreferredSize(new Dimension(750, 450));
            pane.add(this.scrollMsgPane, c);

            ////attach File
            c.gridx = 0;
            c.gridy = 2;
            c.weightx = c.weighty = 0.0;
            this.attachFile.setPreferredSize(new Dimension(100, 40));
            this.attachFile.addActionListener(this);
            pane.add(this.attachFile, c);

            ////Input field & Send btn
            Container inputMsgPane = new Container();
            inputMsgPane.setLayout(new BorderLayout());

            this.scrollInputField.setPreferredSize(new Dimension(650, 100));
            this.scrollInputField.setBorder(BorderFactory.createLineBorder(Color.black, 2));
            inputMsgPane.add(this.scrollInputField, BorderLayout.LINE_START);

            this.sendBtn.setPreferredSize(new Dimension(100, 40));
            this.sendBtn.addActionListener(this);
            inputMsgPane.add(this.sendBtn);

            c.gridx = 0;
            c.gridy = 3;
            c.insets = new Insets(0, 0, 0, 0);
            c.weightx = c.weighty = 1.0;
            pane.add(inputMsgPane, c);
//            c.gridx = 0;
//            c.gridy = 3;
//            c.gridwidth = 2;
//            c.weightx = c.weighty = 0.8;
//            c.insets = new Insets(0, 10, 20, 10);
//            pane.add(this.inputField, c);
//
//            c.gridx = 2;
//            c.gridwidth = 1;
//            c.weightx = c.weighty = 0.0;
//            c.insets = new Insets(0, 0, 20, 10);
//            pane.add(this.sendBtn, c);
            //

            this.chatFrame.setPreferredSize(new Dimension(800, 750));
            this.chatFrame.pack();
            this.chatFrame.setVisible(true);
        }

        public String getUsername() {
            return this.username;
        }

        public void addChatMessageFromOtherUser(String message) {
            try {
                this.htmlEdit.insertHTML(this.htmlDoc, this.htmlDoc.getLength(), "<span style='color:red'><b>" + this.username + ": </b></span>" + message + "<br>", 0, 0, null);
                this.msgPane.setCaretPosition(this.htmlDoc.getLength());
            } catch (Exception e) {
                showDialogMessage(e.toString());
            }
        }

        public void addChatMessageFromMySelf(String message) {
            try {
                this.htmlEdit.insertHTML(this.htmlDoc, this.htmlDoc.getLength(), "<i><b>" + myUserName + ": </b></i>" + message + "<br>", 0, 0, null);
                this.msgPane.setCaretPosition(this.htmlDoc.getLength());
            } catch (Exception e) {
                showDialogMessage(e.toString());
            }
        }
    }

    class clientReader implements Runnable {
        public void run() {
            while (isConnected) {
                try {
                    String receiveMsg = socketReader.readLine();
                    String[] splitMsg = receiveMsg.split(",", 2);

                    String msgHead = splitMsg[0];
                    switch (msgHead) {
                        case SUCCESS_HEAD: {
                            String msgSecondHead = splitMsg[1];

                            if (msgSecondHead.equals(LOGIN_HEAD)) {
                                showDialogMessage("Login successfully");
                                loginSuccess();
                            } else {
                                if (msgSecondHead.equals(REGISTER_HEAD)) {
                                    showDialogMessage("Register successfully!!\nNow you can login and chat with other user :D");
                                } else {
                                    if (msgSecondHead.equals(LOGOUT_HEAD)) {
                                        logout_AfterConfirmFromServer();
                                    }
                                }
                            }
                            break;
                        }
                        case FAIL_HEAD: {
                            String msgSecondHead = splitMsg[1];

                            if (msgSecondHead.equals(LOGIN_HEAD)) {
                                showDialogMessage("Username or Password is incorrect!!\nOr the account is not exist!!");
                            } else {
                                if (msgSecondHead.equals(REGISTER_HEAD)) {
                                    showDialogMessage("Register Failed!!\nUsername has been used,\nPlease choose other name.");
                                }
                            }
                            break;
                        }
                        case LIST_USER_ONLINE: {
                            updateListUserOnline(splitMsg[1]);
                            break;
                        }
                        case CHAT_HEAD: {
                            String[] splitMsgContent = splitMsg[1].split(",", 2);
                            String name = splitMsgContent[0];
                            String message = splitMsgContent[1];
                            showChat(name, message);
                            break;
                        }
                        case FILE_SEND_HEAD: {
                            String[] splitFileMsg = splitMsg[1].split(",", 3);
                            String userSent = splitFileMsg[0];
                            String fileName = splitFileMsg[1];
                            String fileContentBase64 = splitFileMsg[2];
                            askAndSaveFile(userSent, fileName, fileContentBase64);
                            break;
                        }
                    }
                } catch (Exception e) {
                    showDialogMessage(e.toString());
                    isConnected = false;
                }
            }
        }
    }

    void updateListUserOnline(String namesListWithComma) {
        String[] names = namesListWithComma.split(",");

        this.listUserChooseBtn.clear();
        this.listBtnPane.removeAll();

        for (String n : names) {
            if (!n.isBlank()) {
                JButton btn = createChatBtnWithUser(n);
                this.listUserChooseBtn.add(btn);
                this.listBtnPane.add(btn);
            }
        }

        this.listBtnPane.revalidate();
        this.listBtnPane.repaint();
    }

    void askAndSaveFile(String usersent, String filename, String fileContentBase64) {
        try {
            UserChatFrame ucf = null;
            for (UserChatFrame ucfEntry: this.uChatFrames) {
                if (ucfEntry.getUsername().equals(usersent)) {
                    ucf = ucfEntry;
                    break;
                }
            }

            if (ucf!=null) {
                ucf.addChatMessageFromOtherUser("Sent file <span style='color:red'><i>'" + filename + "</i></span>");
                byte[] fileBytes = Base64.getDecoder().decode(fileContentBase64);

                int ans = JOptionPane.showConfirmDialog(mainFrame, usersent + " has sent a file: " + filename + "\nSize: " + fileBytes.length / 1024.0 + " KB\nSave it?");

                if (ans == JOptionPane.YES_OPTION) {
                    JFileChooser jfc = new JFileChooser();
                    jfc.setSelectedFile(new File(filename));
                    int returnValSaveFile = jfc.showSaveDialog(ucf.chatFrame);
                    if (returnValSaveFile == JFileChooser.APPROVE_OPTION) {
                        File file = jfc.getSelectedFile();

                        FileOutputStream os = new FileOutputStream(file.getAbsolutePath(), false);

                        os.write(fileBytes, 0, fileBytes.length);
                        os.close();
                    }
                }
            }
        }catch (Exception e) {
            showDialogMessage(e.toString());
        }
    }

    void showChat(String user, String message) {
        boolean isUserChatFrameExist = false;

        for (UserChatFrame ucf : this.uChatFrames) {
            if (ucf.getUsername().equals(user)) {
                isUserChatFrameExist = true;
                ucf.addChatMessageFromOtherUser(message);
            }
        }

        if (!isUserChatFrameExist) {
            UserChatFrame ucf = new UserChatFrame(user);
            ucf.addChatMessageFromOtherUser(message);
            this.uChatFrames.add(ucf);
        }
    }

    void sendMsgToServer(String message) {
        this.socketWriter.println(message);
    }

    void logout_AfterConfirmFromServer() {
        removeCurrentPane();
        addOpenningPane();
        myUserName = null;
    }

    JButton createChatBtnWithUser(String username) {
        JButton btn = new JButton(username);
        btn.setActionCommand(username);
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = e.getActionCommand();

                if (!isUserChatFrameExist(username)) {
                    UserChatFrame ucf = new UserChatFrame(username);
                    uChatFrames.add(ucf);
                }
            }
        });

        return btn;
    }

    void loginSuccess() {
        removeCurrentPane();
        addChatListPane();
        //addOpenningPane();
    }

    boolean isUserChatFrameExist(String name) {
        for (UserChatFrame ucf : uChatFrames) {
            if (ucf.getUsername().equals(name)) {
                ucf.chatFrame.revalidate();
                ucf.chatFrame.repaint();
                ucf.chatFrame.pack();
                ucf.chatFrame.setVisible(true);
                return true;
            }
        }
        return false;
    }
}