package org.maxgamer.structure.areagrid;

import java.util.ArrayList;
import java.util.HashSet;

public class AreaGrid<T extends MBR>{
	final int BITS_PER_REF = 64;
	
	/** The number of bits we need to shift when converting x/y coordinates to grid coordinates - Faster than dividing*/
	private byte bits;
	/** The array of grids we are to use. */
	private Grid[][] grid;
	
	public AreaGrid(int width, int height, int lengths){
		if((lengths & -lengths) != lengths){ // It looks like voodoo, but will return true if lengths isn't a power of 2.
			throw new IllegalArgumentException("Lengths should be a multiple of 2!");
		}
		width = (width + lengths - 1)/lengths;
		height = (height + lengths - 1)/lengths;
		
		//Counts the number of bits required to bitshift.
		if( lengths >= 0x7FFF ) { lengths >>= 15; bits += 15; }
		if( lengths >= 0x7F ) { lengths >>= 7; bits += 7; }
		if( lengths >= 0x7 ) { lengths >>= 3; bits += 3; }
		if( lengths >= 0x3 ) { lengths >>= 2; bits += 2; }
		if( lengths >= 0x1 ) { lengths >>= 1; bits += 1; }
		if( lengths >= 0x1 ) { lengths >>= 1; bits += 1; }
		
		bits--; //Take one for some reason.
		
		//Initialize our grid array.
		grid = new Grid[width][];		
		for(int i = 0; i < width; i++){
			grid[i] = new Grid[height];
		}
	}
	
	/**
	 * Finds an approximate amount of memory used by this grid.
	 * Does not include the size of all mbr's in the field.
	 * @return The memory usage, in bits.
	 */
	public int getMemSize(){
		int m = 0;
		m += 8; //Bits field
		m += BITS_PER_REF; //Grid[][] Field.
		
		m += grid.length * BITS_PER_REF;
		for(int i = 0; i < grid.length; i++){
			m += grid[i].length * BITS_PER_REF;
			for(int j = 0; j < grid[i].length; j++){
				if(grid[i][j] == null) continue;
				m += BITS_PER_REF; //Arraylist
				m += 32; //Arraylist size field
				m += BITS_PER_REF * 16; //Arraylist objects
				m += grid[i][j].objects.size() * BITS_PER_REF;
			}
		}
		return m;
	}
	
	protected void validate(MBR m){
		if(m.getDimensions() < 2){
			throw new IllegalArgumentException("AreaGrids are 2D, and thus require MBR's have two dimensions at least.");
		}
		if(m.getDimension(0) < 0 || m.getDimension(1) < 0){
			throw new IllegalArgumentException("AreaGrids require all MBR's to have all dimensions (lengths) of minimum 0.");
		}
	}
	
	/**
	 * Gets all MBRs that overlap with the given MBR.
	 * @param query The MBR to check for overlaps with
	 * @param guess The normal maximum number of results to expect (Efficiency)
	 * @return A HashSet (Never null, possible empty) of overlapping results.
	 */
	@SuppressWarnings("unchecked")
	public HashSet<T> get(MBR query, int guess){
		validate(query);
		
		int X = (query.getMin(0)) >> bits;
		int Y = (query.getMin(1)) >> bits;
		
		//We want to reuse the bits that were dropped off from above, and add them here.
		int dx = ((query.getMin(0) & ((1 << bits) - 1)) + (query.getDimension(0)) >> bits);
		int dy = ((query.getMin(1) & ((1 << bits) - 1)) + (query.getDimension(1)) >> bits);
		
		HashSet<T> objects = new HashSet<T>(guess);
		
		//We must put it in each grid that it overlaps with.
		for(int xOffset = 0; xOffset <= dx; xOffset++){
			for(int yOffset = 0; yOffset <= dy; yOffset++){
				Grid g = grid[X + xOffset][Y + yOffset];
				if(g != null){
					synchronized(g.objects){
						for(MBR o : g.objects){
							//Version 2.0
							//We use <= query.getMin(0) because the boundaries TOUCH but do not overlap!
							if(o.getMin(0) + o.getDimension(0) <= query.getMin(0)) continue; //o's max is lower than query's min
							if(o.getMin(0) > query.getMin(0) + query.getDimension(0)) continue; //o's min is higher than query's max
							
							if(o.getMin(1) + o.getDimension(1) <= query.getMin(1)) continue; //o's max is lower than query's min
							if(o.getMin(1) > query.getMin(1) + query.getDimension(1)) continue; //o's min is higher than query's max
							objects.add((T) o);
						}
					}
				}
			}
		}
		
		
		return objects;
	}
	
	@SuppressWarnings("unchecked")
	public <U extends T> HashSet<U> get(MBR query, int guess, Class<U> clazz){
		validate(query);
		
		int X = (query.getMin(0)) >> bits;
		int Y = (query.getMin(1)) >> bits;
		
		//We want to reuse the bits that were dropped off from above, and add them here.
		int dx = ((query.getMin(0) & ((1 << bits) - 1)) + (query.getDimension(0)) >> bits);
		int dy = ((query.getMin(1) & ((1 << bits) - 1)) + (query.getDimension(1)) >> bits);
		
		HashSet<U> objects = new HashSet<U>(guess);
		
		//We must put it in each grid that it overlaps with.
		for(int xOffset = 0; xOffset <= dx; xOffset++){
			for(int yOffset = 0; yOffset <= dy; yOffset++){
				Grid g;
				try{
					g = grid[X + xOffset][Y + yOffset];
				}
				catch(IndexOutOfBoundsException e){
					//We're < 0 or >= length. There are no MBR's here!
					continue;
				}
				if(g != null){
					synchronized(g.objects){
						for(MBR o : g.objects){
							//Version 2.0
							//We use <= query.getMin(0) because the boundaries TOUCH but do not overlap!
							if(o.getMin(0) + o.getDimension(0) <= query.getMin(0)) continue; //o's max is lower than query's min
							if(o.getMin(0) > query.getMin(0) + query.getDimension(0)) continue; //o's min is higher than query's max
							
							if(o.getMin(1) + o.getDimension(1) <= query.getMin(1)) continue; //o's max is lower than query's min
							if(o.getMin(1) > query.getMin(1) + query.getDimension(1)) continue; //o's min is higher than query's max
							
							if(clazz.isInstance(o)){
								objects.add((U) o);
							}
						}
					}
				}
			}
		}
		
		
		return objects;
	}
	
	/**
	 * Fetches a list of MBR's which overlap with the given coordinates.
	 * @param x The X coordinate
	 * @param y The Y coordinate
	 * @return The MBR's at that position.
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<T> get(int x, int y){
		
		int X = (x) >> bits;
		int Y = (y) >> bits;
		
		Grid g = grid[X][Y];
		if(g == null){
			return new ArrayList<T>(0);
		}
		ArrayList<T> objects;
		synchronized(g.objects){
			objects = new ArrayList<T>(g.objects.size());
			for(MBR m : g.objects){
				//Why do we use <= here?
				//Because if it were < it would cause inconsistency.
				//If it is on the very edge of a grid, it would not
				//be added to the neighbour grid. Thus, the object's
				//ordinate should be > x and not >= x.
				
				if(m.getMin(0) > x) continue; //x is too small.
				if(m.getMin(0) + m.getDimension(0) <= x) continue; //x is too big
				
				if(m.getMin(1) > y) continue; //y is too small.
				if(m.getMin(1) + m.getDimension(1) <= y) continue; //y is too big.
				objects.add((T) m);
			}
		}
		
		return objects;
	}
	
	/**
	 * Adds the given MBR to this grid.
	 * @param m The MBR to add.
	 */
	public void put(T m){
		validate(m);
		
		int X = (m.getMin(0)) >> bits;
		int Y = (m.getMin(1)) >> bits;
		
		int dx = (m.getDimension(0)) >> bits;
		int dy = (m.getDimension(1)) >> bits;
		
		//We must put it in each grid that it overlaps with.
		for(int xOffset = 0; xOffset <= dx; xOffset++){
			for(int yOffset = 0; yOffset <= dy; yOffset++){
				Grid g = grid[X + xOffset][Y + yOffset];
				if(g == null){
					g = new Grid(20 << bits); //Guess size for RS objects is usually 150 entities per 8x8 cube.
					grid[X + xOffset][Y + yOffset] = g;
				}
				synchronized(g.objects){
					g.objects.add(m);
				}
			}
		}
		
	}
	
	/**
	 * Removes the given MBR from this area grid.
	 * @param m The MBR to remove.
	 */
	public void remove(T m){
		validate(m);
		
		int X = (m.getMin(0)) >> bits;
		int Y = (m.getMin(1)) >> bits;
		
		int dx = (m.getDimension(0)) >> bits;
		int dy = (m.getDimension(1)) >> bits;
		
		//We must put it in each grid that it overlaps with.
		for(int xOffset = 0; xOffset <= dx; xOffset++){
			for(int yOffset = 0; yOffset <= dy; yOffset++){
				Grid g = grid[X + xOffset][Y + yOffset];
				if(g == null){
					continue;
				}
				synchronized(g.objects){
					g.objects.remove(m);
				}
			}
		}
		
	}
	
	public static class Grid{
		private ArrayList<MBR> objects;
		public Grid(int guessSize){ objects = new ArrayList<MBR>(guessSize); }
	}
}