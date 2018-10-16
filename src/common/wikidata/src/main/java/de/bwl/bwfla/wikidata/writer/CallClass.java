package de.bwl.bwfla.wikidata.writer;

public class CallClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// ApiConnection connection =
		// ApiConnection.getTestWikidataApiConnection();
		// Optional login :

		/*
		 * try { connection.login("username", "password"); Boolean
		 * s=connection.isLoggedIn(); System.out.println(s); }
		 * catch(LoginFailedException e1) { e1.printStackTrace(); }
		 */

		try {
			// ArrayList<ArrayList<RDFNode>> dataList = new
			// ArrayList<ArrayList<RDFNode>>();
			// dataList=WikiWrite.getTheSoftwareQid("micro");
			// System.out.println(dataList.get(0));
			// System.out.println(dataList.get(1));

			// String q=WikiWrite.fetchQidForPuid("fmt/829");
			// System.out.println(q);

			//String q=WikiWrite.createSoftwareItem("microsoft3","Automation Software");
			//System.out.println(q);

			String s=WikiWrite.addReadableFileFormat("Q29478810", "Q2141903");
			System.out.println(s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
