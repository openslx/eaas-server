//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.bwl.bwfla.wikidata.reader;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import java.util.ArrayList;
import java.util.Iterator;

import static de.bwl.bwfla.wikidata.reader.WikiRead.wikidataEndPoint;

public class Utils {
	public Utils() {
	}

	protected static ArrayList<String> fromRdfnodeToQID(ArrayList<RDFNode> nodes) {
		ArrayList<String> qids = new ArrayList();
		Iterator var2 = nodes.iterator();
		while(var2.hasNext()) {
			qids.add(fromRdfnodeToQID((RDFNode)var2.next()));
		}
		return qids;
	}
	protected static String fromRdfnodeToQID(RDFNode node) {
		String qidLink = node.toString();
		return qidLink.substring(qidLink.indexOf("entity") + 7);
	}
	protected static String reformatLabel(RDFNode node) {
		String qidLink = node.toString();
		return qidLink.substring(0, qidLink.indexOf("@"));
	}
	protected static ResultSet getQueryResult(String queryString){
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.sparqlService(wikidataEndPoint, query);
		return qExe.execSelect();
	}
}
