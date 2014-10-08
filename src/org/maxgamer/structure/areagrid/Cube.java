package org.maxgamer.structure.areagrid;


public class Cube implements MBR {
	private int[] coords;
	private int[] dimensions;

	public Cube(int[] coords, int[] dimensions) {
		if (coords.length != dimensions.length) {
			throw new RuntimeException("Cannot have a MBR which has " + coords.length + " coords but " + dimensions.length + " dimensions!");
		}

		this.coords = coords;
		this.dimensions = dimensions;
	}

	@Override
	public int getMin(int axis) {
		return coords[axis];
	}

	@Override
	public int getDimension(int axis) {
		return dimensions[axis];
	}

	@Override
	public int getDimensions() {
		return dimensions.length;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Cube:");

		for (int i = 0; i < getDimensions(); i++) {
			sb.append(" (" + coords[i] + ".." + ( dimensions[i]) + ")");
		}
		return sb.toString();
	}
}