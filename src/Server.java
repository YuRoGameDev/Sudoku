import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
//import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    static List<ServerThread> totalPlayers;
    static int nextID = 1;
    static String winnerID = "";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Missing Port Number");
            System.exit(1);
        }
        Socket socket;
        ServerThread servThread = null;

        Sudoku sudokuGame = new Sudoku();
        sudokuGame.fillValues();

        totalPlayers = new ArrayList<>();

        try (
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));) {
            while (true) {
                try {
                    //Called when new client connects
                    socket = serverSocket.accept();
                    servThread = new ServerThread(socket, sudokuGame);
                    totalPlayers.add(servThread);
                    servThread.start();
                    System.out.println(totalPlayers.size() + " Player(s) connected");
                } catch (Exception e) {
                    System.out.println("Connection Error: ");
                    e.printStackTrace();
                }
                if (sudokuGame.isBoardFull()) {
                    //When the board is full
                    GameEnd();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + args[0] + " or listening for a connection");
            System.out.println(e.getMessage());
        }

    }

    //When the board is full
    public static void GameEnd() {
    
        int id = -1, points = -1;
        //Display results
        for (ServerThread player : totalPlayers) {

            int[] stats = player.returnPlayerStats();
            if (id == -1) {
                id = stats[0];
                points = stats[1];
            } else {
                if (points < stats[1]) {
                    id = stats[0];
                    points = stats[1];
                }
            }

            player.pW.println("Game Over! Player " + stats[0] + " has " + stats[1] + " points");
        }

        //Display winner
        for (ServerThread player : totalPlayers) {
            player.pW.println("Winner: Player " + id +" with " + points + " points");
        }
        
    }

    //Closes the server if all players leave
    public static void CloseGame() {
        if(totalPlayers.size() == 0){
            System.out.println("All players have left.");
            System.exit(1);
        } 
    }

    static class ServerThread extends Thread {
        String inputLine;
        BufferedReader bR;
        PrintWriter pW;
        Socket s;
        Sudoku sudoku;

        final int id;
        int points;

        public ServerThread(Socket socket, Sudoku sudoku) {
            this.id = getNextId();
            this.points = 0;
            this.s = socket;
            this.sudoku = sudoku;
        }

        public synchronized static int getNextId() {
            return nextID++;
        }

        @Override
        public void run() {
            try {
                bR = new BufferedReader(new InputStreamReader(s.getInputStream()));
                pW = new PrintWriter(s.getOutputStream(), true);
            } catch (Exception e) {
                System.out.println("Error: " + e);
                return;
            }

            try {
                inputLine = bR.readLine();
                while (!sudoku.isBoardFull()) {
                    System.out.println("Sudoku board full? " + sudoku.isBoardFull());
                    String[] inputs = inputLine.split(" ");
                    System.out.println(inputs[0]);

                    if (inputs.length != 4 && inputs.length != 1) {
                        pW.println("Invalid Input Length!\n" + sudoku.getSudokuString());
                    } else if (inputs[0].toLowerCase().equals("show")) {
                        pW.println(sudoku.getSudokuString());
                    } else if (inputs[0].toLowerCase().equals("update")) {
                        int[] nums = new int[3];
                        for (int i = 1; i < inputs.length; i++) {
                            try {
                                nums[i - 1] = Integer.parseInt(inputs[i]);
                            } catch (NumberFormatException e) {
                                pW.println("Invalid Input!\n" + sudoku.getSudokuString());
                            }
                        }

                        if (!sudoku.enterNumber(nums[0], nums[1], nums[2])) {
                            System.out.println("Numbers");
                            System.out.println(nums[0] + " " + nums[1] + " " + nums[2]);
                            pW.println("Invalid Placement!\n" + sudoku.getSudokuString());

                        } else {
                            points++;
                            System.out.println(sudoku.isBoardFull());
                            if (sudoku.isBoardFull()) {
                                synchronized (winnerID) {
                                    if (!winnerID.isEmpty()) {
                                        break;
                                    }
                                    winnerID = "Player" + id;
                                }

                                break;
                            }
                            pW.println("Successful input!\n" + sudoku.getSudokuString());

                        }
                    } else {
                        pW.println("Invalid Input!\n" + sudoku.getSudokuString());
                    }

                    System.out.println("Awaiting User Input");
                    inputLine = bR.readLine();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e);
            } finally {

                //Keeps track when a player quits
                synchronized (totalPlayers) {
                    totalPlayers.remove(this);
                    System.out.println(totalPlayers.size() + " Player(s) connected");
                    CloseGame();
                }

                GameEnd();

                try {
                    System.out.println("Connection Closing..");
                    if (bR != null) {
                        bR.close();
                        System.out.println("Socket Input Stream Closed");
                    }

                    if (pW != null) {
                        pW.close();
                        System.out.println("Socket Out Closed");
                    }
                    if (s != null) {
                        s.close();
                        System.out.println("Socket Closed");
                    }

                } catch (IOException ie) {
                    System.out.println("Socket Close Error");
                }
            }
        }

        public int[] returnPlayerStats()
        {
            int[] stats = {id,points};
            return stats;
        }


    }
}
