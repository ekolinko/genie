package edu.ucsd.genie.connectionmanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.stream.JsonReader;

import edu.ucsd.genie.R;
import edu.ucsd.genie.datamanager.DataManager;
import edu.ucsd.genie.datamanager.datastructures.SessionData;
import edu.ucsd.genie.jsonmanager.JsonManager;

/**
 * Class that handles authentication with BuildingDepot at UCSD.
 */
public class ConnectionManager {

	public enum HttpType {
		POST, GET;
	}
	
	public enum Status {
		SUCCESS("Success"), 
		ERROR_UNSUPPORTED_ENCODING_EXCEPTION("The specified encoding is unsupported."), 
		ERROR_PROTOCOL_EXCEPTION("The specified protocol is unsupported."), 
		ERROR_IO_EXCEPTION("Unable to connect. Please check your internet connection and try again."), 
		ERROR_AUTHENTICATION_FAILURE("The username or password you entered is incorrect."), 
		ERROR_FAILURE("A failure has occurred."), 
		ERROR_ILLEGAL_STATE_EXCEPTION("The application encountered an illegal state."), 
		ERROR_JSON_EXCEPTION("The application encountered an exception when parsing JSON."), 
		ERROR_NOT_CONNECTED("The application is not connected to BuildingDepot. Please close the application and try again."), 
		ERROR_MALFORMED_URL_EXCEPTION("The specified url is malformed.");
	
		/**
		 * The error message that is associated with the status.
		 */
		private final String mMessage;
		
		private Status (String message) {
			mMessage = message;
		}
		
		public String toString() {
			return mMessage;
		}
	
	}

	/**
	 * The context of the application.
	 */
	private final Context mContext;

	/**
	 * The main genie url.
	 */
	private final String mGenieUrl;

	/**
	 * Flag indicating whether Genie is connected to BuildingDepot.
	 */
	private boolean mIsConnected = false;

	/**
	 * The session data associated with the connection.
	 */
	private SessionData mSessionData;

	private static ConnectionManager instance = null;

	private ConnectionManager(Context context) {
		mContext = context;
		mGenieUrl = mContext.getString(R.string.url_genie);
	}

	public static ConnectionManager getInstance(Context context) {
		if (instance == null) {
			synchronized (ConnectionManager.class) {
				if (instance == null) {
					instance = new ConnectionManager(context);
				}
			}
		}
		return instance;
	}

	/**
	 * Login to BuildingDepot at UCSD using the specified username and password.
	 * This method uses the network and should NOT be called on the main Android
	 * UI thread.
	 * 
	 * @param username
	 *            the username with which to connect to BuildingDepot.
	 * @param password
	 *            the password with which to connect to BuildingDepot.
	 */
	public Status login(final String username, final String password) {
		JSONObject request = new JSONObject();
		try {
			request.put(mContext.getString(R.string.json_username), username);
			request.put(mContext.getString(R.string.json_password), password);
		} catch (JSONException jse) {
			new Response(Status.ERROR_JSON_EXCEPTION);
		}
		Response response = sendRequest(mContext.getString(R.string.url_login),
				request, HttpType.POST);
		if (response.getStatus() == Status.SUCCESS) {
			mIsConnected = true;
			JsonReader reader = response.getMessage();
			try {
				reader.beginObject();
				while (reader.hasNext()) {
					String name = reader.nextName();
					if (name.equals(mContext.getString(R.string.json_bdaccess))) {
						mSessionData = JsonManager.getInstance(mContext).readSessionData(username, reader);
					} else
						reader.skipValue();
				}
				reader.endObject();
			} catch (IOException e) {
				return Status.ERROR_JSON_EXCEPTION;
			}
		}
		return response.getStatus();
	}
	
	/**
	 * Send feedback from the user.
	 * 
	 * @param value the value of the feedback from -3 to 3, -3 being the coldest and 3 being the hottest.
	 * @param comment the user comment associated with the value.
	 * @return the status of the command.
	 */
	public Status sendFeedback(final int value, final String comment) {
		if (mIsConnected && mSessionData != null) {
			JSONObject request = new JSONObject();
			try {
				request.put(mContext.getString(R.string.json_room), DataManager.getInstance(mContext).getCurrentRoom());
				request.put(mContext.getString(R.string.json_feeling), Integer.toString(value));
				request.put(mContext.getString(R.string.json_comment), comment);
			} catch (JSONException jse) {
				new Response(Status.ERROR_JSON_EXCEPTION);
			}
			Response response = sendRequest(mContext.getString(R.string.url_send_feedback), request, HttpType.POST);
			return response.getStatus();
		}

		return Status.ERROR_NOT_CONNECTED;
	}
	
	/**
	 * Send a user report about something wrong in a room they occupy.
	 * 
	 * @param report the user report associated with the room they occupy.
	 * @return the status of the command.
	 */
	public Status sendReport(final String report) {
		if (mIsConnected && mSessionData != null) {
			JSONObject request = new JSONObject();
			try {
				request.put(mContext.getString(R.string.json_room), DataManager.getInstance(mContext).getCurrentRoom());
				request.put(mContext.getString(R.string.json_report), report);
			} catch (JSONException jse) {
				new Response(Status.ERROR_JSON_EXCEPTION);
			}
			Response response = sendRequest(mContext.getString(R.string.url_send_report), request, HttpType.POST);
			return response.getStatus();
		}

		return Status.ERROR_NOT_CONNECTED;
	}
	
	/**
	 * Update the user's password with the new password.
	 * 
	 * @param password the user's new password.
	 * @param oldPassword the user's old password for verification.
	 */
	public Status updatePassword(final String password, final String oldPassword) {
		if (mIsConnected && mSessionData != null) {
			JSONObject request = new JSONObject();
			try {
				request.put(mContext.getString(R.string.json_request), mContext.getString(R.string.json_request_password));
				request.put(mContext.getString(R.string.json_password),
						password);
				request.put(mContext.getString(R.string.json_oldpassword),
						oldPassword);
			} catch (JSONException jse) {
				new Response(Status.ERROR_JSON_EXCEPTION);
			}
			Response response = sendRequest(String.format(
					mContext.getString(R.string.url_update_user),
					mSessionData.getUsername()), request, HttpType.POST);
			return response.getStatus();
		}

		return Status.ERROR_NOT_CONNECTED;
	}
	
	/**
	 * Update the user's rooms with the specified rooms.
	 * 
	 * @param rooms the new list of rooms for the user.
	 */
	public Status updateRooms(final ArrayList<String> rooms) {
		if (mIsConnected && mSessionData != null) {
			JSONObject request = new JSONObject();
			try {
				request.put(mContext.getString(R.string.json_request), mContext.getString(R.string.json_request_rooms));
				request.put(mContext.getString(R.string.json_room), TextUtils.join(",", rooms));
			} catch (JSONException jse) {
				new Response(Status.ERROR_JSON_EXCEPTION);
			}
			Response response = sendRequest(String.format(
					mContext.getString(R.string.url_update_user),
					mSessionData.getUsername()), request, HttpType.POST);
			return response.getStatus();
		}

		return Status.ERROR_NOT_CONNECTED;
	}
	
	/**
	 * Send an HVAC control command to Genie.
	 * 
	 * @param value the value of the command - 1 (OFF), 3 (ON)
	 */
	public Status sendHVACControlCommand(int value, String duration) {
		if (mIsConnected && mSessionData != null) {
			JSONObject request = new JSONObject();
			try {
				request.put(mContext.getString(R.string.json_room), DataManager.getInstance(mContext).getCurrentRoom());
				request.put(mContext.getString(R.string.json_command), mContext.getString(R.string.hvac_command_actuate));
				request.put(mContext.getString(R.string.json_value), value);
				request.put(mContext.getString(R.string.json_duration), duration);
			} catch (JSONException jse) {
				new Response(Status.ERROR_JSON_EXCEPTION);
			}
			Response response = sendRequest(mContext.getString(R.string.url_send_hvac_control_command), request, HttpType.POST);
			return response.getStatus();
		}
		
		return Status.ERROR_NOT_CONNECTED;
	}
	
	/**
	 * Send an HVAC control command to Genie.
	 * 
	 * @param value the temperature to set for Genie.
	 */
	public Status sendHVACTemperatureCommand(float value, String duration) {
		if (mIsConnected && mSessionData != null) {
			JSONObject request = new JSONObject();
			try {
				request.put(mContext.getString(R.string.json_room), DataManager.getInstance(mContext).getCurrentRoom());
				request.put(mContext.getString(R.string.json_command), mContext.getString(R.string.hvac_command_temperature));
				request.put(mContext.getString(R.string.json_value), value);
				request.put(mContext.getString(R.string.json_duration), duration);
			} catch (JSONException jse) {
				new Response(Status.ERROR_JSON_EXCEPTION);
			}
			Response response = sendRequest(mContext.getString(R.string.url_send_hvac_control_command), request, HttpType.POST);
			return response.getStatus();
		}
		
		return Status.ERROR_NOT_CONNECTED;
	}
	
	/**
	 * Send an HVAC schedule command to Genie.
	 * 
	 * @param start the start time of the HVAC schedule.
	 * @param end the end time of the HVAC schedule.
	 */
	public Status sendHVACScheduleCommand(String start, String end) {
		if (mIsConnected && mSessionData != null) {
			JSONObject request = new JSONObject();
			try {
				request.put(mContext.getString(R.string.json_start), start);
				request.put(mContext.getString(R.string.json_end), end);
			} catch (JSONException jse) {
				new Response(Status.ERROR_JSON_EXCEPTION);
			}
			Response response = sendRequest(String.format(mContext.getString(R.string.url_send_hvac_control_schedule), DataManager.getInstance(mContext).getCurrentRoom()), request, HttpType.POST);
			return response.getStatus();
		}
		
		return Status.ERROR_NOT_CONNECTED;
	}
	
	/**
	 * Get the user information for the session.
	 * 
	 * @param username
	 *            the username with which to connect to BuildingDepot.
	 * @param password
	 *            the password with which to connect to BuildingDepot.
	 */
	public Status getUserInformation() {
		if (mIsConnected && mSessionData != null) {
			Response response = sendRequest(String.format(mContext.getString(R.string.url_get_user_information), mSessionData.getUsername()));
			if (response.getStatus() == Status.SUCCESS) {
				JsonReader reader = response.getMessage();
				try {
					reader.beginObject();
					while (reader.hasNext()) {
						String name = reader.nextName();
						if (name.equals(mContext.getString(R.string.json_user))) {
							DataManager.getInstance(mContext).setUser(JsonManager.getInstance(mContext).readUserInformation(reader));
						} else
							reader.skipValue();
					}
					reader.endObject();
				} catch (IOException e) {
					return Status.ERROR_JSON_EXCEPTION;
				}
			}
			return response.getStatus();
		}
		
		return Status.ERROR_NOT_CONNECTED;
	}
	
	/**
	 * Get at list of rooms in the building
	 */
	public Status getRooms() {
		if (mIsConnected && mSessionData != null) {
			Response response = sendRequest(String.format(mContext.getString(R.string.url_get_rooms), mContext.getString(R.string.building)));
			if (response.getStatus() == Status.SUCCESS) {
				JsonReader reader = response.getMessage();
				try {
					reader.beginObject();
					while (reader.hasNext()) {
						String name = reader.nextName();
						if (name.equals(mContext.getString(R.string.json_rooms))) {
							DataManager.getInstance(mContext).setRooms(JsonManager.getInstance(mContext).readRooms(reader));
						} else
							reader.skipValue();
					}
					reader.endObject();
				} catch (IOException e) {
					return Status.ERROR_JSON_EXCEPTION;
				}
			}
			return response.getStatus();
		}
		
		return Status.ERROR_NOT_CONNECTED;
	}
	
	/**
	 * Get detailed information about a specific room.
	 */
	public Status getRoomInformation() {
		if (mIsConnected && mSessionData != null) {
			Response response = sendRequest(String.format(mContext.getString(R.string.url_get_room_information), mContext.getString(R.string.building), DataManager.getInstance(mContext).getCurrentRoom()));
			if (response.getStatus() == Status.SUCCESS) {
				JsonReader reader = response.getMessage();
				try {
					reader.beginObject();
					while (reader.hasNext()) {
						String name = reader.nextName();
						if (name.equals(mContext.getString(R.string.json_room))) {
							reader.skipValue();
						} else if (name.equals(mContext.getString(R.string.json_zone))) {
							DataManager.getInstance(mContext).setZone(JsonManager.getInstance(mContext).readZone(reader));	
						} else
							reader.skipValue();
					}
					reader.endObject();
				} catch (IOException e) {
					return Status.ERROR_JSON_EXCEPTION;
				}
			}
			return response.getStatus();
		}
		
		return Status.ERROR_NOT_CONNECTED;
	}
	
	/**
	 * Get the latest data bout the sensors.
	 * 
	 * @param building the name of the building.
	 * @param room the name of the room for which to get the latest information.
	 */
	public Status getLatestData() {
		if (mIsConnected && mSessionData != null) {
			Response response = sendRequest(String.format(mContext.getString(R.string.url_get_latest_data), mContext.getString(R.string.building), DataManager.getInstance(mContext).getCurrentRoom()));
			if (response.getStatus() == Status.SUCCESS) {
				JsonReader reader = response.getMessage();
				DataManager.getInstance(mContext).setLatestData(JsonManager.getInstance(mContext).readLatestData(reader));
			}
			return response.getStatus();
		}
		
		return Status.ERROR_NOT_CONNECTED;
	}

	/**
	 * Log out of BuildingDepot at UCSD. This method uses the network and should
	 * NOT be called on the main Android SUI thread.
	 */
	public Status logout() {
		if (mIsConnected && mSessionData != null) {
			HttpClient httpClient = getSSLClient();
			HttpDelete httpRequest = new HttpDelete(mGenieUrl + String.format(mContext.getString(R.string.url_logout), mSessionData.getAuthenticationToken()));
			httpRequest.setHeader(mContext.getString(R.string.http_content_type),
					mContext.getString(R.string.http_application_json));
			httpRequest.setHeader(mContext.getString(R.string.http_api_key),
					mSessionData.getApiKey());
			httpRequest.setHeader(mContext.getString(R.string.http_authentication_token),
					mSessionData.getAuthenticationToken());
			HttpResponse httpResponse;
			try {
				httpResponse = httpClient.execute(httpRequest);
			} catch (ClientProtocolException e) {
				return Status.ERROR_PROTOCOL_EXCEPTION;
			} catch (IOException e) {
				return Status.ERROR_IO_EXCEPTION;
			}
			StatusLine statusLine = httpResponse.getStatusLine();
			if (statusLine.getStatusCode() == mContext.getResources().getInteger(
					R.integer.http_status_success)) {
				return Status.SUCCESS;
			} else
				return Status.ERROR_FAILURE;
		}

		return Status.ERROR_NOT_CONNECTED;
	}

	/**
	 * Send an HTTPRequest with a JSON message to the specified url at Genie.
	 * 
	 * @param url
	 *            the url to send the request to.
	 * @param request
	 *            the request to send.
	 * @return the response with a status and message.
	 */
	private Response sendRequest(String url, JSONObject request, HttpType type) {
//		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
//		DefaultHttpClient client = new DefaultHttpClient();
//		SchemeRegistry registry = new SchemeRegistry();
//		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
//		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
//		registry.register(new Scheme("https", socketFactory, 443));
//		SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
//		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
//		HttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());
		
		HttpClient httpClient = getSSLClient();
		HttpRequestBase httpRequest = null;
		if (type == HttpType.POST) {
			httpRequest = new HttpPost(mGenieUrl + url);
			if (request != null) {
				try {
					((HttpPost)httpRequest).setEntity(new StringEntity(request.toString()));
				} catch (UnsupportedEncodingException e) {
					return new Response(Status.ERROR_UNSUPPORTED_ENCODING_EXCEPTION);
				}
			}
		} else if (type == HttpType.GET)
			httpRequest = new HttpGet(mGenieUrl + url);
		
		httpRequest.setHeader(mContext.getString(R.string.http_accept),
				mContext.getString(R.string.http_application_json));
		httpRequest.setHeader(mContext.getString(R.string.http_content_type),
				mContext.getString(R.string.http_application_json));
		if (mIsConnected && mSessionData != null) {
			httpRequest.setHeader(mContext.getString(R.string.http_api_key),
					mSessionData.getApiKey());
			httpRequest.setHeader(
					mContext.getString(R.string.http_authentication_token),
					mSessionData.getAuthenticationToken());
		}
		HttpResponse httpResponse;
		try {
			httpResponse = httpClient.execute(httpRequest);
		} catch (ClientProtocolException e) {
			return new Response(Status.ERROR_PROTOCOL_EXCEPTION);
		} catch (IOException e) {
			return new Response(Status.ERROR_IO_EXCEPTION);
		}
		StatusLine statusLine = httpResponse.getStatusLine();
		if (statusLine.getStatusCode() == mContext.getResources().getInteger(
				R.integer.http_status_success)) {
			HttpEntity entity = httpResponse.getEntity();
			try {
				InputStream content = entity.getContent();
				return new Response(Status.SUCCESS, new JsonReader(
						new InputStreamReader(content)));
			} catch (IllegalStateException e) {
				return new Response(Status.ERROR_ILLEGAL_STATE_EXCEPTION);
			} catch (IOException e) {
				return new Response(Status.ERROR_IO_EXCEPTION);
			}
		} else if (statusLine.getStatusCode() == mContext.getResources()
				.getInteger(R.integer.http_status_authentication_failure)) {
			return new Response(Status.ERROR_AUTHENTICATION_FAILURE);
		} else
			return new Response(Status.ERROR_FAILURE);
	}

	/**
	 * Send an HTTPRequest with a JSON message to the specified url at Genie.
	 * 
	 * @param url
	 *            the url to send the request to.
	 * @return the response with a status and message.
	 */
	private Response sendRequest(String url) {
		return sendRequest(url, null, HttpType.GET);
	}

	/**
	 * Returns a SSL client for use with https.
	 * 
	 * @return SSL client for use with https.
	 */
	private HttpClient getSSLClient() {
	    try {
	    	HttpClient client = new DefaultHttpClient();
	        X509TrustManager tm = new X509TrustManager() { 
	            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };
	        SSLContext ctx = SSLContext.getInstance("TLS");
	        ctx.init(null, new TrustManager[]{tm}, null);
	        SSLSocketFactory ssf = new GenieSSLSocketFactory(ctx);
	        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        ClientConnectionManager ccm = client.getConnectionManager();
	        SchemeRegistry sr = ccm.getSchemeRegistry();
	        sr.register(new Scheme("https", ssf, 443));
	        return new DefaultHttpClient(ccm, client.getParams());
	    } catch (Exception ex) {
	        return null;
	    }
	}
	
	/**
	 * Class for generating SSL sockets for Genie.
	 */
	class GenieSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public GenieSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        public GenieSSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
           super(null);
           sslContext = context;
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
   }
	
	/**
	 * Class representing a response containing a status and JSON message.
	 */
	private class Response {
		/**
		 * The status of the response.
		 */
		private final Status mStatus;

		/**
		 * The JSON message.
		 */
		private JsonReader mMessage;

		public Response(Status status) {
			mStatus = status;
		}

		public Response(Status status, JsonReader message) {
			mStatus = status;
			mMessage = message;
		}

		public Status getStatus() {
			return mStatus;
		}

		public JsonReader getMessage() {
			return mMessage;
		}
	}
}
