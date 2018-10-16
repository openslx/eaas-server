package pronom.nationalarchives.gov.impl;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import pronom.nationalarchives.gov.xsd.FileFormatType;
import pronom.nationalarchives.gov.xsd.SignatureFileType;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PronomParser
{
	public static SignatureFileType unmarshalPronomXml() throws BWFLAException
	{
		Configuration config = ConfigurationProvider.getConfiguration();
		String pronomFilePath = config.get("imageclassifier.pronom_file_path");

		if(pronomFilePath == null)
			throw new BWFLAException("pronom signature file not configured");

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance("pronom.nationalarchives.gov.xsd");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			JAXBElement<SignatureFileType> signatureFileTypeJAXBElement
					= (JAXBElement<SignatureFileType>) unmarshaller.unmarshal(
					new File(pronomFilePath));
			return signatureFileTypeJAXBElement.getValue();
		}
		catch(JAXBException e) {
			throw new BWFLAException("unmarshal PronomXML failed: ", e);
		}
	}

	public static Map<String, String> buildPronomIdToNameIndex() throws BWFLAException
	{
		final SignatureFileType signature = PronomParser.unmarshalPronomXml();
		SignatureFileType.FileFormatCollection fileFormatCollection = signature.getFileFormatCollection();
		if (fileFormatCollection == null)
			throw new BWFLAException("Invalid file-format-collection in pronom signatures!");

		List<FileFormatType> fileFormatTypes = fileFormatCollection.getFileFormat();
		if (fileFormatTypes == null)
			throw new BWFLAException("Invalid file-format-types in pronom signatures!");

		final Map<String, String> index = new HashMap<String, String>();
		for (FileFormatType type : fileFormatTypes)
			index.put(type.getPUID(), type.getName());

		return index;
	}
}
