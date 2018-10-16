package solutions.emulation.preservica.client;

public class SDBConfiguration {
	
	private String username = null;
	private String password = null;
	private boolean isRestricted = false;
	private String restPath = "/sdb/rest/";	
	private String url;

	public SDBConfiguration(String url)
	{
		this.url = url;
	}
	
	public SDBConfiguration(String url, String username, String password)
	{
		this.url = url;
		this.username = username;
		this.password = password;
		setRestricted(true);
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public String getRestPath() {
		return restPath;
	}

	public void setRestPath(String restPath) {
		this.restPath = restPath;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isRestricted() {
		return isRestricted;
	}

	public void setRestricted(boolean isRestricted) {
		this.isRestricted = isRestricted;
	}

	
}
