package de.bwl.bwfla.common.utils;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class EaasBuildInfo
{
	private static final Properties PROPERTIES = EaasBuildInfo.load("/eaas-version.properties");

	private static final String PROPERTY_COMMIT_ID = "git.commit.id";


	public static String getVersion()
	{
		return EaasBuildInfo.get(PROPERTY_COMMIT_ID);
	}

	private static String get(String key)
	{
		final String value = PROPERTIES.getProperty(key);
		return (value != null) ? value : "UNKNOWN";
	}

	private static Properties load(String url)
	{
		final Properties properties = new Properties();

		try (InputStream istream = EaasBuildInfo.class.getClassLoader().getResourceAsStream(url)) {
			if (istream != null) {
				properties.load(istream);
			}

			var commit = properties.getProperty(PROPERTY_COMMIT_ID);
			if (commit != null) {
				commit = commit.replace("-false", "")
						.replace("-true", "-dirty");

				properties.setProperty(PROPERTY_COMMIT_ID, commit.toUpperCase());
			}
		}
		catch (Throwable error) {
			Logger.getLogger(EaasBuildInfo.class.getSimpleName())
					.log(Level.WARNING, "Loading build-info properties failed", error);
		}

		return properties;
	}
}
