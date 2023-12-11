/**
 * 
 * @author Jim Farese
 * @version 12/11/2023
 * 
 * Hangman Client Class that creates an interface for a player to connect to a local host server 8080 and allow the player to communicate with the server to participate in a game
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class HangmanClientWithoutLock {
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String player;
	
	//Constructors to initialize the client 
	public HangmanClientWithoutLock(Socket socket, String player) {
		try {
			this.socket = socket;
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.player = player;
			
		}catch(IOException e) {
			closeGame(socket, bufferedReader, bufferedWriter);
		}
	}
	
	//Method to send messages to the server
	public void sendMessage() {
		try {
			//Sends player's name to the server
			bufferedWriter.write(player);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			//Takes input from player and sends message to server
			Scanner scanner = new Scanner(System.in);
			while(socket.isConnected()) {
				
				String messageToSend = scanner.nextLine();
				bufferedWriter.write(player + ": " + messageToSend);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
			
		} catch(IOException e) {
			closeGame(socket, bufferedReader, bufferedWriter);
		}
	}
	
	//Method to listen for a guess or message 
	public void ListenForGuess() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String guessFromChat;
				
				while(socket.isConnected()) {
					try {
						guessFromChat = bufferedReader.readLine();
						System.out.println(guessFromChat);
						} catch(IOException e) {
							closeGame(socket, bufferedReader, bufferedWriter);
						}
				}
			}
			
		}).start();
	}
	
	//Method to close the game 
	public void closeGame(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		try {
			if(bufferedReader != null) {
				bufferedReader.close();
			}
			
			if(bufferedWriter != null) {
				bufferedWriter.close();
			}
			
			if(socket != null) {
				socket.close();
			}
		} catch(IOException e) {
			
		}
		
	}
	
	//Main method to connect to the server at local host 8080 and allow to send messages to the server
	public static void main(String[] args)throws IOException {
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("Welcome to the Hangman Server");
		System.out.println("Enter your player name");
		String player = scanner.nextLine();
		Socket socket = new Socket("Localhost", 8080);
		HangmanClientWithoutLock hangmanPlayer = new HangmanClientWithoutLock(socket, player);
		hangmanPlayer.ListenForGuess();
		hangmanPlayer.sendMessage();
		
		
		
	}


}