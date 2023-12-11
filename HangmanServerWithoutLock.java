/**
 * 
 * @author Jim Farese
 * @version 12/11/2023
 * 
 * Hangman Server Class that creates a server that accepts connections from clients wanting to play a hangman game against someone else.  It uses threads to handle multiple clients 
 * 
 */


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class HangmanServerWithoutLock {

	private ServerSocket serverSocket;
	
	
	//Constructor to initialize the server socket
	public HangmanServerWithoutLock(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
		
	}
	
	//Method to start the server
	public void startServer() {
		try {
			//Tries to run the server until it is closed 
			while(!serverSocket.isClosed()) {
				
				//Accepts incoming connections
				Socket socket = serverSocket.accept();
				System.out.println("Someone has joined the game");
				HangmanGameHandlerWithoutLock gameHandler = new HangmanGameHandlerWithoutLock(socket);
				
				Thread thread = new Thread(gameHandler);
				thread.start();
				
				
			}
		} catch(IOException e) {
			
		}
		
	}
	//Method to close the server socket
	public void closeServerSocket() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
				System.out.println("Someone has left the game");
			}
		} catch(IOException e) {
			
		}
	}

	//Main method to start the HangmanServer on local port 8080
	public static void main(String args[]) throws IOException {
		ServerSocket serverSocket = new ServerSocket(8080);
		HangmanServerWithoutLock server = new HangmanServerWithoutLock(serverSocket);
		server.startServer();
		
		
		
	}
}
