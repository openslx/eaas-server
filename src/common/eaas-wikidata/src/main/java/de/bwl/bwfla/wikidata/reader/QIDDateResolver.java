package de.bwl.bwfla.wikidata.reader;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

public class QIDDateResolver {
    protected static final Logger log = Logger.getLogger("QIDDateResolver");

    public static Date findInception(String qID) throws ParseException {
       return WikiRead.resolveInceptionDate(qID);
    }

}
