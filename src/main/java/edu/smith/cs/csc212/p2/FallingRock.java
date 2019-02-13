package edu.smith.cs.csc212.p2;

public class FallingRock extends Rock {
	public FallingRock(World world) {
		super(world);
	}
	
	/*
	 * Make the FallingRock move down every time the player steps
	 */
	public void step() {
		this.moveDown();
	}
}
