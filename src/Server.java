import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    static List<ServerThread> players;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer 8000");
            System.exit(1);
        }
        Socket s;
        ServerThread sThread = null;

        int portNumber = 8000;
        Sudoku sudokuGame = new Sudoku();
        sudokuGame.fillValues();

        players = new ArrayList<>();

        try (
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));) {
            while (true) {
                try {

                    s = serverSocket.accept();
                    sThread = new ServerThread(s, sudokuGame);
                    players.add(sThread);
                    System.out.println(players.size() + " Players connected");
                    sThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Connection Error");
                }
                if (sudokuGame.isBoardFull()) {
                    announceWinner(players);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }

    }

    public static void announceWinner(List<ServerThread> players) {
        ServerThread winner = Collections.max(players, Comparator.comparingInt(ServerThread::getPoints));

        for (ServerThread player : players) {
            player.pW.println("Game Finished! Winner: Player " + winner.getID());
        }
    }

    static int nextID = 1;
    static String winnerID = "";

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
            }

            try {
                inputLine = bR.readLine();

                while (inputLine.compareTo("EXIT") != 0 && !sudoku.isBoardFull()) {
                    System.out.println(sudoku.isBoardFull());
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
                            pW.println("Can't put that number there bud.\n" + sudoku.getSudokuString());

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

                announceWinner(players);
                players.remove(id);

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

        public int getID() {
            return id;
        }

        public int getPoints() {
            return points;
        }

    }
}
