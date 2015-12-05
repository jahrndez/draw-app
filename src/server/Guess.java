package server;

/**
 * Store the information that clients send to server
 */
public class Guess {

    private Player player;
    private String guess;
	
	public Guess(Player player, String guess) {
		this.player = player;
		this.guess = guess;
	}
	
	public Player player() {
        return player;
    }
	
	public String word() {
		return guess;
	}
}
