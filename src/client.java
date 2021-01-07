import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.net.*;

public class client {
    public static void main(String[]args) {
        System.out.println("Hello World");
        clientGUI x = new clientGUI();
        x.createMainFrame();
    }
}

class clientGUI {
    //GUI define
    JFrame mainFrame;
    ArrayList<UserChatFrame> uChatFrames;
    JScrollPane mainScrollUserList;
    ArrayList<JButton> listUserChooseBtn;

    //Login page
    

    public clientGUI(){

    }

    public void createMainFrame() {
        mainFrame = new JFrame("Main frame");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setPreferredSize(new Dimension(300, 800));
        mainFrame.setResizable(true);

        //Add component to pane
        Container pane = mainFrame.getContentPane();



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
        JScrollPane scrollMsgPane;

        JTextArea inputField;
        JScrollPane scrollInputField;

        JButton sendBtn;
        JButton attachFile;
        String username; //the user this client is chat with

        public UserChatFrame (String chatWithUser) {
            this.username = chatWithUser;

            this.attachFile = new JButton("Attach File" );

            this.sendBtn = new JButton("Send");
            this.sendBtn.setActionCommand(chatWithUser);

            this.inputField = new JTextArea();
            this.scrollInputField = new JScrollPane(this.inputField);

            this.msgPane = new JTextPane();
            this.scrollMsgPane = new JScrollPane(this.msgPane);

            this.chatWithLabel = new JLabel(chatWithUser);
        }

        public void actionPerformed(ActionEvent ae) {
            System.out.println(ae.getActionCommand());
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
            pane.add(this.attachFile, c);

            ////Input field & Send btn
            Container inputMsgPane = new Container();
            inputMsgPane.setLayout(new BorderLayout());

            this.scrollInputField.setPreferredSize(new Dimension(650, 100));
            this.scrollInputField.setBorder(BorderFactory.createLineBorder(Color.black, 2));
            inputMsgPane.add(this.scrollInputField, BorderLayout.LINE_START);

            this.sendBtn.setPreferredSize(new Dimension(100, 40));
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
    }

    class clientSocket {

    }

    class clientReader {

    }
}