package com.info7255.seconddemo.jwtdemo.model;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtKeys {

	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	
	
	public JwtKeys(PublicKey publicKey, PrivateKey privateKey) {
		super();
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	public PublicKey getPublicKey() {
		return publicKey;
	}
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	
	
	
	
}
