import java.net.*;
import java.io.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

class CServerThread extends Thread {
    private final Socket client1;
    private final Socket client2;
    private BufferedReader inFromClient1;
    private PrintWriter outToClient1;
    private PrintWriter outToClient2;
    private final CBattleShip battleShip;
    private final CBattleShip.CPlayer player1;
    private final CBattleShip.CPlayer player2;
    private final CyclicBarrier barrier;
    private final ReentrantLock lock;
    boolean done;


    public CServerThread(Socket socket1, Socket socket2,
                         CBattleShip battleShip, CBattleShip.CPlayer player1,
                         CBattleShip.CPlayer player2, CyclicBarrier barrier,ReentrantLock lock) {
        this.client1 = socket1;
        this.client2 = socket2;
        this.battleShip = battleShip;
        this.player1 = player1;
        this.player2 = player2;
        this.barrier = barrier;
        this.lock = lock;
    }

    public void run(){
        try{
            communicate();
        }catch (Exception e){
            e.printStackTrace(System.out);  }
    }

    public void communicate(){
        try
        {
            inFromClient1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
            outToClient2 = new PrintWriter(client2.getOutputStream(),true);
            outToClient1 = new PrintWriter(client1.getOutputStream(),true);
            //setting player name
            handleNickname();

            startGame();
            while(!done) //infinite loop to continue communication
            {
                String messageReceivedFromClient1 = inFromClient1.readLine();
                if (messageReceivedFromClient1 == null || messageReceivedFromClient1.startsWith("/quit"))
                {
                    outToClient1.println(" (=>leaving the game...)");
                    System.out.println(player1.name + " requested to leave the game  :" + messageReceivedFromClient1);
                    break;
                }
                else
                {
                    outToClient2.println(player1.name + ": " + messageReceivedFromClient1 + "\n");
                    System.out.println("Echo on server :" + messageReceivedFromClient1);
                }
            }
            outToClient1.close();
            inFromClient1.close();
            client1.close();
            System.out.println(client1 + " closed ");
            outToClient2.println(" (=>" + player1.name + " leaved the game)" + "\n");
        }
        catch (Exception e)
        {
            System.out.println("Server communicate error!");
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void handleNickname()
    {
        try
        {
            System.out.println("sending info to client for login...");
            outToClient1.println("Please enter a username before playing");
            player1.name = inFromClient1.readLine();
            System.out.println("player " + player1.name + " logged in!");
            outToClient1.println("Welcome to the battleship game " + player1.name + "!");
            if(barrier.getNumberWaiting() == 0){
                outToClient1.println("the other player is choosing his name...");
            }
            barrier.await();
        }
        catch(Exception e) {
            System.out.println("Server error on handling username!");
            System.out.println(e.getMessage());
            e.printStackTrace();
            shutdown();
        }
    }
    private void startGame()
    {
        outToClient1.println("Here are the rules of battleship: ");
        battleShip.generateRules();
        outToClient1.println("Now we can start!");
        try
        {
            shipPositioning();

            outToClient1.println("OK now we are ready to... battle!!");
            startBattle();
        }
        catch (Exception e)
        {
            System.out.println("error on waiting other threads!");
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void shipPositioning(){
        try{
            outToClient1.println("Let's insert the first ship, the little one!" +
                    " Just type the coordinates starting from the character");
            String input1 = inFromClient1.readLine();

            while (!battleShip.insertShip(player1, CBattleShip.ShipType.little, input1))
            {
                outToClient1.println("Wrong coordinates,try again!");
                input1 = inFromClient1.readLine();
            }
            outToClient1.println("Good! Now insert the second ship, the medium one!" +
                    " Just type the coordinates starting from the character and separate the 2 coordinates with a comma");
            String input2 = inFromClient1.readLine();
            while(!battleShip.insertShip(player1, CBattleShip.ShipType.medium, input2))
            {
                outToClient1.println("Wrong coordinates,try again!");
                input2 = inFromClient1.readLine();
            }
            outToClient1.println("Good! Now insert the third ship, the large one!" +
                    " Just type the coordinates starting from the character and separate the 3 coordinates with a comma");
            String input3 = inFromClient1.readLine();
            while(!battleShip.insertShip(player1, CBattleShip.ShipType.large, input3))
            {
                outToClient1.println("Wrong coordinates,try again!");
                input3 = inFromClient1.readLine();
            }
            if(barrier.getNumberWaiting() == 0)
                outToClient1.println(player2.name + " is choosing his ships location...");
            barrier.await();
        }
        catch (Exception e){
            System.out.println("ERROR on ship positioning!");
            System.out.println(e.getMessage());
            e.printStackTrace();
            shutdown();
        }
    }

    private void startBattle(){
        while (!battleShip.CheckLosingCondition(player2))
        {
            try
            {
                lock.lock();
                outToClient1.println("It's your turn!");
                displayGridSituation();
                outToClient1.println("Ok,now type the coordinates to fire!");
                CBattleShip.ShotOutcome shotOutcome;
                do {
                    String coord = inFromClient1.readLine();
                    shotOutcome = battleShip.fire(player2,coord);
                    if(shotOutcome == CBattleShip.ShotOutcome.invalidCoordinates)
                        outToClient1.println("Invalid coordinates, retry");
                    else
                        break;
                }
                while(true);

                String feedbackToPlayer1;
                String feedbackToPlayer2;
                switch (shotOutcome) {
                    case hit -> {
                        feedbackToPlayer1 = "Hit!";
                        feedbackToPlayer2 = "Your ship got hit on " + "TODO cell coordinates" + " by " + player1.name + "!";
                    }
                    case hitAndSunk -> {
                        feedbackToPlayer1 = "Hit and sunk!";
                        feedbackToPlayer2 = "You got hit and sunk on " + "TODO cell coordinates" + " by " + player1.name + "!";
                    }
                    case missed -> {
                        feedbackToPlayer1 = "Missed";
                        feedbackToPlayer2 = player1.name + " missed";
                    }
                    default -> throw new IllegalStateException("Value unmanaged: " + shotOutcome);
                }
                //sending feedbacks to the players
                outToClient1.println(feedbackToPlayer1);
                outToClient2.println(feedbackToPlayer2);
                outToClient1.println("End of turn");
            }
            catch (IOException e)
            {
                System.out.println("ERROR on reading coordinates of the shot from the player!");
                System.out.println(e.getMessage());
                e.printStackTrace();
                shutdown();
            }
            finally {
                lock.unlock();
            }
        }
        //the game is over, sending feedbacks to the players
        outToClient1.println("The battle is over, you won!");
        outToClient2.println("The battle is over, you lost! " + player1.name + " won!");
        battleShip.ended = true;
        shutdown();
    }

    private void displayGridSituation()
    {
        try{
            outToClient1.println("Do you want to display your situation?(y/n)");
            String response;
            while(true)
            {
                response = inFromClient1.readLine();
                if((response.equalsIgnoreCase("n")) || (response.equalsIgnoreCase("y")))
                    break;
                else
                    outToClient1.println("Wrong response,try again");
            }
            if(response.equalsIgnoreCase("y"))
            {
                outToClient1.println(battleShip.displayGrid(player1,false));
            }
            outToClient1.println("Do you want to display other player's situation?(y/n)");
            while(true)
            {
                response = inFromClient1.readLine();
                if((response.equalsIgnoreCase("n")) || (response.equalsIgnoreCase("y")))
                    break;
                else
                    outToClient1.println("Wrong response,try again");
            }
            if(response.equalsIgnoreCase("y"))

            {
                outToClient1.println(battleShip.displayGrid(player2,true));
            }
        }
        catch(Exception e )
        {
            System.out.println("Error on displaying grid situations");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void shutdown()
    {
        try
        {
            inFromClient1.close();
            outToClient1.close();
            if(!client1.isClosed())
            {
                client1.close();
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("server thread ERROR on shutting down client connection");
        }
    }
}

public class CServer {

    ServerSocket server;
    boolean done;                        // true if the server has to shut down

    CServerThread serverThread1 ;
    CServerThread serverThread2 ;
    public void start(){
        try{
            server = new ServerSocket(6789);
            System.out.println("Server waiting for first player... ");
            Socket socket1 = server.accept();
            System.out.println("Server socket  " + socket1);
            System.out.println("Server waiting for second player... ");
            Socket socket2 = server.accept();
            System.out.println("Server socket  " + socket2);
            System.out.println("Cannot handle other connections");
            CBattleShip battleShip = new CBattleShip();
            //creating 2 threads to handle 2 different clients
            CyclicBarrier barrier = new CyclicBarrier(2);
            ReentrantLock lock = new ReentrantLock(true);
            CBattleShip.CPlayer player1 = new CBattleShip.CPlayer();
            CBattleShip.CPlayer player2 = new CBattleShip.CPlayer();
            serverThread1 = new CServerThread(socket1,socket2,battleShip,player1,player2,barrier,lock);
            serverThread1.start();
            serverThread2 = new CServerThread(socket2,socket1,battleShip,player2,player1,barrier,lock);
            serverThread2.start();
        }
        catch (Exception e){
            System.out.println("Server acceptance error!");
            System.out.println(e.getMessage());
            e.printStackTrace();
            shutdown();
        }
    }

    public void shutdown(){
        done = true;
        try{
            if(!server.isClosed())
                server.close();
            //shutdown of all the other connections
            serverThread1.shutdown();
            serverThread2.shutdown();
        }
        catch (Exception e)
        {
            System.out.println("Server shutdown error!");
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void main (String[] args){
        CServer chatServer = new CServer();
        chatServer.start();
    }
}
