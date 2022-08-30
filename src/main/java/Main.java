import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import connector.HttpClientGitHubConnector;
import model.GPGKey;
import model.UserInfo;
import wrapper.GPGWrapper;
import wrapper.GitWrapper;

public class Main {

	private static final Logger LOG = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) throws IOException {

		if (ArrayUtils.isEmpty(args) || StringUtils.isBlank(args[0])) {
			LOG.severe("Token is not set or empty");
			return;
		}

		HttpClientGitHubConnector connector = new HttpClientGitHubConnector(args[0]);
		UserInfo userInfo = connector.getUserInformation();

		if (StringUtils.isBlank(userInfo.getName()) || StringUtils.isBlank(userInfo.geteMail())) {
			LOG.severe("User NOT in GHE!");
			return;
		}

		LOG.info("Found User in GHE: " + userInfo.getName() + ", " + userInfo.geteMail());

		String passphrase;
		if (StringUtils.isBlank(args[1])) {
			passphrase = readPasswordFromInput();
		}
		else {
			passphrase = args[1];
		}

		final String keyID =
				GPGWrapper.createKeyForGHE(userInfo.getName(), userInfo.geteMail(), StringUtils.trim(passphrase));

		LOG.info("Key Generated");

		final GPGKey gpgKey = GPGWrapper.exportKeyforGHE(keyID);

		connector.sendNewGPGKey(gpgKey);

		LOG.info("Key Uploaded");

		connector.close();

		GitWrapper.setGitSettings(userInfo.getName(), userInfo.geteMail(), keyID);

		LOG.info("Settings done ... Have a nice day :)");
	}

	private static String readPasswordFromInput() {
		// coding effort ... tbd
		return StringUtils.EMPTY;
	}

}
