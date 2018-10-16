package de.bwl.bwfla.common.utils;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbValidator;
import gov.loc.marc21.slim.*;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

import java.util.HashMap;

public class MARC21Xml {

    CollectionType collection;

    private static CollectionType fromValue(File marcfile) throws JAXBException, FileNotFoundException {
        JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        JAXBElement<CollectionType> result= (JAXBElement<CollectionType>)unmarshaller
                .unmarshal(new StreamSource(new FileReader(marcfile)));
        JaxbValidator.validate(result);
        return result.getValue();
    }

    public MARC21Xml(File fromFile) throws IOException, JAXBException, BWFLAException {
        collection = fromValue(fromFile);

        if(collection.getRecord().size() == 0)
            throw new BWFLAException("empty collection");
    }

    public MARC21Xml(WebTarget target) throws BWFLAException {

        Invocation.Builder restRequest = target.request();
        Response response = restRequest.get();

        // System.out.println("request marc data: " + target.getUri());
        switch (Response.Status.fromStatusCode(response.getStatus())) {
            case OK:
                collection = response.readEntity(CollectionType.class);
                break;

            default:
               throw new BWFLAException("Request failed: " + response.getStatus());
        }
        response.close();

    }

    public HashMap<String,String> getTag(String tag)
    {
        if(collection.getRecord().size() == 0)
            return new HashMap<>();

        RecordType record = collection.getRecord().get(0);

        HashMap<String, String> result = new HashMap<>();;
        for(DataFieldType field : record.getDatafield())
        {
            if(!field.getTag().equals(tag))
                continue;

            for(SubfieldatafieldType sub : field.getSubfield())
            {
                if(result.get(sub.getCode()) != null)
                    result.put(sub.getCode(), result.get(sub.getCode()+ " " + sub.getValue()));
                else
                    result.put(sub.getCode(), sub.getValue());
            }
        }
//        if(result.size() == 0)
//            System.out.println("no data found for tag: " +  tag);
//        else
//            System.out.println("result " + result.entrySet());
        return result;
    }




}
