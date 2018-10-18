/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.shibboleth.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

public class ShibUtil {

	private final static Logger logger = LoggerFactory.getLogger(ShibUtil.class);

	/**
	 * Load the given property file.
	 * 
	 * @param filename
	 * @return
	 */
	public static Properties loadProperties(String filename) {
		Properties properties = new Properties();
		try (InputStream in = ShibUtil.class.getClassLoader().getResourceAsStream(filename)) {

			if (in == null) {
				logger.error("Could not find properties file: " + filename);
				return null;
			}

			properties.load(in);
		} catch (IOException e) {
			logger.error("Could not load properties file " + filename, e);
		}

		return properties;
	}

	public static SamlSpConfigurationEntity loadSpConfigFromXML(String filename) {
		SamlSpConfigurationEntity sp = new SamlSpConfigurationEntity();

		try (InputStream in = ShibUtil.class.getClassLoader().getResourceAsStream(filename)) {
			
			if (in == null) {
				logger.info(filename + " does not exist. Shibboleth SP disabled.");
				sp.setEnabled(false);
				sp.setHostNameList(Lists.newArrayList("localhost", "127.0.0.1"));
				return sp;
			}
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(in);
			
			sp.setEnabled(Boolean.parseBoolean(doc.getElementsByTagName("enabled").item(0).getFirstChild().getNodeValue()));
			sp.setId(Long.parseLong(doc.getElementsByTagName("id").item(0).getFirstChild().getNodeValue()));
			sp.setEntityId(doc.getElementsByTagName("entity_id").item(0).getFirstChild().getNodeValue());
			sp.setAcs(doc.getElementsByTagName("acs").item(0).getFirstChild().getNodeValue());
			sp.setEcp(doc.getElementsByTagName("ecp").item(0).getFirstChild().getNodeValue());
			sp.setCertificate(doc.getElementsByTagName("certificate").item(0).getFirstChild().getNodeValue());
			sp.setPrivateKey(doc.getElementsByTagName("private_key").item(0).getFirstChild().getNodeValue());

			LinkedList<String> hostnames = new LinkedList<>();
			NodeList hostnamesXML = doc.getElementsByTagName("hostname");
			for (int i = 0; i < hostnamesXML.getLength(); i++) {
				hostnames.add(hostnamesXML.item(i).getFirstChild().getNodeValue());
			}
			sp.setHostNameList(hostnames);

		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}

		return sp;
	}

}
