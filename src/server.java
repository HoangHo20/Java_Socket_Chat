import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
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

    }
}

class svGUI implements ActionListener {
    JTextPane messageArea;
    JButton startBtn;

    public svGUI() {
        messageArea = new JTextPane();
        startBtn = new JButton("Start Server >");
        startBtn.setActionCommand("cmdRunSV");
        startBtn.addActionListener(this);
    }

    public void actionPerformed(ActionEvent ae) {

    }
}

class svNet {;
    //Server Controller
    ServerSocket server;
    ArrayList<client> Clients;
}

class client_controller implements Runnable, GlobalConstants{
    Socket socket;
    BufferedReader reader; //read from client
    PrintWriter writer; //write to client
    boolean running;

    public client_controller (Socket client) {
        try {
            this.running = true;
            this.socket = client;
            this.writer = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(client.getOutputStream())
                    ));
            this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        }catch (Exception e) {
            //Error
            System.out.println(e);
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
                System.out.println(e);
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
            System.out.println(e);
        }
    }
}

class database_controller {

}