package de.bwl.bwfla.wikidata.reader;

import de.bwl.bwfla.wikidata.reader.config.Config;

public class SparqlQueries {

	static final private String PREFIXES = "PREFIX wd: <http://www.wikidata.org/entity/>\n" +
			"PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
			"PREFIX wikibase: <http://wikiba.se/ontology#>\n" +
			"PREFIX p: <http://www.wikidata.org/prop/>\n" +
			"PREFIX ps: <http://www.wikidata.org/prop/statement/>\n" +
			"PREFIX pq: <http://www.wikidata.org/prop/qualifier/>\n" +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
			"PREFIX bd: <http://www.bigdata.com/rdf#>\n";

	public static final String SUBCLASSOF = "P279";
	private final static String INCEPTION_PROPERTY = "P571";
	private final static String FOLLOWS = "P155";
	private final static String FOLLOWED_BY = "P156";
	private static final String pronomFileFormatProperty = "P2748";
	private static final String readableFileFormatProperty = "P1072";
	private static final String writeableFileFormatProperty = "P1073";
	private static final String locFDDIDProperty = "P3266";
	private static final String licenseProperty = "P275";
	private static final String officialWebsiteProperty = "P856";
	private static final String dependsOnSoftwareProperty = "P1547";
	private static final String programmingLanProperty = "P277";
	private static final String mediaTypeProperty = "P1163";
	private static final String bugTrackingSystemProperty = "P1401";
	private static final String softwareVersionProperty = "P348";
	private static final String developerProperty = "P178";
	private static final String fileExtensionProperty = "P1195";
	private static final String platformProperty = "P400";
	private static final String operatingSystemProperty = "P306";
	private static final String userManualProperty = "P2078";
	private static final String LANG = " SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE]," + Config.getLANGUAGE() + "\". }";

	public static String getInceptionDate(String qID) {
		return PREFIXES +
				"SELECT ?inception \n" +
				"WHERE \n" +
				"{\n" +
				"wd:" + qID + " wdt:" + INCEPTION_PROPERTY + " ?inception.\n" +
				LANG +
				"}";
	}

	public static String getPredecessorAndSuccessor(String qID) {
		return PREFIXES +
				"SELECT ?followedBy ?follows \n" +
				"WHERE \n" +
				"{\n" +
				"wd:" + qID + " wdt:" + FOLLOWED_BY + " ?followedBy.\n" +
				"wd:" + qID + " wdt:" + FOLLOWS + " ?follows.\n" +
				LANG +
				"}";
	}

	public static String checkIfStatementExist(String subject, String predicate, String object) {
		return PREFIXES +
				"ASK\n" +
				"WHERE \n" +
				"{\n" +
				"wd:" + subject + " wdt:" + predicate + " wd:" + object + ".\n" +
				"}";
	}


	//TODO remove or rework following queries

	// Query to fetch the read/write softwares for a given PRONOM ID
	public static String pronomQuery(String pronomVal) {
		return "PREFIX  wd:    <http://www.wikidata.org/entity/>\n"
				+ "PREFIX  wdt: <http://www.wikidata.org/prop/direct/>\n"
				+ "PREFIX wikibase: <http://wikiba.se/ontology#>\n" + "PREFIX bd: <http://www.bigdata.com/rdf#>\n"
				+ "SELECT  ?FileFormat ?FileFormatLabel ?ReadSoftware ?WriteSoftware\n" + "WHERE\n"
				+ "  { ?FileFormat wdt:" + pronomFileFormatProperty + " \"" + pronomVal + "\" .\n"
				+ "    {?ReadSoftware wdt:" + readableFileFormatProperty + " ?FileFormat } \n" + "    UNION \n"
				+ "    {?WriteSoftware wdt:" + writeableFileFormatProperty + " ?FileFormat } \n"
				+ "SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\" }\n" + "  }\n" + "";
	}

	// Query to fetch the read/write softwares for a given PRONOM ID
	public static String pronomQuerySoftwareLabel(String pronomVal, String language) {
		return "PREFIX  wd:    <http://www.wikidata.org/entity/>\n"
				+ "PREFIX  wdt: <http://www.wikidata.org/prop/direct/>\n"
				+ "PREFIX wikibase: <http://wikiba.se/ontology#>\n" + "PREFIX bd: <http://www.bigdata.com/rdf#>\n"
				+ "SELECT  ?FileFormat ?FileFormatLabel ?ReadSoftware ?ReadSoftwareLabel ?WriteSoftware ?WriteSoftwareLabel \n" + "WHERE\n"
				+ "  { ?FileFormat wdt:" + pronomFileFormatProperty + " \"" + pronomVal + "\" .\n"
				+ "    {?ReadSoftware wdt:" + readableFileFormatProperty + " ?FileFormat . ?ReadSoftware wdt:P31 wd:Q7397} \n" + "    UNION \n"
				+ "    {?WriteSoftware wdt:" + writeableFileFormatProperty + " ?FileFormat . ?WriteSoftware wdt:P31 wd:Q7397} \n"
				+ "SERVICE wikibase:label { bd:serviceParam wikibase:language \"" + language + "\" }\n" + "  }\n" + "";
	}

	public static String getOSbyName(String osName){
		return "PREFIX  wd:    <http://www.wikidata.org/entity/>\n" +
				"PREFIX  wdt: <http://www.wikidata.org/prop/direct/>\n" +
				"PREFIX wikibase: <http://wikiba.se/ontology#>\n" +
				"PREFIX bd: <http://www.bigdata.com/rdf#>\n" +
				"SELECT ?os ?osLabel WHERE {\n" +
				"  ?os wdt:P31 wd:Q9135 .\n" +
				"  ?os rdfs:label ?osLabel .\n" +
				"  FILTER(str(?osLabel) = \"" + osName + "\") .\n" +
				"}";
	}

	// Query to fetch the read/write softwares for a given LOCFDD ID
	public static String locfddQuery(String locfddVal) {
		return "PREFIX  wd:    <http://www.wikidata.org/entity/>\n"
				+ "PREFIX  wdt: <http://www.wikidata.org/prop/direct/>\n"
				+ "PREFIX wikibase: <http://wikiba.se/ontology#>\n" + "PREFIX bd: <http://www.bigdata.com/rdf#>\n"
				+ "SELECT  ?FileFormat ?FileFormatLabel ?ReadSoftware ?WriteSoftware\n" + "WHERE\n"
				+ "  { ?FileFormat wdt:" + locFDDIDProperty + " \"" + locfddVal + "\" .\n" + "  {?ReadSoftware wdt:"
				+ readableFileFormatProperty + " ?FileFormat } \n" + "  UNION \n" + "  {?WriteSoftware wdt:"
				+ writeableFileFormatProperty + " ?FileFormat } .\n"
				+ "SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\" }\n" + "  }\n" + "";
	}

	// Query to fetch the read/write softwares for a given PRONOM ID and LOCFDD ID
	public static String pronomlocfddQuery(String locfddVal, String pronomVal) {
		return "PREFIX  wd:    <http://www.wikidata.org/entity/>\n"
				+ "PREFIX  wdt: <http://www.wikidata.org/prop/direct/>\n"
				+ "PREFIX wikibase: <http://wikiba.se/ontology#>\n" + "PREFIX bd: <http://www.bigdata.com/rdf#>\n"
				+ "SELECT  ?FileFormat ?FileFormatLabel ?ReadSoftware ?WriteSoftware\n" + "WHERE\n"
				+ "  { ?FileFormat wdt:" + locFDDIDProperty + "\"" + locfddVal + "\" .\n" + "    ?FileFormat wdt:"
				+ pronomFileFormatProperty + " \"" + pronomVal + "\" .\n" + "    {?ReadSoftware wdt:"
				+ readableFileFormatProperty + " ?FileFormat } \n" + "    UNION \n" + "    {?WriteSoftware wdt:"
				+ writeableFileFormatProperty + " ?FileFormat } .\n"
				+ "SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\" }\n" + "  }\n" + "";
	}

	// Query to fetch the properties of a Software item
	public static String propertiesQuery(String qIDVal) {
		return "PREFIX  wd:    <http://www.wikidata.org/entity/>\n"
				+ "PREFIX  wdt: <http://www.wikidata.org/prop/direct/>\n"
				+ "PREFIX wikibase: <http://wikiba.se/ontology#>\n" + "PREFIX bd: <http://www.bigdata.com/rdf#>\n"
				+ "PREFIX schema: <http://schema.org/>\n"
				+ "SELECT  ?item ?itemLabel ?licenseLabel ?versionLabel ?websiteLabel ?developerLabel ?softwareLabel ?fileExtensionLabel ?readableFileFormatLabel ?writableFileFormatLabel ?platformLabel ?programmingLanguageLabel ?operatingSystemLabel ?mediaTypeLabel ?userManualLinkLabel ?bugTrackingSystemLabel\n"
				+ "WHERE\n" + "  { ?article schema:about ?item ;\n" + " schema:inLanguage  \"en\" ; \n"
				+ "schema:isPartOf    <https://en.wikipedia.org/> \n"
				+ "FILTER ( ?item = <http://www.wikidata.org/entity/" + qIDVal + "> ) \n" + "OPTIONAL{?item wdt:"
				+ licenseProperty + " ?license .}\n" + "OPTIONAL{?item wdt:" + softwareVersionProperty
				+ " ?version .}\n" + "OPTIONAL{?item wdt:" + officialWebsiteProperty + " ?website .}\n"
				+ "OPTIONAL{?item wdt:" + developerProperty + " ?developer .}\n" + "OPTIONAL{?item wdt:"
				+ dependsOnSoftwareProperty + " ?software .}\n" + "OPTIONAL{?item wdt:" + fileExtensionProperty
				+ " ?fileExtension .}\n" + "OPTIONAL{?item wdt:" + readableFileFormatProperty
				+ " ?readableFileFormat .}\n" + "OPTIONAL{?item wdt:" + writeableFileFormatProperty
				+ " ?writableFileFormat .}\n" + "OPTIONAL{?item wdt:" + platformProperty + " ?platform .}\n"
				+ "OPTIONAL{?item wdt:" + programmingLanProperty + " ?programmingLanguage .}\n" + "OPTIONAL{?item wdt:"
				+ operatingSystemProperty + " ?operatingSystem .}\n" + "OPTIONAL{?item wdt:" + mediaTypeProperty
				+ " ?mediaType .}\n" + "OPTIONAL{?item wdt:" + userManualProperty + " ?userManualLink .}\n"
				+ "OPTIONAL{?item wdt:" + bugTrackingSystemProperty + " ?bugTrackingSystem .}\n"
				+ "SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\" }\n" + "  }\n" + "";
	}

	public static String getFileFormatProperty(String QID) {
		return "PREFIX wd: <http://www.wikidata.org/entity/>\n" +
				"PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
				"PREFIX wikibase: <http://wikiba.se/ontology#>\n" +
				"PREFIX p: <http://www.wikidata.org/prop/>\n" +
				"PREFIX ps: <http://www.wikidata.org/prop/statement/>\n" +
				"PREFIX pq: <http://www.wikidata.org/prop/qualifier/>\n" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
				"PREFIX bd: <http://www.bigdata.com/rdf#>\n" +
				"\n" +
				"SELECT * WHERE {\n" +
				"  {\n" +
				"SELECT ?readPronom WHERE {\n" +
				"  ?item wdt:" + readableFileFormatProperty + " ?y. ?y wdt:" + pronomFileFormatProperty + " ?readPronom  \n" +
				"  FILTER(?item = wd:" + QID + ")\n" +
				"    } \n" +
				"    } \n" +
				"  UNION \n" +
				"    {\n" +
				"SELECT ?writePronom WHERE {\n" +
				"  ?item wdt:" + writeableFileFormatProperty + " ?x. ?x wdt:" + pronomFileFormatProperty + " ?writePronom  \n" +
				"  FILTER(?item = wd:" + QID + ")\n" +
				"    } \n" +
				"    }\n" +
				"}";
	}


}
