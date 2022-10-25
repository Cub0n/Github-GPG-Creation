package connector;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;

import model.GPGKey;
import model.UserInfo;

public final class HttpClientGitHubConnector {

	private static final String GITHUB_URL = "https://api.github.com";

	private static final Logger LOG = Logger.getLogger(HttpClientGitHubConnector.class.getName());

	private final ObjectMapper mapper = new ObjectMapper();

	private HttpClient client;

	private final String token;

	/**
	 * Instantiates a new HttpClientGitHubConnector with a default HttpClient.
	 */
	public HttpClientGitHubConnector(final String token) {
		this.token = token;
		this.client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
	}

	public UserInfo getUserInformation() {

		UserInfo userInfo = null;

		try {
			HttpResponse<String> httpResponse = send("/user", "GET", null);
			userInfo = mapper.readValue(httpResponse.body(), UserInfo.class);
		}
		catch (IOException | InterruptedException exception) {
			LOG.warning("Error: Connecting to Github");
			// Restore interrupted state...
			Thread.currentThread().interrupt();
		}

		return userInfo;
	}

	public String sendNewGPGKey(final GPGKey gpgKey) {
		String ret = null;

		try {
			StringWriter writer = new StringWriter();

			mapper.writeValue(writer, gpgKey);

			HttpResponse<String> httpResponse = send("/user/gpg_keys", "POST", writer.toString());
			ret = httpResponse.body();
		}
		catch (IOException | InterruptedException exception) {
			LOG.warning("Error: Could not send GPG Key");
			// Restore interrupted state...
			Thread.currentThread().interrupt();
		}

		return ret;

	}

	private HttpResponse<String> send(final String urlTail, final String method, final String body)
			throws InterruptedException, IOException {

		HttpRequest.Builder builder = HttpRequest.newBuilder();

		try {
			builder.uri((new URL(GITHUB_URL + urlTail)).toURI());
		}
		catch (URISyntaxException e) {
			throw new IOException("Invalid URL", e);
		}

		builder.header("Content-Type", "application/json");
		builder.header("Accept", "application/vnd.github+json");
		builder.header("Authorization", "token " + token);

		HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.noBody();

		if (StringUtils.isNotBlank(body)) {
			publisher = HttpRequest.BodyPublishers.ofByteArray(body.getBytes());
		}

		builder.method(method, publisher);

		HttpRequest request = builder.build();

		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}

	// Hook for cleaning up ...
	public void close() {
		client = null;
	}
}
