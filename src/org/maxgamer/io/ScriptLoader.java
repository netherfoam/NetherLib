package org.maxgamer.io;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class ScriptLoader<T>{
	private File folder;
	private LinkedList<Class<T>> scripts;
	private static Pattern exclude = Pattern.compile("\\$\\d{1,}");
	
	private Class<T> type;
	
	public ScriptLoader(Class<T> types, File folder){
		this.type = types;
		this.folder = folder;
	}
	
	public void reload(){
		this.scripts = new LinkedList<Class<T>>();
		
		if(!folder.exists()) {
			return;
		}
		
		FilenameFilter fil = new FilenameFilter() {
			@Override
			public boolean accept(File f, String name) {
				if (name.endsWith(".class") && !exclude.matcher(name).find()) {
					return true;
				}
				return false;
			}
		};

		LinkedList<File> files = ScriptLoader.getFiles(folder, fil);
		
		URL[] urls = new URL[files.size()];
		for (int i = 0; i < files.size(); i++) {
			try {
				urls[i] = files.get(i).toURI().toURL();
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		URL url = null;
		try {
			url = folder.toURI().toURL();
		}
		catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		
		ClassLoader clazzLoaderOld = Thread.currentThread().getContextClassLoader();

		URLClassLoader reloader = new URLClassLoader(new URL[]{url});
		Thread.currentThread().setContextClassLoader(reloader);
		
		for (File file : files) {
			String name = file.toString();
			name = name.replace(folder.toString() + File.separator, ""); //Remove base path
			name = name.substring(0, name.length() - 6); //Trim off .class
			name = name.replace(File.separator, ".");
			try {
				@SuppressWarnings("unchecked")
				Class<T> clazz = (Class<T>) reloader.loadClass(name);
				
				if(this.type.isInterface()){
					//It's an interface.
					if(isInterface(this.type, clazz) == false){
						continue; //We're not looking for you.
					}
				}
				else{
					//It's a class
					if(isSuperClass(clazz, this.type) == false){
						continue; //We're not looking for you.
					}
				}
				
				scripts.add(clazz);
			}
			catch (Exception e) {
				System.out.println(e.getMessage() + " Failed to load - " + e.getClass().getSimpleName());
			}
			catch (Error e) {
				e.printStackTrace();
				System.out.println("[Severe] Something went wrong loading " + file.toString());
			}
		}
		Thread.currentThread().setContextClassLoader(clazzLoaderOld);
	}
	
	/**
	 * Returns true if the given base class is a subclass of the given superclass.
	 * @param base The base class
	 * @param superClazz The class which might be a super class
	 * @return true if the given base class is a subclass of the given superclass.
	 */
	private static boolean isSuperClass(Class<?> base, Class<?> superClazz){
		while(base != null){
			if(base.equals(superClazz)){
				return true;
			}
			base = base.getSuperclass();
		}
		return false;
	}
	
	private static boolean isInterface(Class<?> interfac, Class<?> child){
		LinkedList<Class<?>> interfaces = new LinkedList<Class<?>>();
		Class<?>[] starters = child.getInterfaces();
		
		for(int i = 0; i < starters.length; i++){
			interfaces.add(starters[i]);
		}
		
		while(interfaces.isEmpty() == false){
			Class<?> face = interfaces.pop();
			if(face.getName().equals(interfac.getName())){
				return true;
			}
			else{
				starters = face.getInterfaces();
				for(int i = 0; i < starters.length; i++){
					interfaces.add(starters[i]);
				}
			}
		}
		return false;
	}
	
	public LinkedList<Class<T>> getScripts(){
		if(scripts == null) {
			reload();
		}
		return scripts;
	}
	
	/**
	 * Gets the script by the given name. This method is case insensitive for convenience.
	 * @param name The name of the script
	 * @return The script, or null if it was not found.
	 */
	public Class<T> getScript(String name){
		for(Class<T> clazz : this.getScripts()){
			if(clazz.getName().equalsIgnoreCase(name)){
				return clazz;
			}
		}
		return null;
	}
	
	/**
	 * Fetches all files in the given folder that can be accepted by the given filter. This method is recursive.
	 * 
	 * @param dir
	 *            The directory to search
	 * @param filter
	 *            The filter. Must not be null.
	 * @return The list of files, never null.
	 */
	public static LinkedList<File> getFiles(File dir, FilenameFilter filter) {
		LinkedList<File> files = new LinkedList<File>();
		if (dir == null || dir.isDirectory() == false) {
			return files;
		}

		for (File f : dir.listFiles()) {
			if (f.getName().startsWith(".")) {
				continue;
			}
			if (f.isDirectory()) {
				files.addAll(getFiles(f, filter));
			}

			if (filter.accept(f, f.getName())) {
				files.add(f);
			}
		}

		return files;
	}
}