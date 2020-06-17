package de.bwl.bwfla.wikidata.reader;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import de.bwl.bwfla.wikidata.reader.entities.FileFormats;
import de.bwl.bwfla.wikidata.reader.entities.RelatedQIDS;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;



public class WikiRead {

	protected static final Logger log = Logger.getLogger("WikiRead");

	static String wikidataEndPoint = "https://query.wikidata.org/sparql";

	/*
	 * This function returns the list of readable and writeable softwares based
	 * on Pronom ID
	 */
	public static ArrayList<ArrayList<String>> readWithPronomId(String PronomId, String language)  {
		QuerySolution qs;
		// Fetch the query string from sparqlQueries Class
		String queryString = SparqlQueries.pronomQuerySoftwareLabel(PronomId, language);
		ArrayList<ArrayList<String>> dataList = new ArrayList<ArrayList<String>>();

		// Create ArrayLists to store the readable and writable softwares
		for (int i = 0; i < 2; i++) {
			ArrayList<String> list = new ArrayList<String>();
			dataList.add(list);
		}
			Query query = QueryFactory.create(queryString);
			QueryExecution qExe = QueryExecutionFactory.sparqlService(wikidataEndPoint, query);
			ResultSet results = qExe.execSelect();
			while (results.hasNext()) {
				qs = results.nextSolution();

				// Fetch the result of query execution
				RDFNode readSoft = qs.get("ReadSoftware");
				RDFNode readSoftLabel = qs.get("ReadSoftwareLabel");
				RDFNode writeSoftware = qs.get("WriteSoftware");
				RDFNode writeSoftwareLabel = qs.get("WriteSoftwareLabel");

				String readStr = parseRdfNode(readSoft, readSoftLabel);
				String writeStr = parseRdfNode(writeSoftware, writeSoftwareLabel);

				if(readStr != null)
					dataList.get(0).add(readStr);
				if(writeStr != null)
					dataList.get(1).add(writeStr);
			}
		return dataList;
	}

	public static FileFormats getFileFormats(String qID){
		ResultSet results = Utils.getQueryResult(SparqlQueries.getFileFormatProperty(qID));
		ArrayList<String> readFormats = new ArrayList<>();
		ArrayList<String> writeFormats = new ArrayList<>();
		while(results.hasNext()){
			QuerySolution querySolution = results.nextSolution();
			addIfNotNull(readFormats, querySolution, "readPronom");
			addIfNotNull(readFormats, querySolution, "writePronom");
		}
		return new FileFormats(readFormats, writeFormats);
	}

	private static String parseRdfNode(RDFNode software, RDFNode label) {
		if(software != null && label != null)
			return Utils.fromRdfnodeToQID(software)  + "," + Utils.reformatLabel(label);
		else
			return null;
	}

	private static void addIfNotNull(ArrayList<String> list, QuerySolution querySolution, String var){
		RDFNode solution = querySolution.get(var);
		if(solution!= null)
			list.add(solution.toString());
	}

	protected static boolean checkExistence(String subject, String predicate, String object)  {
		QuerySolution qs;
		// Fetch the query string from sparqlQueries Class
		String queryString = SparqlQueries.checkIfStatementExist(subject, predicate,object);
		return executeAskQuery(queryString);
	}

	protected static Date resolveInceptionDate(String qID) throws ParseException {
		// Fetch the query string from sparqlQueries Class
		String queryString = SparqlQueries.getInceptionDate(qID);
		ResultSet results = executeSelectQuery(queryString);
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
			String resultStr =  results.next().getLiteral("inception").toString();
			return fmt.parse(resultStr);
	}

	protected static RelatedQIDS findFollowingAndFollowedQIDS(String qID) {
		// Fetch the query string from sparqlQueries Class
		String queryString = SparqlQueries.getPredecessorAndSuccessor(qID);
		ResultSet results = executeSelectQuery(queryString);
		RelatedQIDS relatedQIDS = new RelatedQIDS(qID);
		while (results.hasNext()) {
			QuerySolution solution = results.next();
			relatedQIDS.addFollowingQID(Utils.fromRdfnodeToQID(solution.get("follows")));
			relatedQIDS.addFollowedQID(Utils.fromRdfnodeToQID(solution.get("followedBy")));
		}
		return relatedQIDS;
	}

	/**
	 * helpres
	 */
	private static ResultSet executeSelectQuery(String queryString) {
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.sparqlService(wikidataEndPoint, query);
		return qExe.execSelect();
	}
	private static boolean executeAskQuery(String queryString) {
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.sparqlService(wikidataEndPoint, query);
		return qExe.execAsk();
	}




	/**
	 * TODO remove or rework remaining student code.
	 */



	/* This function displays the properties of a Software Item */
	public static void getProperties(String qID) {
		String queryString = SparqlQueries.propertiesQuery(qID);
		HashMap<String, ArrayList<RDFNode>> propertyMap = new HashMap<String, ArrayList<RDFNode>>();
		ArrayList<ArrayList<RDFNode>> dataList = new ArrayList<ArrayList<RDFNode>>();
		// Create ArrayLists to store properties' values
		for (int i = 0; i < 15; i++) {
			ArrayList<RDFNode> list = new ArrayList<RDFNode>();
			dataList.add(list);
		}
		try {
			QuerySolution qs;
			Query query = QueryFactory.create(queryString);
			QueryExecution qExe = QueryExecutionFactory.sparqlService(wikidataEndPoint, query);
			ResultSet results = qExe.execSelect();
			while (results.hasNext()) {
				qs = results.nextSolution();
				dataList.get(0).add(qs.get("itemLabel"));
				dataList.get(1).add(qs.get("licenseLabel"));
				dataList.get(2).add(qs.get("versionLabel"));
				dataList.get(3).add(qs.get("websiteLabel"));
				dataList.get(4).add(qs.get("developerLabel"));
				dataList.get(5).add(qs.get("softwareLabel"));
				dataList.get(6).add(qs.get("fileExtensionLabel"));
				dataList.get(7).add(qs.get("platformLabel"));
				dataList.get(8).add(qs.get("programmingLanguageLabel"));
				dataList.get(9).add(qs.get("operatingSystemLabel"));
				dataList.get(10).add(qs.get("mediaTypeLabel"));
				dataList.get(11).add(qs.get("userManualLinkLabel"));
				dataList.get(12).add(qs.get("bugTrackingSystemLabel"));
				dataList.get(13).add(qs.get("readableFileFormatLabel"));
				dataList.get(14).add(qs.get("writableFileFormatLabel"));
			}


			// Remove null values from the list
			removeNull(dataList);



			propertyMap.put("Label", dataList.get(0));
			propertyMap.put("License", dataList.get(1));
			propertyMap.put("Version", dataList.get(2));
			propertyMap.put("Website", dataList.get(3));
			propertyMap.put("Developer", dataList.get(4));
			propertyMap.put("Depends On Software", dataList.get(5));
			propertyMap.put("FileExtension", dataList.get(6));
			propertyMap.put("Platform", dataList.get(7));
			propertyMap.put("Programming Laguages", dataList.get(8));
			propertyMap.put("Operating Systems", dataList.get(9));
			propertyMap.put("Media Types", dataList.get(10));
			propertyMap.put("User Manuals", dataList.get(11));
			propertyMap.put("Bug Tracking", dataList.get(12));
			propertyMap.put("Readable File Formats", dataList.get(13));
			propertyMap.put("Writeable File Formats", dataList.get(14));

			Set keys = propertyMap.keySet();

			for (Iterator i = keys.iterator(); i.hasNext();) {
				String key = (String) i.next();
				ArrayList<RDFNode> value = propertyMap.get(key);

				Set<RDFNode> set = new HashSet<RDFNode>(value);

				System.out.println(" " + key + " = " + set);
			}

		} catch (Exception e) {
			if (e.getMessage().equals("HttpException: 404 Not Found")) {
				System.out.println("The Sparql end point is not correct");
			}

			if (e.getMessage().contains("Unresolved prefixed name")) {
				System.out.println(e.getMessage());
			}

		}

	}

	private static void removeNullFromString(ArrayList<ArrayList<String>> dataList) {
		for (int i = 0; i < dataList.size(); i++) {
			dataList.get(i).removeAll(Collections.singleton("null,null"));
		}
	}

	/*
	 * This function returns the list of readable and writeable softwares based
	 * on LocFDD and Pronom ID
	 */
	public static ArrayList<ArrayList<RDFNode>> readWithLocfddIdPronomId(String LocfddId, String PronomId) {
		QuerySolution qs;

		// Fetch the query string from sparqlQueries Class
		String queryString = SparqlQueries.pronomlocfddQuery(LocfddId, PronomId);

		ArrayList<ArrayList<RDFNode>> dataList = new ArrayList<ArrayList<RDFNode>>();
		// Create ArrayLists to store the readable and writable softwares
		for (int i = 0; i < 2; i++) {
			ArrayList<RDFNode> list = new ArrayList<RDFNode>();
			dataList.add(list);
		}

		try {
			ResultSet results = Utils.getQueryResult(queryString);
			while (results.hasNext()) {
				// Fetch the result of query execution
				qs = results.nextSolution();
				dataList.get(0).add(qs.get("ReadSoftware"));
				dataList.get(1).add(qs.get("WriteSoftware"));
			}
			// Remove null values from lists
			removeNull(dataList);

		} catch (Exception e) {
			if (e.getMessage().equals("HttpException: 404 Not Found")) {
				System.out.println("The Sparql end point is not correct");
			}

			if (e.getMessage().contains("Unresolved prefixed name")) {
				System.out.println(e.getMessage());
			}

		}
		return dataList;
	}

	/*
	 * This function returns the list of readable and writeable softwares based
	 * on LocFDD ID
	 */
	public static ArrayList<ArrayList<RDFNode>> readWithLocfddId(String LocfddId) {
		QuerySolution qs;

		// Fetch the query string from sparqlQueries Class
		String queryString = SparqlQueries.locfddQuery(LocfddId);

		ArrayList<ArrayList<RDFNode>> dataList = new ArrayList<ArrayList<RDFNode>>();
		// Create ArrayLists to store properties' values
		for (int i = 0; i < 2; i++) {
			ArrayList<RDFNode> list = new ArrayList<RDFNode>();
			dataList.add(list);
		}
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qExe = QueryExecutionFactory.sparqlService(wikidataEndPoint, query);

			ResultSet results = qExe.execSelect();

			while (results.hasNext()) {
				qs = results.nextSolution();
				// Fetch the result of query execution
				dataList.get(0).add(qs.get("ReadSoftware"));
				dataList.get(1).add(qs.get("WriteSoftware"));
			}
			// Remove the null values from the lists
			removeNull(dataList);

		} catch (Exception e) {
			if (e.getMessage().equals("HttpException: 404 Not Found")) {
				System.out.println("The Sparql end point is not correct");
			}

			if (e.getMessage().contains("Unresolved prefixed name")) {
				System.out.println(e.getMessage());
			}

		}

		return dataList;
	}

	private static void removeNull(ArrayList<ArrayList<RDFNode>> dataList) {
		for (int i = 0; i < dataList.size(); i++) {

			dataList.get(i).removeAll(Collections.singleton(null));
		}
	}
}
