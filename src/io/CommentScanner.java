package io;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * A scanner which ignores all lines that contain standard java comments.
 * This class skips over any text on a single line preceded by a //.
 * It also skips any text contained in a /* block. Any blank lines are
 * also stripped from the input, so that only lines with uncommented
 * text are returned. 
 * */
public class CommentScanner implements Closeable{
	//The current state, are we in a comment block? true for yes.
	private boolean commented;
	//The buffer, we use this to detect next lines.
	private String next;
	//The internal scanner we read from.
	private Scanner sc;
	
	/**
	 * Creates a new CommentScanner object.
	 * @param f The file to read from
	 */
	public CommentScanner(File f) throws FileNotFoundException{
		this.sc = new Scanner(f);
	}
	
	/**
	 * Returns true if we have available input, false otherwise.
	 * @return true if we have available input, false otherwise.
	 */
	public boolean hasNextLine(){
		if(next != null){
			return true;
		}
		else if(sc.hasNextLine()){
			try{
				next = readLine();
			}
			catch(IndexOutOfBoundsException e){
				return false; //No more data.
			}
			
			return true;
		}
		return false; //Nothing in buffer, nothing in scanner. End of file.
	}
	
	/**
	 * Reads the next line of uncommented input.
	 * @return the next line of uncommented input
	 * @throws IndexOutOfBoundsException if we have no more input available
	 */
	public String readLine(){
		if(next != null){ //We have one in the buffer
			String s = next;
			next = null; //We're using this input up.
			s = parse(s);
			if(s.isEmpty() == false) return s;
		}
		
		while(sc.hasNextLine()){ //Wait for some valid input
			String s = sc.nextLine();
			s = parse(s);
			if(s.isEmpty() == false) return s;
		}
		
		//Nothing in the buffer, nothing in the scanner
		throw new IndexOutOfBoundsException("No more file input.");
	}
	
	private String parse(String s){
		if(commented){
			//We are in a comment block.
			int end = s.indexOf("*/");
			if(end < 0) return ""; //Line is commented out.
			else{
				s = s.substring(end + 2, s.length());
				commented = false;
				return parse(s); //We've removed that comment.
			}
		}
		else{
			int index1 = s.indexOf("//");
			int index2 = s.indexOf("/*");
			
			if(index1 >= 0 && index2 >= 0){
				if(index1 > index2){
					index1 = -1;
				}
				else{
					index2 = -1;
				}
			}
			
			if(index1 >= 0){
				return s.substring(0, index1); //Read the whole line up until the //.
			}
			else if(index2 >= 0){
				commented = true;
				s = s.substring(0, index2) + parse(s.substring(index2 + 2, s.length()));
				return s;
			}
			else{
				return s; //No comment here.
			}
		}
	}
	
	/**
	 * Closes the underlying scanner/file input stream.
	 */
	@Override
	public void close() throws IOException {
		sc.close();
	}
	
}