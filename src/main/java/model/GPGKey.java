package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class GPGKey {

	@JsonProperty(value = "armored_public_key")
	private String armoredPublicKey;

	public String getArmoredPublicKey() {
		return armoredPublicKey;
	}

	public void setArmoredPublicKey(String armoredPublicKey) {
		this.armoredPublicKey = armoredPublicKey;
	}

}
