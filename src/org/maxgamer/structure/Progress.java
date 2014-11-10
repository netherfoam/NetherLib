package org.maxgamer.structure;

/**
 * A simple class which can be informed of progress updates, with an implementable method
 * which can be used to handle changes in progress.
 */
public abstract class Progress{
	/** Maximum progress value */
	protected double max;
	/** The current progress value */
	protected double cur;
	/** The current status as a string. */
	protected String status;
	
	public Progress(double max){
		this.max = max;
	}
	
	/**
	 * Shorthand for current progress / max progress * 100
	 * @return the percentage of completeness (0-100)
	 */
	public final double getPercentage(){
		return (cur / max) * 100;
	}
	
	/**
	 * Called when the progress is changed on this class. This is called before
	 * the progress is actually set.
	 * @param newProgress the amount that will be the new progress
	 */
	public abstract void onChange(double newProgress);
	
	/**
	 * Sets the progress of the bar
	 * @param d
	 */
	public final void setProgress(double d){
		if(d < cur) throw new IllegalArgumentException("Progress can't go backwards.");
		if(d == cur) return; /* We don't throw updates when this occurs, but it's not necessarily an error */
		
		onChange(d);
		
		this.cur = d;
	}
	
	public final void addProgress(double n){
		setProgress(getProgress() + n);
	}
	
	public final double getProgress(){
		return cur;
	}
	
	public final boolean isFinished(){
		return cur >= max;
	}
	
	public final boolean isStarted(){
		return cur > 0;
	}
	
	public final String getStatus(){
		if(status == null) return "";
		else return status;
	}
	
	public final void setStatus(String s){
		this.status = s;
	}
}