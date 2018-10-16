package de.bwl.bwfla.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EaasBuildInfo {
	public static String getVersion() {
		Properties prop = new Properties();
		InputStream in = EaasBuildInfo.class.getClassLoader().getResourceAsStream("/eaas-version.properties");
		if(in == null)
			return "no build version: file not found";
		try {
			prop.load(in);
		} catch (IOException e) {

			e.printStackTrace();
			return "no build version";
		}

		try {
			in.close();
		} catch (IOException e) {
			return "no build version";
		}
		String ret = prop.getProperty("git.commit.id");
		if(ret == null)
			return "no build version";
		return ret;
	}
}
