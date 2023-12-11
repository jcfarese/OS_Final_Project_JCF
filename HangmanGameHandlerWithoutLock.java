/**
 * 
 * @author Jim Farese
 * @version 12/11/2023
 * 
 * Hangman Game Handler Class that creates an array to manage player connections, creates games for players, chooses a word from a dictionary, and allows communication between the players playing the game.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.io.*;
//import java.util.concurrent.locks.ReentrantLock;


public class HangmanGameHandlerWithoutLock implements Runnable{
	
	//Lists that are used to manage people playing a game and waiting in a lobby
	public static ArrayList<HangmanGameHandlerWithoutLock> hangmanGameHandler = new ArrayList(); 
	public static ArrayList<HangmanGameHandlerWithoutLock> waitingLobby = new ArrayList();
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private PrintWriter printWriter;
	private String playersName;
	private List<String> dictionary; 
	private String wordToGuess;
	private StringBuilder currentWordState;
	private int attemptsRemaining;
	private Set<String> guessedLetters;
	//private static ReentrantLock lobbyLock = new ReentrantLock();
	//private static ReentrantLock gameLock = new ReentrantLock();
	private static int waitingLobbySize = 2;
	private boolean isGameOver = false;

	//Constructor to create an instance of the game and allow players to talk to eachother
	public HangmanGameHandlerWithoutLock(Socket socket) {
	
		//creates a dictionary for the game to choose a word from
		this.dictionary = Arrays.asList("algorithm", "application", "binary", "buffer", "cache", "computer", "database", "encryption", "firewall", "hardware", "hardware", "interface", "kernel", "linux", "mainframe", "memory", "microcomputer", "network", "operatingsystem", "password", "printer", "program", "server", "supercomputer", "table", "thread", "virtualmemory", "windows");
		this.wordToGuess = selectRandomWord();
		this.currentWordState = new StringBuilder();
		this.attemptsRemaining = 6;
		this.guessedLetters = new HashSet<>();
		initializeCurrentWordState();
		
		//tries to setup input and output for a player and gets their name to broadcast to the entire server
		try {
			this.socket = socket;
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.printWriter = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()), true);
			this.playersName = bufferedReader.readLine();
			hangmanGameHandler.add(this);
			broadcastMessage(playersName + " has joined the game");
			int players = hangmanGameHandler.size();
			if(players == 1) {
				broadcastMessage("You are the only player playing the game");
			}else {
				broadcastMessage("There are " + players + " playing the game");
			}
		} catch(IOException e) {
			closeGame(socket, bufferedReader, bufferedWriter);
		}
		
		
	}
	
	@Override
	public void run() {
			// TODO Auto-generated method stub
		try {
			while(socket.isConnected()) {
				if(hangmanGameHandler.size()>=2) {
					startHangmanGame();
			}
			
			Thread.sleep(1000);
				
			}
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//Adds a player to the waiting lobby
	public static void addToWaitingLobby(HangmanGameHandlerWithoutLock handler) {
		//lobbyLock.lock();
			try {
				waitingLobby.add(handler);
				
				if(waitingLobby.size()>= waitingLobbySize) {
					movePlayersToGameHandler();
				}
				
				
			}finally {
					//lobbyLock.unlock();
			}
	}
	
	//Moves a player from the waiting lobby to the game handler lobby
	private static void movePlayersToGameHandler() {
		//gameLock.lock();
		//lobbyLock.lock();
		try {
			for(int i=0; i < waitingLobbySize; i++) {
				HangmanGameHandlerWithoutLock player = waitingLobby.remove(0);
				hangmanGameHandler.add(player);
				System.out.println(player.playersName + " has joined the game.");
			}
		}finally {
			//lobbyLock.unlock();
			//gameLock.unlock();
		}
	}

	//Sends messages to all the players
	public void broadcastMessage(String message) {
		for(HangmanGameHandlerWithoutLock hangmanPlayer:hangmanGameHandler) {
			try {
				if (!hangmanPlayer.playersName.equals(playersName)){
					hangmanPlayer.bufferedWriter.write(message);
					hangmanPlayer.bufferedWriter.newLine();
					hangmanPlayer.bufferedWriter.flush();
				}
			} catch(IOException e) {
				closeGame(socket, bufferedReader, bufferedWriter);
			}
		}
	}
	
	//Removes a player from the game
	public void removePlayer() {
		hangmanGameHandler.remove(this);
		broadcastMessage(playersName + " has left the game");
		int players = hangmanGameHandler.size();
		if(players == 1) {
			broadcastMessage("You are the only player playing the game");
		}else {
			broadcastMessage("There are " + players + " playing the game");
		}
		
	}
	
	//Ends the game for a player
	public void closeGame(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		removePlayer();
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
			closeGame(socket, bufferedReader, bufferedWriter);
		}
		
	}
	

	//Selects a random word from the dictionary
	 public String selectRandomWord() {
	        Random random = new Random();
	        int randomIndex = random.nextInt(dictionary.size());
	        return dictionary.get(randomIndex);
	    }
	 
	 //Turns the game to underscores
	 public void initializeCurrentWordState() {
	        for (int i = 0; i < wordToGuess.length(); i++) {
	            currentWordState.append("_");
	        }
	    }
	 
	 //Checks if a guessed letter is in the word or not
	 public boolean guessLetter(String letter) {
	        boolean isLetterPresent = false;
	 
	        // Check if the letter has already been guessed.
	        if (guessedLetters.contains(letter)) {
	            printWriter.println("You have already guessed this letter. Please try again.");
	            return false;
	        }
	 
	        // Add the guessed letter to the set of guessed letters.
	        guessedLetters.add(letter);
	 
	        // Check if the guessed letter is present in the word.
	        for (int i = 0; i < wordToGuess.length(); i++) {
	        	char c = letter.charAt(0);
	            if (wordToGuess.charAt(i) == c) {
	                // Update the current word state with the guessed letter.
	                currentWordState.setCharAt(i, letter.charAt(0));
	               
	                isLetterPresent = true;
	            }
	        }
	 
	        // If the guessed letter is not present, decrease the number of attempts remaining.
	        if (!isLetterPresent) {
	            attemptsRemaining--;
	        }
	 
        	return isLetterPresent;
	 }
	 
	 //Checks if the player has won the game or not
	  public boolean hasPlayerWon() {
	        boolean playerWon = currentWordState.toString().equals(wordToGuess);
	        if(playerWon) {
	        	isGameOver = true;
	        }
	        
	        return playerWon;
	  }
	  
	  //Checks if the player has lost the game or not
	  public boolean hasPlayerLost() {
	        boolean playerLost = attemptsRemaining == 0;
	        if(playerLost) {
	        	isGameOver = true;
	        }
	        
	        return playerLost;
	  }
	  
	  //Getter for game state
	  public String getCurrentWordState() {
	        return currentWordState.toString();
	  }
	 
	  //Getter for game state
	  public int getAttemptsRemaining() {
	        return attemptsRemaining;
	  }
	  
	  //Getter for game state
	  public Set<String> getGuessedLetters() {
	        return guessedLetters;
	  }
	  
	  //Starts the Hangman game for the player
	  public void startHangmanGame() {
		  //gameLock.lock();
	        try {
	        	if(isGameOver) {
	        		return;
	        	}
	        	printWriter.println("The word to guess has " + wordToGuess.length() + " letters.");
	        	printWriter.println("You have " + attemptsRemaining + " attempts remaining.");
	 
	        	printWriter.println("Current word state: " + getCurrentWordState());
	        	printWriter.println("Guessed letters: " + getGuessedLetters());
	        	printWriter.println("Guess a letter: ");
	        	String temp = bufferedReader.readLine().toLowerCase();
	        	String[] parts = temp.split(" ");
	        	String letter = parts[1];
	        	System.out.println(letter);
	        
	 
	        	boolean isLetterPresent = guessLetter(letter);
	 
	        	if (isLetterPresent) {
	        		printWriter.println("Correct guess!");
	        	} else {
	        		printWriter.println("Incorrect guess. Attempts remaining: " + getAttemptsRemaining());
	        	}
	 
	        	if (hasPlayerWon()) {
	        		printWriter.println("Congratulations! You have won the game. The word was: " + wordToGuess);
	        		broadcastMessage(playersName + " has won the game!");
	        		isGameOver = true;
	        	
	        	} else if (hasPlayerLost()) {
	        		printWriter.println("Game over! You have run out of attempts. The word was: " + wordToGuess);
	        		isGameOver = true;
	        		
        		}
	        }catch(IOException e) {
	        	closeGame(socket, bufferedReader, bufferedWriter);
	        }
	        finally {
	        	//gameLock.unlock();
	        }
	 
	 }
	 
	 
}