package org.sirebug.filter;

import java.io.Serializable;

public class SirebugSession implements Serializable
{
	// IP Check (used only if IP Check is enabled)
	private boolean m_bIPCheckPerformed = false;
	private String m_sClientIP;
	private String m_sIPMask;
	private boolean m_bIPCheckSucceeded;

	// Current session parameters

	/**
	 * False if Hirebug is disabled for the current session.
	 * This happens when user clicks button "disable" on UI.
	 */
	private boolean m_bHirebugEnabled = true;

	private SessionHistory m_history;

	public SirebugSession()
	{
		m_history = new SessionHistory();
	}

	/**
	 * @return the history
	 */
	public SessionHistory getHistory()
	{
		return m_history;
	}

	/**
	 * @return the bIPCheckPerformed
	 */
	public boolean isIPCheckPerformed()
	{
		return m_bIPCheckPerformed;
	}

	/**
	 * @param asCheckPerformed the bIPCheckPerformed to set
	 */
	public void setIPCheckPerformed(boolean asCheckPerformed)
	{
		m_bIPCheckPerformed = asCheckPerformed;
	}

	/**
	 * @return the bIPCheckSucceeded
	 */
	public boolean isIPCheckSucceeded()
	{
		return m_bIPCheckSucceeded;
	}

	/**
	 * @param asCheckSucceeded the bIPCheckSucceeded to set
	 */
	public void setIPCheckSucceeded(boolean asCheckSucceeded)
	{
		m_bIPCheckSucceeded = asCheckSucceeded;
	}

	public boolean isHirebugEnabled()
	{
		return m_bHirebugEnabled;
	}

	public void setHirebugEnabled( boolean hirebugEnabled )
	{
		m_bHirebugEnabled = hirebugEnabled;
	}
}

