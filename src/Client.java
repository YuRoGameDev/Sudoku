import java.io.*;
import java.net.*;

public class Client {

    public static void main(String[] args) {
        InetAddress address = null;

        try {
            if(args.length < 2){
                address = InetAddress.getLocalHost();
            }else{
                address = InetAddress.getByName(args[1]);
            }
           
            System.out.println("Current Address: " + address);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        Socket s = null;
        String inputLine = null;
        BufferedReader bR = null;
        BufferedReader iS = null;
        PrintWriter pW = null;

        
        try {         
            s = new Socket(address, Integer.parseInt(args[0]));
            bR = new BufferedReader(new InputStreamReader(System.in));
            iS = new BufferedReader(new InputStreamReader(s.getInputStream()));
            pW = new PrintWriter(s.getOutputStream(), true);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        String response = null;

        try {
            System.out.println("Multiplayer Sudoku!");
            System.out.println("show - Shows the current board");
            System.out.println("update [row (0-8, column (0-8), number (0-9))] - Update the board");
            System.out.println("quit - Leave the game");

            inputLine = bR.readLine();
            while (inputLine.compareTo("quit") != 0) {
                pW.println(inputLine);
                response = iS.readLine();
                if (response.contains("Game Finished")) {
                    System.out.println(response);
                    break;
                }
                while (!response.equals("")) {
                    System.out.println(response);
                    response = iS.readLine();
                }
                response = iS.readLine();
                System.out.println("Awaiting Server input");
                inputLine = bR.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Socket read Error");
        } finally {
            try {
                iS.close();
                pW.close();
                bR.close();
                s.close();
                System.out.println("Connection Closed");
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }

        }
    }

}
