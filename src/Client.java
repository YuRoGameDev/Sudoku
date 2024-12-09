import java.io.*;
import java.net.*;

public class Client {

    public static void main(String[] args) {
        InetAddress address = null;

        try {
            address = InetAddress.getByName(args[0]);
            System.out.println("Got Address" + address);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        Socket s = null;
        String inputLine = null;
        BufferedReader bR = null;
        BufferedReader iS = null;
        PrintWriter pW = null;

        
        try {
            System.out.println("D");
            s = new Socket(address, Integer.parseInt(args[1]));
            System.out.println("A");
            bR = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("B");
            iS = new BufferedReader(new InputStreamReader(s.getInputStream()));
            System.out.println("C");
            pW = new PrintWriter(s.getOutputStream(), true);
            System.out.println("D");
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        String response = null;

        try {
            System.out.println("Welcome to the game!\n"
                    + "Commands: update [row, col, num], show\n"
                    + "Update Input Format: update Row (0-8), Column (0-8), Number (0-9)\n"
                    + "Example: update 3 4 5\n"
                    + "To start, enter show");
            inputLine = bR.readLine();
            while (inputLine.compareTo("QUIT") != 0) {
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
                // TODO: handle exception
                System.out.println("Error: " + e);
            }

        }
    }

}
