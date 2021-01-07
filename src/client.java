import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.net.*;

public class client {
    public static void main(String[]args) {
        System.out.println("Hello World");
        clientGUI x = new clientGUI();
    }
}

class clientGUI {
    //GUI define
    JFrame mainFrame;
    ArrayList<UserChatFrame> uChatFrames;
    JScrollPane mainScrollUserList;
    ArrayList<JButton> listUserChooseBtn;

    public clientGUI(){

    }

    class UserChatFrame implements ActionListener {
        JFrame chatFrame;
        JTextPane msgPane;
        JTextField inputField;
        JButton sendBtn;
        String username; //the user this client is chat with

        public UserChatFrame (String chatWithUser) {
            this.username = chatWithUser;

            this.sendBtn = new JButton("Send");
            this.sendBtn.setActionCommand(chatWithUser);

            this.inputField = new JTextField();
            this.msgPane = new JTextPane();
        }

        public void actionPerformed(ActionEvent ae) {
            System.out.println(ae.getActionCommand());
        }

        public void CreateFrame() {
            this.chatFrame = new JFrame(this.username);
            this.chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.chatFrame.setResizable(true);

            //Add component to frame
            Container pane = this.chatFrame.getContentPane();

            pane.setLayout(new GridBagLayout());

            //

            this.chatFrame.setPreferredSize(new Dimension(800, 600));
            this.chatFrame.pack();
            this.chatFrame.setVisible(true);
        }
    }

    class clientSocket {

    }

    class clientReader {

    }
}