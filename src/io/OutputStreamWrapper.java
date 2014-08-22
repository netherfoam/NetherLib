package org.maxgamer.maxbans.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class OutputStreamWrapper extends OutputStream{
	private OutputStream o;
	public OutputStreamWrapper(OutputStream out) {
		this.o = out;
	}
	
	public void writeByte(byte b) throws IOException{
		this.write(b);
	}
	public void writeShort(short s) throws IOException{
		this.writeByte((byte) (s >> 8));
		this.writeByte((byte) s);
	}
	public void writeInt(int i) throws IOException{
		this.writeShort((short) (i >> 16));
		this.writeShort((short) i);
	}
	public void writeLong(long l) throws IOException{
		this.writeInt((int) (l >> 32));
		this.writeInt((int) l);
	}
	public void writeChar(char c) throws IOException{
		this.writeByte((byte) c);
	}
	public void write(String s) throws IOException{
		byte[] data;
		try {
			data = s.getBytes(InputStreamWrapper.CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			data = new byte[0];
		}
		for(byte b : data){
			this.write(b);
		}
		this.write(0);
	}
	@Override
	public void close(){
		try {
			o.close();
		} catch (IOException e) {
			//end
		}
	}

	@Override
	public void write(int b) throws IOException {
		o.write(b);
	}
}