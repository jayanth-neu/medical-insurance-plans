package com.info7255.seconddemo.jwtdemo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class EtagService {

	public String getEtag(JSONObject jsonObject) {
		
		String encoded=null;
		try
		{
			
			MessageDigest digest=MessageDigest.getInstance("SHA-256");
			byte[] hash=digest.digest(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
			encoded=Base64.getEncoder().encodeToString(hash);
		}catch(NoSuchAlgorithmException ex) { ex.printStackTrace();}
		return "\""+encoded+"\"";
	}

}
