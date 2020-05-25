package de.bwl.bwfla.wikidata.reader;

import java.util.logging.Logger;

public class QIDComparator {

    private static final Logger log = Logger.getLogger(QIDComparator.class.getName());

    public static boolean checkIfSubclass(String objQID, String subQID){
        return WikiRead.checkExistence(objQID, SparqlQueries.SUBCLASSOF, subQID);
    }


}
