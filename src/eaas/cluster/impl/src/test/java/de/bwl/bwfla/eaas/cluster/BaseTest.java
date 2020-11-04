package de.bwl.bwfla.eaas.cluster;

import java.util.logging.Logger;


public class BaseTest
{
	private static final String LOG_PREFIX = "##### ";
	private static final String LOG_SUFFIX = " #####";

	protected final Logger log = Logger.getLogger(this.getClass().getName());


	public void info(String message)
	{
		log.info(LOG_PREFIX + message + LOG_SUFFIX);
	}
}
