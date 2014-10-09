package home.costin.util;

import java.util.*;

/**
 * This type was created in VisualAge.
 */
public class TimerThread extends Thread 
{
	int time=1000;
	LinkedList timerListeners= new LinkedList();
/**
 * TimerThread constructor comment.
 */
public TimerThread() {
	super();
}
/**
 * TimerThread constructor comment.
 * @param name java.lang.String
 */
public TimerThread(String name) {
	super(name);
}
/**
 * TimerThread constructor comment.
 * @param group java.lang.ThreadGroup
 * @param name java.lang.String
 */
public TimerThread(ThreadGroup group, String name) {
	super(group, name);
}
public synchronized void addTimeListener(TimerListener listener)
{
//		java.util.Enumeration e= timerListeners.elements();
//		while (e. hasMoreElements())
//			{
//				((TimerListener) e.nextElement()).timeElapsed();
//			}

	timerListeners.add(listener);

}
private synchronized void fireEvent()
{
	Iterator e= timerListeners.iterator();
	while (e. hasNext())
		{
			try
			{
			 ((TimerListener) e.next()).timeElapsed();
			}
			catch (Exception ex)
			{
				System.err.println(ex);
			}
		}
}
	public int getTimeInterval()
	{
		return time;
	}
public synchronized void  removeTimeListener(TimerListener listener)
{
	timerListeners.remove(listener);
}
public void run() {
	while (true)
	{
		try
		{
			sleep(time);
			fireEvent();
		}
		catch (InterruptedException ex)
		{
			break;
		}
	}
}
	public void setTimeInterval(int millis) 
	{
		time= millis;
	}
}
