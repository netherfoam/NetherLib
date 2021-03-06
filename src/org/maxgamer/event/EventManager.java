package org.maxgamer.event;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;

import org.maxgamer.io.ScriptLoader;

public class EventManager {
	private HashMap<EventPriority, LinkedList<HandlerExecutor>> listeners;
	
	public void reload(){
		listeners = new HashMap<EventPriority, LinkedList<HandlerExecutor>>(EventPriority.values().length);
		
		ScriptLoader<EventListener> listeners = new ScriptLoader<EventListener>(EventListener.class, new File("bin"));
		listeners.reload();
		for(Class<EventListener> clazz : listeners.getScripts()){
			//We don't want interfaces or incomplete classes to be registered as commands.
			if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) continue;
			if(clazz.isAnnotationPresent(AutoRegister.class) == false) continue; //Don't autoregister.
			
			try{
				EventListener listener = clazz.newInstance();
				register(listener);
			}
			catch(Exception e){
				System.out.println("Failed to register Generic Listener: " + clazz.getName());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns true if the given base class is a subclass of the given superclass.
	 * @param base The base class
	 * @param superClazz The class which might be a super class
	 * @return true if the given base class is a subclass of the given superclass.
	 */
	private static boolean isSuperClass(Class<?> base, Class<?> superClazz){
		while(base != null){
			if(base == superClazz){
				return true;
			}
			base = base.getSuperclass();
		}
		return false;
	}
	
	/**
	 * Registers the given action listener with the given priority. A listener may not be registered twice.
	 * 
	 * @param listener
	 *            The listener object
	 * @param priority
	 *            The priority to register it as. Parsing null will set priority to ActionPriority.NORMAL
	 */
	public void register(EventListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listeners may not be null.");
		}
		
		LinkedList<HandlerExecutor> handlers = getHandlers(listener);
		if(handlers.isEmpty()){
			System.out.println("<=== WARNING ===>");
			System.out.println("Event listener: " + listener.getClass().getCanonicalName());
			System.out.println("Has no methods which will be registered for events. Are there any defined?");
			System.out.println("Please check all listeners have the @EventHandler annotation.");
			System.out.println("<=== WARNING ===>");
		}
		
		for(HandlerExecutor h : handlers){
			LinkedList<HandlerExecutor> list = listeners.get(h.getPriority());
			if(list == null){
				list = new LinkedList<HandlerExecutor>();
				listeners.put(h.getPriority(), list);
			}
			list.add(h);
		}
		
	}
	
	/**
	 * Fetches all of the <br/>
	 * <b>@ActionHandler</b><br/>methods from the given listener object
	 * @param listener The listener to fetch handlers from
	 * @return A linked list (Never null, possibly empty) of handlers.
	 */
	private LinkedList<HandlerExecutor> getHandlers(EventListener listener){
		LinkedList<HandlerExecutor> actions = new LinkedList<HandlerExecutor>();
		for(Method m : listener.getClass().getMethods()){
			if(isHandler(m) == false) {
				continue;
			}
			
			HandlerExecutor exec = new HandlerExecutor(listener, m);
			actions.add(exec);
		}
		return actions;
	}
	
	/**
	 * Returns true if the given method is an action handler method
	 * This also sets it to accessible if it is not.
	 * @param m The method.
	 * @return true if the given method is an action handler method
	 */
	private static boolean isHandler(Method m){
		if (m.getParameterTypes().length != 1) {
			return false; // Wrong number of args
		}
		Class<?> clazz = m.getParameterTypes()[0];
		if(isSuperClass(clazz, Event.class) == false){
			return false;
		}
		if (m.isAnnotationPresent(EventHandler.class) == false) {
			return false; // There is no @ActionHandler annotation
		}
		if(m.isAccessible() == false){
			m.setAccessible(true);
		}
		return true;
	}

	/**
	 * Unregisters the given listener
	 * 
	 * @param listener
	 *            The listener
	 * @return True if it was removed, false if it could not be found.
	 */
	public boolean unregister(EventListener listener) {
		LinkedList<HandlerExecutor> handlers = getHandlers(listener);
		for(HandlerExecutor h : handlers){
			LinkedList<HandlerExecutor> list = listeners.get(h.getPriority());
			if(list != null){
				list.remove(h);
			}
		}
		return false;
	}

	/**
	 * Broadcasts the specified action to all listeners
	 * @param event The action to broadcast.
	 */
	public void callEvent(Event event) {
		if (event == null) {
			throw new NullPointerException("Event may not be null!");
		}
		
		for (EventPriority priority : EventPriority.values()) {
			LinkedList<HandlerExecutor> list = listeners.get(priority);
			if (list == null) {
				continue;
			}
			
			for(HandlerExecutor h : list){
				if(h.getMethod().getParameterTypes()[0].isInstance(event) == false) {
					continue;
				}
				
				try{
					h.getMethod().invoke(h.getTarget(), event);
				}
				catch(Throwable t){
					t.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Debug tool, which returns all of the registered listeners in
	 * a nicely formatted string.
	 * @return The debug info
	 */
	public String describe(){
		StringBuilder sb = new StringBuilder();
		
		for(EventPriority p : EventPriority.values()){
			sb.append("<=== " + p.toString() + " ===>\n");
			LinkedList<HandlerExecutor> list = listeners.get(p);
			if(list == null) {
				continue;
			}
			for(HandlerExecutor h : list){
				sb.append("---> " + h.getMethod().toGenericString() + "\n");
			}
		}
		
		return sb.toString();
	}
}