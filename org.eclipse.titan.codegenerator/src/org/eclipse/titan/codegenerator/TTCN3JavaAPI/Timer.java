/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   
 *   Keremi, Andras
 *   Eros, Levente
 *   Kovacs, Gabor
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.TTCN3JavaAPI;

public class Timer {

	/** start time. Number of milliseconds since 1970-01-01 00:00:00 */
	public long starttime;
	
	/** timer delay between start time and timer event in milliseconds */
	public long timetorun;
	
	/** true if and only if timer is running */
    public boolean running;
    
    /** true if and only if timer delay is set */
    private boolean set;

    public Timer(FLOAT t) {
        set(t);
    }
    
    public Timer(){
    	
    }
    
    public void set(FLOAT t){
    	set0(t.value);
    }
    
	/**
	 * Sets timer delay between start time and timer event
	 * @param t timer delay in seconds
	 */
    private void set0(double t) {
        timetorun = (long) (t * 1000.0);
        running = false;
        set = true;
    }
	
    public void start(){
        if (!set) {
        	System.out.println("Timer not set.");
        	return;
        } else {
            starttime = System.currentTimeMillis();
            running = true;
        }
    }
    
    public void stop() {
    	running = false;
    }
    
    public boolean timeout(){
    	if (!set) {
        	System.out.println("Timer not set.");
        	return false;
        } else {
            if (running){
                return (System.currentTimeMillis() - starttime > timetorun);
            }
            else
                return false;
        }
    }
    
    public FLOAT read(){
    	return new FLOAT(read0());
    }
    
    private double read0(){
    	if(!set) return 0.0;
    	else if(running){
    		double retv = (double)(timetorun-(System.currentTimeMillis() - starttime))/1000.0;
    		return retv>0.0 ? retv : 0.0;
    	}
    	else return 0.0;
    }
    
    public boolean running() throws TTCNJavaAPIException{
    	return running&&!timeout();
    }

}
