package io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class CircularBuffer implements ByteReader, ByteWriter{
	public static void main(String[] args) {
		byte[] data = new byte[]{1,2,3,4};
		CircularBuffer cb = new CircularBuffer(data.length - 1);
		
		for(byte b : data){
			cb.writeByte(b);
		}
		
		System.out.println("Read: " + cb.readByte());
		cb.mark(); System.out.println("Marked");
		System.out.println("Read: " + cb.readByte());
		System.out.println("Read: " + cb.readByte());
		cb.reset(); System.out.println("Reset");
		System.out.println("Read: " + cb.readByte());
		System.out.println("Read: " + cb.readByte());
		System.out.println("Read: " + cb.readByte());
		cb.mark(); System.out.println("Marked");
		
		for(byte b : new byte[]{5,6,7,8}){
			cb.writeByte(b);
		}
		
		System.out.println("Read: " + cb.readByte());
		cb.mark(); System.out.println("Marked");
		System.out.println("Read: " + cb.readByte());
		System.out.println("Read: " + cb.readByte());
		cb.reset(); System.out.println("Reset");
		System.out.println("Read: " + cb.readByte());
		System.out.println("Read: " + cb.readByte());
		System.out.println("Read: " + cb.readByte());
		cb.mark(); System.out.println("Marked");
		
		for(byte b : new byte[]{9,10,11,12}){
			cb.writeByte(b);
		}
		
		System.out.println("Read: " + cb.readByte());
		cb.mark(); System.out.println("Marked");
		System.out.println("Read: " + cb.readByte());
		System.out.println("Read: " + cb.readByte());
		cb.reset(); System.out.println("Reset");
		System.out.println("Read: " + cb.readByte());
		System.out.println("Read: " + cb.readByte());
		System.out.println("Read: " + cb.readByte());
		cb.mark(); System.out.println("Marked");
	}
	
	/** The buffer data */
	protected byte[] data;
	
	/** the index to write at next. When this equals readpos, we have no data available */
	protected int writepos = 0;
	
	/** The index of the next byte read.  When this equals readpos, we have no data available */
	protected int readpos = 0;
	
	/** The position that was marked, we cannot advance past this. */
	protected int mark = 0;
	
	/** Size since last mark */
	protected int size = 0;
	
	/**
	 * Constructs a circular buffer of the given size.
	 * This buffer will auto-resize in factors of two
	 * of the given initial size when more space
	 * is required.
	 * @param initSize the given size
	 */
	public CircularBuffer(int initSize){
		if(initSize <= 0){
			throw new IllegalArgumentException("May not create a circular buffer with size <= 0, given: " + initSize);
		}
		this.data = new byte[initSize + 1];
	}
	
	/**
	 * Writes the given byte, resizing if necessary
	 * @param b the byte to write
	 * @ 
	 */
	public void writeByte(byte b)  {
		if((writepos + 1) % data.length == mark){
			//throw new IndexOutOfBoundsException("No space available");
			resize((this.data.length - 1) * 2);
		}
		
		data[writepos++] = b;
		if(writepos >= data.length){
			writepos = 0;
		}
		size++;
	}
	
	private void resize(int newSize){
		int available = this.available();
		if(newSize < available) throw new IllegalArgumentException("Cannot fit " + available() + " bytes into a " + newSize + " array!");
		
		byte[] newData = new byte[newSize + 1]; //+1, see constructor.
		this.reset(); //We want all of the data from our mark (inclusive) up to our writepos (exclusive)
		
		int write = this.available();
		
		this.read(newData, 0, write);
		
		this.mark = 0;
		this.readpos = write - available;
		this.writepos = write;
		
		System.out.println("Resized from " + (this.data.length - 1) + " to " + (newData.length - 1));
		System.out.println("M: " + this.mark + ", R: " + this.readpos + ", W: " + this.writepos);
		System.out.println("New Data: " + Arrays.toString(newData));
		this.data = newData;
	}
	
	/**
	 * Reads a byte.
	 * @return The byte read
	 * @ if the buffer is empty
	 */
	public byte readByte() {
		if(readpos == writepos){
			throw new IndexOutOfBoundsException("The buffer is empty!");
		}
		
		byte b = data[readpos++];
		
		if(readpos >= data.length){
			readpos = 0;
		}
		
		return b;
	}
	
	/**
	 * @return the number of available bytes
	 */
	public int available(){
		if(readpos <= writepos){
			return writepos - readpos;
		}
		else{
			return data.length - (writepos - readpos);
		}
	}
	
	public void writeShort(short s) {
		writeByte((byte) (s >> 8));
		writeByte((byte) s);
	}
	public void writeInt(int i) {
		writeShort((short) (i >> 16));
		writeShort((short) i);
	}
	public void writeLong(long l) {
		writeInt((int) (l >> 16));
		writeInt((int) l);
	}
	public void writeFloat(float f) {
		writeInt(Float.floatToIntBits(f));
	}
	public void writeDouble(double d) {
		writeLong(Double.doubleToRawLongBits(d));
	}
	
	public short readShort() {
		return (short) (((readByte() & 0xFF) << 8) | (readByte() & 0xFF));
	}
	public int readInt() {
		return (((readShort() & 0xFFFF) << 16) | (readShort() & 0xFFFF));
	}
	public long readLong() {
		return (((readInt() & 0xFFFFFFFF) << 16) | (readInt() & 0xFFFFFFFF));
	}
	public float readFloat() {
		return Float.intBitsToFloat(readInt());
	}
	public double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public boolean isEmpty() {
		return available() <= 0;
	}
	
	public void mark(){
		this.mark = this.readpos;
		this.size = available();
	}
	
	public void reset(){
		this.readpos = this.mark;
	}

	@Override
	public void write(byte[] src, int start, int end)  {
		while(start < end){
			this.writeByte(src[start++]);
		}
	}

	@Override
	public void write(byte[] src)  {
		this.write(src, 0, src.length);
	}

	@Override
	public void read(byte[] dest, int start, int end)  {
		while(start < end){
			dest[start++] = readByte();
		}
	}

	@Override
	public void read(byte[] dest)  {
		read(dest, 0, dest.length);
	}

	@Override
	public OutputStream getOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int b)  {
				CircularBuffer.this.writeByte((byte) b);
			}
		};
	}

	@Override
	public InputStream getInputStream() {
		return new InputStream() {
			@Override
			public int read()  {
				return CircularBuffer.this.readByte() & 0xFF; //Must be 0-255
			}
		};
	}
}