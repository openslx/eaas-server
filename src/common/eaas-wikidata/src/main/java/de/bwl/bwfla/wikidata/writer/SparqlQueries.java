package de.bwl.bwfla.wikidata.writer;

public class SparqlQueries {
	static String softwareQidString = "";
	static String puidQidString = "";
	static String instanceProperty="P31";
	static String softwareInstanceItem="Q7397";
	static String pronomFileFormatProperty="P2748";

	public static String getSoftwareQidQuery(String softwareName) {
		String[] splitStr = softwareName.split("\\s+");
		softwareQidString = "PREFIX  wd:    <http://www.wikidata.org/entity/>\n"
				+ "PREFIX  wdt: <http://www.wikidata.org/prop/direct/>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX wikibase: <http://wikiba.se/ontology#>\n" + "PREFIX bd: <http://www.bigdata.com/rdf#>\n"
				+ "PREFIX schema: <http://schema.org/>\n" + "SELECT  distinct ?sLabel ?software\n" + "WHERE\n"
				+ "  { ?software wdt:"+instanceProperty+" wd:"+softwareInstanceItem+".\n" + "  ?software rdfs:label ?s. \n" + " FILTER (regex(str(?s),\".*"
				+ splitStr[0] + ".*\",'i')). \n" + "  FILTER (lang(?s) = \"en\").\n"
				+ "SERVICE wikibase:label {bd:serviceParam wikibase:language \"en\"}.\n" + "}\n" + "";
		return softwareQidString;

	}

	public static String getQidForPuid(String puid) {

		puidQidString = "PREFIX  wd:    <http://www.wikidata.org/entity/>\n"
				+ "PREFIX  wdt: <http://www.wikidata.org/prop/direct/>\n"
				+ "PREFIX wikibase: <http://wikiba.se/ontology#>\n" + "PREFIX bd: <http://www.bigdata.com/rdf#>\n"
				+ "SELECT  ?item ?itemLabel\n" + "WHERE\n" + "{ ?item wdt:"+pronomFileFormatProperty+" \"" + puid + "\" . \n"
				+ " SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\". }\n" + "  }\n" + "";
		return puidQidString;

	}
}
