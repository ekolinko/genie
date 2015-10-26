package edu.ucsd.genie.datamanager.datastructures;

/**
 * Class for storing session data for specified user.
 */
public class SessionData {

	/**
	 * The username associated with the session.
	 */
	private final String mUsername;
	
	/**
	 * The authentication token associated with the session.
	 */
	private final String mAuthenticationToken;

	/**
	 * The API key associated with the session.
	 */
	private final String mApiKey;

	public SessionData(String username, String authenticationToken, String apiKey) {
		mUsername = username;
		mAuthenticationToken = authenticationToken;
		mApiKey = apiKey;
	}
	
	public String getUsername() {
		return mUsername;
	}

	public String getAuthenticationToken() {
		return mAuthenticationToken;
	}

	public String getApiKey() {
		return mApiKey;
	}
}