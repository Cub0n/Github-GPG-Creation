
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

		final HttpClientGitHubConnector connector = new HttpClientGitHubConnector(args[0]);
		final UserInfo userInfo = connector.getUserInformation();

		if (StringUtils.isBlank(userInfo.getName()) || StringUtils.isBlank(userInfo.geteMail())) {
			LOG.severe("User NOT in GHE!");
			return;
		}

		LOG.info("Found User in GHE: " + userInfo.getName() + ", " + userInfo.geteMail());

		String passphrase;
		if ((args.length == 2) && StringUtils.isNotBlank(args[1])) {
			passphrase = args[1];
		}
		else {
			passphrase = readPasswordFromInput();
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

	private static String readPasswordFromInput() throws IOException {

		boolean identical = false;
		String password = null;
		String checked = null;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
			while (!identical) {

				System.out.println("Please type new password for GPG Key: ");
				password = reader.readLine();

				System.out.println("Repeat password: ");
				checked = reader.readLine();

				identical =
						(StringUtils.isNotBlank(password)
								&& StringUtils.isNotBlank(checked)
								&& StringUtils.equals(password, checked));
			}
		}

		return password;
	}
}
