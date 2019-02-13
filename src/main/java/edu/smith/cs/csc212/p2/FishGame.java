package edu.smith.cs.csc212.p2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class manages our model of gameplay: missing and found fish, etc.
 * @author jfoley
 *
 */
public class FishGame {
	/**
	 * This is the world in which the fish are missing. (It's mostly a List!).
	 */
	World world;
	/**
	 * The player (a Fish.COLORS[0]-colored fish) goes seeking their friends.
	 */
	Fish player;
	/**
	 * The home location.
	 */
	FishHome home;
	/**
	 * These are the missing fish!
	 */
	List<Fish> missing;
	
	/**
	 * These are fish we've found!
	 */
	List<Fish> found;
	
	/**
	 * These are fish that are at home
	 */
	List<Fish> atHome;
	
	/**
	 * Number of steps!
	 */
	int stepsTaken;
	
	/**
	 * Score!
	 */
	int score;
	
	
	public static final int NUM_ROCKS = 10;
	/**
	 * Create a FishGame of a particular size.
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	public FishGame(int w, int h) {
		world = new World(w, h);
		
		missing = new ArrayList<Fish>();
		found = new ArrayList<Fish>();
		atHome = new ArrayList<Fish>();
		
		// Add a home!
		home = world.insertFishHome();
		
		//Insert rocks
		for (int i=0; i<NUM_ROCKS; i++) {
			world.insertRockRandomly();
		}
		
		//Insert a snail
		world.insertSnailRandomly();
		
		// Make the player out of the 0th fish color.
		player = new Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);
		
		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < Fish.COLORS.length - 1; ft++) {
			Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);
		}
	}
	
	
	/**
	 * How we tell if the game is over: if missingFishLeft() == 0.
	 * @return the size of the missing list.
	 */
	public int missingFishLeft() {
		return missing.size();
	}
	
	/**
	 * This method is how the PlayFish app tells whether we're done.
	 * @return true if the player has won (or maybe lost?).
	 */
	public boolean gameOver() {
		return found.isEmpty() && missing.isEmpty();
	}

	/**
	 * Update positions of everything (the user has just pressed a button).
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1;
				
		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();
		// The player is there, too, let's skip them.
		overlap.remove(this.player);
		
		// If we find a fish, remove it from missing.
		for (WorldObject wo : overlap) {
			// It is missing if it's in our missing list.
			if (missing.contains(wo)) {
				// Remove this fish from the missing list.
				missing.remove(wo);
				// Add fish to found list
				found.add((Fish) wo);
				
				// Increase score when you find a fish!
				score += ((Fish) wo).getPoints();
			}
		}
		
		// Make sure missing fish *do* something.
		wanderMissingFish();
		comeHome();
		
		//Use getLost() if the player has taken at least 20 steps
		if(stepsTaken > 20) {
			getLost();
		}
		// When fish get added to "found" they will follow the player around.
		World.objectsFollow(player, found);
		// Step any world-objects that run themselves.
		world.stepAll();
	}
	
	/**
	 * Call moveRandomly() on all of the missing fish to make them seem alive.
	 */
	private void wanderMissingFish() {
		Random rand = ThreadLocalRandom.current();
		for (Fish lost : missing) {
			if(rand.nextDouble()< 0.8 && lost.fastScared == true) {
					lost.moveRandomly();
				}
			else if (rand.nextDouble() < 0.3) {
					lost.moveRandomly();
					}
		}
	}
	/**
	 * Make it so that there is a 10% chance that fish that are not first in line get lost again
	 */
	public void getLost() {
		Random rand = ThreadLocalRandom.current();
		for (Fish f : found) {
			if(found.indexOf(f) >= 1) {
				if(rand.nextDouble() > 0.9) {
					missing.add(f);
				}
			}
		}
		for(Fish f : missing) {
			if(found.contains(f)) {
				found.remove(f);
				}
		}
	}

	
	public void comeHome(){
		//Check to see if any found fish are home, and add them to atHome if there are
		if (found.size() > 0) {
			for(Fish f : found) {
				List<WorldObject> overlap = f.findSameCell();
				for(int i = 0; i < overlap.size(); i++) {
					if(overlap.get(i) instanceof FishHome){
						for(int j = 0; j < overlap.size(); j++) {
							if(overlap.get(j) instanceof Fish){
								world.remove(overlap.get(j));
							if (found.contains(overlap.get(j))) {
									atHome.add((Fish)overlap.get(j));
								}
								
							}
						}
					}
				}
			}
		}
		//Check to see if any missing fish are home, and add them to atHome if there are
		if (missing.size() > 0) {
			for(Fish f : missing) {
				List<WorldObject> overlap = f.findSameCell();
				for(int i = 0; i < overlap.size(); i++) {
					if(overlap.get(i) instanceof FishHome){
						for(int j = 0; j < overlap.size(); j++) {
							if(overlap.get(j) instanceof Fish){
								world.remove(overlap.get(j));
							if (missing.contains(overlap.get(j))) {
									atHome.add((Fish)overlap.get(j));
								}
								
							}
						}
					}
				}
			}
		}
		//Remove fish that are in atHome from found and missing
		for(Fish f : atHome) {
			if(found.contains(f)) {
				found.remove(f);
				}
			if(missing.contains(f)) {
				missing.remove(f);
			}
		}
	}

	/**
	 * This gets a click on the grid. We want it to destroy rocks that ruin the game.
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 */
	public void click(int x, int y) {
		// TODO(P2) use this print to debug your World.canSwim changes!
		System.out.println("Clicked on: "+x+","+y+ " world.canSwim(player,...)="+world.canSwim(player, x, y));
		List<WorldObject> atPoint = world.find(x, y);
		for (WorldObject it : atPoint) {
			if (it instanceof Rock) {
			it.remove();	
			}
		}
	}

	
}
