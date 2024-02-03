import java.io.*;
import java.net.*;

public class CClient {
    String serverAddress = "localhost";
    int serverPort = 6789;
    Socket mySocket;
    String messageFromUser;                             // string that has to sent as message
    String messageReceived;                         // message received from server by the other client
    PrintWriter outToServer;
    BufferedReader inFromServer;
    BufferedReader keyboard;
    boolean done;                   // true if the client has to be disconnected and shut down





    public void connect() {
        System.out.println("CLIENT started ...");
        try {
            mySocket = new Socket(serverAddress, serverPort);
            outToServer = new PrintWriter(mySocket.getOutputStream(), true);
            inFromServer = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

        } catch (UnknownHostException e) {
            System.err.println("Host unknown");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Connection ERROR!");
            shutdown();
        }
    }

    public void communicate() {
        try
        {
            CClientThread inputHandler = new CClientThread();
            inputHandler.start();

            while ((messageReceived = inFromServer.readLine()) != null)
            {
                System.out.println(messageReceived);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Error communicating with the server!");
            shutdown();
        }
    }
    public void shutdown() {
        done = true;
        try {
            inFromServer.close();
            outToServer.close();
            if (!mySocket.isClosed())
            {
                mySocket.close();
                System.out.println("CLIENT: disconnected");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Error on shutting down");
            System.exit(1);
        }

    }

    public void start() {
        connect();
        communicate();
    }

    public static void main(String[] args) {
        CClient client = new CClient();
        client.start();
    }

    class CClientThread extends Thread {
        @Override
        public void run() {
            try {
                keyboard = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    messageFromUser = keyboard.readLine();
                    if (messageFromUser.startsWith("/quit")) {
                        outToServer.println(messageFromUser);
                        keyboard.close();
                        System.out.println("CLIENT: stop communicating and closing connection");
                        shutdown();
                    } else {
                        outToServer.println(messageFromUser);
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                System.out.println("Error on input from keyboard");
                shutdown();
            }
        }
    }
}








