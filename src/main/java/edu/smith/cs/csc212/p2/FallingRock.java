package edu.smith.cs.csc212.p2;

public class FallingRock extends Rock {
	public FallingRock(World world) {
		super(world);
	}
	public void step() {
		this.moveDown();
	}
}
