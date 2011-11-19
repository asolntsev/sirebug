package org.sirebug.filter;

import org.sirebug.result.ThreadExecutionHistory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SessionHistory implements Serializable
{
	protected static final int MAX_HISTORY_LENGTH = 30;
	
	private int minStep;
	private int maxStep;
	private final Map<String, ThreadExecutionHistory> clicks;

	protected SessionHistory()
	{
		minStep = 0;
		maxStep = 0;
		clicks = new HashMap<String, ThreadExecutionHistory>();
	}

	public int addThreadExecutionHistory(ThreadExecutionHistory t)
	{
		// Add new step
		maxStep++;
		clicks.put(String.valueOf(maxStep), t);

		// Remove oldest steps to reduce memory usage
		while (maxStep > MAX_HISTORY_LENGTH+ minStep)
		{
			clicks.remove( String.valueOf(minStep) );
			minStep++;
		}
		return maxStep;
	}

	public String toString()
	{
		return ""; // make it invisible on "show_session" screen
	}

	public ThreadExecutionHistory getThreadExecutionHistory(int step)
	{
		return getThreadExecutionHistory(String.valueOf(step));
	}

	public ThreadExecutionHistory getThreadExecutionHistory(String sStep)
	{
		return (ThreadExecutionHistory) clicks.get(sStep);
	}

	/**
	 * @return the nMaxStep
	 */
	public int getMaxStep()
	{
		return maxStep;
	}

	/**
	 * @return the nMinStep
	 */
	public int getMinStep()
	{
		return minStep;
	}
}
