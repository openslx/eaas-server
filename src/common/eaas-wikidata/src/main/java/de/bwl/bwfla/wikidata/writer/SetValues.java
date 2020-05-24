package de.bwl.bwfla.wikidata.writer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SetValues {
	String language;
	String label;
	String description;
	String fileFormatQuid;
	String token = "token=%2B%5C";

	SetValues(String language, String label, String description) {
		this.language = language;
		this.label = label;
		this.description = description;

	}

	SetValues(String fileFormatQUID) {
		this.fileFormatQuid = fileFormatQUID;
	}

	public String generateTokenData() throws UnsupportedEncodingException {
		String dataParameter = "data=%7B%22labels%22%3A%5B%7B%22language%22%3A%22" + language
				+ "%22%2C%22value%22%3A%22" + URLEncoder.encode(label)
				+ "%22%7D%5D%2C%22descriptions%22%3A%5B%7B%22language%22%3A%22en%22%2C%22value%22%3A%22"
				+ URLEncoder.encode(description) + "%22%7D%5D%7D";
		String tokenWithData = token + "&" + dataParameter;
		return tokenWithData;
	}

	public String generateTokenClaim() {
		String valueParameter = "value=%7B%22entity-type%22%3A%22item%22%2C%22numeric-id%22%3A%22%22%2C%22id%22%3A%22"
				+ fileFormatQuid + "%22%7D";
		String tokenWithData = token + "&" + valueParameter;
		return tokenWithData;
	}
}
