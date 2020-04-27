package de.bwl.bwfla.wikidata.writer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.wikidata.wdtk.wikibaseapi.apierrors.EditConflictErrorException;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;
import org.wikidata.wdtk.wikibaseapi.apierrors.NoSuchEntityErrorException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import javax.json.Json;
import javax.json.JsonReader;


public class WikiWrite {
	public static String qID;
	public static RDFNode puidQid = null;
	public static String result;
	static String wikidataSparqlEndPoint = "https://query.wikidata.org/sparql";
	static String wikidataAPIEndPoint = "https://www.wikidata.org/w/api.php";
	static String softwareInstanceItem = "Q7397";
	static String instanceOf = "P31";
	static String readableFileFormatProperty = "P1072";

	/*
	 * This function returns 2 lists with "Software names" and "QIDs" of
	 * softwares
	 */
	public static ArrayList<ArrayList<RDFNode>> getTheSoftwareQid(String softwareName) {
		QuerySolution qs;
		ArrayList<ArrayList<RDFNode>> dataList = new ArrayList<ArrayList<RDFNode>>();
		// Create ArrayLists to store Software names and their QIDs
		for (int i = 0; i < 2; i++) {
			ArrayList<RDFNode> list = new ArrayList<RDFNode>();
			dataList.add(list);
		}
		// Fetch the query string from SparqlQueries Class
		String queryString = SparqlQueries.getSoftwareQidQuery(softwareName);
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qExe = QueryExecutionFactory.sparqlService(wikidataSparqlEndPoint, query);
			ResultSet results = qExe.execSelect();

			while (results.hasNext()) {
				qs = results.nextSolution();
				// Fetch the name of softwares and their QIDs
				dataList.get(0).add(qs.get("software"));
				dataList.get(1).add(qs.get("sLabel"));
			}
			// Remove null values from the lists
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

	// This function returns the QID for the "File Format" based on the PUID
	public static String fetchQidForPuid(String puid) {
		QuerySolution qs;

		// Fetch the query string from sparqlQueries Class
		String queryString = SparqlQueries.getQidForPuid(puid);
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qExe = QueryExecutionFactory.sparqlService(wikidataSparqlEndPoint, query);
			ResultSet results = qExe.execSelect();
			if (results.hasNext()) {
				qs = results.nextSolution();
				// Get QID of the file format
				puidQid = qs.get("item");
			} else {

				return "The PUID entered is incorrect";
			}
		}

		catch (Exception e) {
			if (e.getMessage().equals("HttpException: 404 Not Found")) {
				System.out.println("The Sparql end point is not correct");
			}
			if (e.getMessage().contains("Unresolved prefixed name")) {
				System.out.println(e.getMessage());
			}

		}
		return puidQid.toString();
	}

	/*
	 * This function adds a property "Readable file format-P1072" in Wikidata
	 * for a Software item
	 */
	public static String addReadableFileFormat(String softwareQID, String puidQid)
			throws MediaWikiApiErrorException, EditConflictErrorException, NoSuchEntityErrorException {

		String tokenWithValue, output;
		ClientResponse<String> response = null;
		// Adding readable file format statement to the software
		SetValues createItem = new SetValues(puidQid.toString());
		try {
			tokenWithValue = createItem.generateTokenClaim();

			/*
			 * The URI to create a statement for the "Software item" for the
			 * property "Readable file format"
			 */

			ClientRequest request = new ClientRequest(wikidataAPIEndPoint + "?action=wbcreateclaim&entity="
					+ softwareQID + "&property=" + readableFileFormatProperty + "&snaktype=value&format=json");
			request.accept("application/x-www-form-urlencoded");
			request.body("application/x-www-form-urlencoded", tokenWithValue);

			response = request.post(String.class);
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new ByteArrayInputStream(response.getEntity().getBytes())));

			while ((output = br.readLine()) != null) {
				// if uri is wrong
				if (output.contains("Page not found")) {
					System.out.println("Could not find the page, please check the URI");
				}
				// if the action is wrong in uri
				else if (output.contains("unknown_action")) {
					System.out.println("Please verify the action in the URI");
				}
				// if the property is wrong in uri
				else if (output.contains("invalid-snak")) {
					System.out.println("Please verify the value of property in URI");
				} else if (output.contains("success")) {
					result = "File format added to Software Item";
				} else {
					result = "The QUID/s could not be found in Wikidata";
				}
			}

		}

		catch (UnknownHostException e) {
			System.out.println("Please verify the host URL: " + e.getMessage());

		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return result;
	}

	/*
	 * This function creates a Software item if they don't already exist in
	 * Wikidata
	 */
	public static String createSoftwareItem(String softwareName, String description)
			throws MediaWikiApiErrorException, EditConflictErrorException, NoSuchEntityErrorException {
		String tokenWithData, output, tokenWithClaim;

		/*
		 * Set values for language , the name of the software and its
		 * description
		 */
		SetValues createItem = new SetValues("en", softwareName, description);
		// Fetch the values with token in URLEncoded form
		try {
			tokenWithData = createItem.generateTokenData();

			// The URI is set to create an item
			ClientRequest request = new ClientRequest(
					wikidataAPIEndPoint + "?action=wbeditentity&new=item&format=json");
			request.accept("application/x-www-form-urlencoded");

			// The token along with the values are set in the body
			request.body("application/x-www-form-urlencoded", tokenWithData);
			ClientResponse<String> response = request.post(String.class);
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new ByteArrayInputStream(response.getEntity().getBytes())));

			while ((output = br.readLine()) != null) {
                //if uri is wrong
				if (output.contains("Page not found")) {
					return "Could not find the page, please check the URI";
				} 
				//if the action is wrong in uri
				else if (output.contains("unknown_action")) {
					return "Please verify the action in the URI";
				} 
				//if value 'new' is not set right in the uri
				else if (output.contains("unknown_new")) {
					return "Please verify the value of 'new' in the URI";
				} else if (output.contains("success"))
					/*
					 * Parse the Json output and fetch the QID of the new
					 * software item
					 */
					qID = outputItemDetails(output);
				else
					return "The Software item with the name and description already exists";

			}

			/*
			 * adding the "Instance" for software item as per
			 * "Notability Criteria" of Wikidata
			 */
			// The QID "Q7397" stands for item "Software" in Wikidata
			SetValues createInstance = new SetValues(softwareInstanceItem);
			tokenWithClaim = createInstance.generateTokenClaim();
			ClientRequest requestInstance = new ClientRequest(wikidataAPIEndPoint + "?action=wbcreateclaim&entity="
					+ qID + "&property=" + instanceOf + "&snaktype=value&format=json");
			requestInstance.accept("application/x-www-form-urlencoded");
			requestInstance.body("application/x-www-form-urlencoded", tokenWithClaim);
			ClientResponse<String> responseInstance = requestInstance.post(String.class);

			String tf = responseInstance.getEntity();
			BufferedReader brf = new BufferedReader(
					new InputStreamReader(new ByteArrayInputStream(responseInstance.getEntity().getBytes())));
			while ((output = brf.readLine()) != null) {
                //if uri is wrong
				if (output.contains("Page not found")) {
					return "Could not find the page, please check the URI";
				}
				//if action is wrong in the uri
				if (output.contains("unknown_action")) {
					return "Please verify the action in the URI";
				}
				//if the property is wrong in the uri
				if (output.contains("invalid-snak")) {
					return "Please verify the value of property in URI";
				}

			}
		} catch (UnsupportedEncodingException e) {
			System.out.println("Your JRE does not support UTF-8 encoding");
		} catch (UnknownHostException e) {
			System.out.println("Please verify the host URL: " + e.getMessage());

		} catch (Exception e) {
			System.out.println(e.toString());
		}
		// Return the QID of the newly created software item
		return qID;

	}

	// This function parses the Json string
	static String outputItemDetails(String output) {

		// TODO: use a more efficient streaming-parser here
		final JsonReader reader = Json.createReader(new StringReader(output));
		return reader.readObject()
				.getJsonObject("entity")
				.getString("id");
	}

	// This function removes null values from lists
	private static void removeNull(ArrayList<ArrayList<RDFNode>> dataList) {
		for (int i = 0; i < dataList.size(); i++) {

			dataList.get(i).removeAll(Collections.singleton(null));
		}
	}
}
