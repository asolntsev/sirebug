package org.sirebug.filter;

import org.sirebug.result.ThreadExecutionHistory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SessionHistory implements Serializable
{
	protected static final int MAX_HISTORY_LENGTH = 30;
	
	private int m_nMinStep;
	private int m_nMaxStep;
	private final Map<String, ThreadExecutionHistory> m_clicks;

	protected SessionHistory()
	{
		m_nMinStep = 0;
		m_nMaxStep = 0;
		m_clicks = new HashMap<String, ThreadExecutionHistory>();
	}

	public int addThreadExecutionHistory(ThreadExecutionHistory t)
	{
		// Add new step
		m_nMaxStep++;
		m_clicks.put(String.valueOf(m_nMaxStep), t);

		// Remove oldest steps to reduce memory usage
		while (m_nMaxStep > MAX_HISTORY_LENGTH+m_nMinStep)
		{
			m_clicks.remove( String.valueOf(m_nMinStep) );
			m_nMinStep++;
		}
		return m_nMaxStep;
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
		return (ThreadExecutionHistory) m_clicks.get(sStep);
	}

	/**
	 * @return the nMaxStep
	 */
	public int getMaxStep()
	{
		return m_nMaxStep;
	}

	/**
	 * @return the nMinStep
	 */
	public int getMinStep()
	{
		return m_nMinStep;
	}
}
