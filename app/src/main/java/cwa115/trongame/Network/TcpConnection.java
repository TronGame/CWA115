package cwa115.trongame.Network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Basic (blocking) TCP networking functionality.
 * @todo error handling
 */
public class TcpConnection {
    private Socket clientSocket;
    private BufferedWriter outStream;
    private BufferedReader inStream;

    public TcpConnection(String address, int port) {
        try {
            clientSocket = new Socket(address, port);
            outStream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch(Exception e) {
            System.out.println("Error" + e.toString());
            // ...
        }
    }

    public void waitForConnection()
    {
        try {
            while (!clientSocket.isConnected()) {
                synchronized (this) {
                    this.wait(100);
                }

            }
        } catch (Exception e) {
            System.out.println("Error" + e.getMessage());

        }
    }

    public String readLine() {
        try {
            return inStream.readLine();
        } catch(Exception e) {
            // ...
            return "Error";
        }
    }

    public void sendMessage(String str) {
        try {
            outStream.write(str);
            outStream.flush();
        } catch(Exception e) {
            System.out.println("Error" + e.getMessage());
        }
    }



}

