package io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CircularBuffer implements ByteReader, ByteWriter{
	public static void main(String[] args) throws IOException{
		byte[] data = new byte[]{1,2,3,4};
		CircularBuffer cb = new CircularBuffer(data.length);
		
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
	 * @throws IOException 
	 */
	public void writeByte(byte b) throws IOException {
		if((writepos + 1) % data.length == mark){
			throw new IOException("No space available");
		}
		
		data[writepos++] = b;
		if(writepos >= data.length){
			writepos = 0;
		}
		size++;
	}
	
	/**
	 * Reads a byte.
	 * @return The byte read
	 * @throws IOException if the buffer is empty
	 */
	public byte readByte() throws IOException{
		if(readpos == writepos){
			throw new IOException("The buffer is empty!");
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
	
	public void writeShort(short s) throws IOException{
		writeByte((byte) (s >> 8));
		writeByte((byte) s);
	}
	public void writeInt(int i) throws IOException{
		writeShort((short) (i >> 16));
		writeShort((short) i);
	}
	public void writeLong(long l) throws IOException{
		writeInt((int) (l >> 16));
		writeInt((int) l);
	}
	public void writeFloat(float f) throws IOException{
		writeInt(Float.floatToIntBits(f));
	}
	public void writeDouble(double d) throws IOException{
		writeLong(Double.doubleToRawLongBits(d));
	}
	
	public short readShort() throws IOException{
		return (short) (((readByte() & 0xFF) << 8) | (readByte() & 0xFF));
	}
	public int readInt() throws IOException{
		return (((readShort() & 0xFFFF) << 16) | (readShort() & 0xFFFF));
	}
	public long readLong() throws IOException{
		return (((readInt() & 0xFFFFFFFF) << 16) | (readInt() & 0xFFFFFFFF));
	}
	public float readFloat() throws IOException{
		return Float.intBitsToFloat(readInt());
	}
	public double readDouble() throws IOException{
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
	public void write(byte[] src, int start, int end) throws IOException {
		while(start < end){
			this.writeByte(src[start++]);
		}
	}

	@Override
	public void write(byte[] src) throws IOException {
		this.write(src, 0, src.length);
	}

	@Override
	public void read(byte[] dest, int start, int end) throws IOException {
		while(start < end){
			dest[start++] = readByte();
		}
	}

	@Override
	public void read(byte[] dest) throws IOException {
		read(dest, 0, dest.length);
	}

	@Override
	public OutputStream getOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				CircularBuffer.this.writeByte((byte) b);
			}
		};
	}

	@Override
	public InputStream getInputStream() {
		return new InputStream() {
			@Override
			public int read() throws IOException {
				return CircularBuffer.this.readByte() & 0xFF; //Must be 0-255
			}
		};
	}
}