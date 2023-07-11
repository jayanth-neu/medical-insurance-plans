package com.info7255.seconddemo.jwtdemo.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.info7255.seconddemo.jwtdemo.exception.BadRequestException;
import com.info7255.seconddemo.jwtdemo.exception.ConflictException;
import com.info7255.seconddemo.jwtdemo.exception.ETagParseException;
import com.info7255.seconddemo.jwtdemo.exception.ResourceNotFoundException;
import com.info7255.seconddemo.jwtdemo.exception.UnauthorizedException;
import com.info7255.seconddemo.jwtdemo.model.ErrorResponse;
import com.info7255.seconddemo.jwtdemo.model.JwtResponse;
import com.info7255.seconddemo.jwtdemo.service.PlanService;
import com.info7255.seconddemo.jwtdemo.util.JwtUtil;

@RestController
public class PlanController {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private PlanService planService;

	@GetMapping("/getToken")
	public ResponseEntity<JwtResponse> generateToken() {
		String token = jwtUtil.generateToken();
		return new ResponseEntity<>(new JwtResponse(token), HttpStatus.CREATED);
	}

	@PostMapping("/validate")
	public boolean validateToken(@RequestHeader HttpHeaders requestHeader) {
		boolean isValid;
		String authorization = requestHeader.getFirst("Authorization");
		if (authorization == null || authorization.isBlank())
			throw new UnauthorizedException("Missing token!");
		try {
			String token = authorization.split(" ")[1];
			isValid = jwtUtil.validateToken(token);
		} catch (Exception e) {
			return false;
		}
		return isValid;
	}

	@PostMapping(value = "/post", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> postPlan(@RequestBody(required = false) String planObj) {
		if (planObj == null || planObj.isBlank())
			throw new BadRequestException("Missing Request Body");

		JSONObject plan = new JSONObject(planObj);
		JSONObject schemaJSON = new JSONObject(new JSONTokener(
				Objects.requireNonNull(PlanController.class.getResourceAsStream("/static/schema.json"))));
		Schema schema = SchemaLoader.load(schemaJSON);
		try {
			schema.validate(plan);

		} catch (ValidationException e) {
			throw new BadRequestException(e.getMessage());
		}

		String key = "plan:" + plan.getString("objectId");
		if (planService.isKeyPresent(key))
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("Error", "PLAN ALREADY EXISTS"));
		String Etag = planService.createPlan(key, plan);
		HttpHeaders header = new HttpHeaders();
		header.setETag(Etag);
		return new ResponseEntity<>("{\"objectId\": \"" + plan.getString("objectId")
				+ "\"\n \"message\": \"Plan created successfully\"\n	\"Status\": " + HttpStatus.CREATED.value()
				+ "}", header, HttpStatus.CREATED);

	}

	@GetMapping(path = "/get/{ObjectType}/{ObjectId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getPlan(@PathVariable String ObjectType, @PathVariable String ObjectId,
			@RequestHeader HttpHeaders httpHeaders) {
		String key = ObjectType + ":" + ObjectId;
		if (!planService.isKeyPresent(key))
			ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Error", "Object Not Found"));

		List<String> ifNoneMatch;
		try {
			ifNoneMatch = httpHeaders.getIfNoneMatch();

		} catch (Exception ex) {
			throw new ETagParseException("Invalid Etag value");
		}

		String eTag = planService.getEtag(key);
		HttpHeaders headersToSend = new HttpHeaders();
		headersToSend.setETag(eTag);

		if (ObjectType.equals("plan") && ifNoneMatch.contains(eTag))
			return new ResponseEntity<>(null, headersToSend, HttpStatus.NOT_MODIFIED);

		Map<String, Object> objectToReturn = planService.getPlan(key);
		if (ObjectType.equals("plan"))
			return new ResponseEntity<>(objectToReturn, httpHeaders, HttpStatus.OK);
		if (objectToReturn.isEmpty())
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Error", "Object Not Found"));
		return new ResponseEntity<>(objectToReturn, HttpStatus.OK);

	}

	@DeleteMapping(path = "/delete/{ObjectType}/{ObjectId}")
	public ResponseEntity<?> deletePlan(@PathVariable String ObjectType, @PathVariable String ObjectId,
			@RequestHeader HttpHeaders headers) {
		String key = ObjectType + ":" + ObjectId;
		if (!planService.isKeyPresent(key))
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Error", "Object Not Found"));

		String eTag = planService.getEtag(key);
		List<String> ifMatch;
		try {
			ifMatch = headers.getIfMatch();
		} catch (Exception e) {
			throw new ETagParseException("ETag value invalid! Make sure the ETag value is a string!");
		}

		if (ifMatch.size() == 0) throw new ETagParseException("ETag is not provided with request!");
		if (!ifMatch.contains(eTag)) return preConditionFailed(eTag);

		Map<String, Object> plan = planService.getPlan(key);
		planService.deletePlan(key);
		return ResponseEntity.noContent()
                .build();

	}

	@PutMapping(value = "/put/plan/{objectId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updatePlan(@PathVariable String objectId, @RequestBody(required = false) String planObject,
			@RequestHeader HttpHeaders headers) {
		if (planObject == null || planObject.isBlank())
			throw new BadRequestException("Request body is missing!");

		JSONObject plan = new JSONObject(planObject);
		String key = "plan:" + objectId;
		if (!planService.isKeyPresent(key))
			throw new ResourceNotFoundException("Plan not found!");

		String eTag = planService.getEtag(key);
		List<String> ifMatch;
		try {
			ifMatch = headers.getIfMatch();
		} catch (Exception e) {
			throw new ETagParseException("ETag value invalid! Make sure the ETag value is a string!");
		}

		if (ifMatch.size() == 0)
			throw new ETagParseException("ETag is not provided with request!");
		if (!ifMatch.contains(eTag))
			return preConditionFailed(eTag);

		JSONObject schemaJSON = new JSONObject(new JSONTokener(
				Objects.requireNonNull(PlanController.class.getResourceAsStream("/static/schema.json"))));
		Schema schema = SchemaLoader.load(schemaJSON);
		try {
			schema.validate(plan);
		} catch (ValidationException e) {
			throw new BadRequestException(e.getMessage());
		}

		planService.deletePlan(key);
		String updatedETag = planService.createPlan(key, plan);

		HttpHeaders headersToSend = new HttpHeaders();
		headersToSend.setETag(updatedETag);
		return new ResponseEntity<>("{\"message\": \"Plan updated successfully\"}", headersToSend, HttpStatus.OK);
	}

	@PatchMapping(value = "patch/{objectType}/{objectId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> patchPlan(@PathVariable String objectId,
									   @RequestBody(required = false) String planObject,
									   @RequestHeader HttpHeaders headers) {
		if (planObject == null || planObject.isBlank()) throw new BadRequestException("Request body is missing!");

		JSONObject plan = new JSONObject(planObject);
		String key = "plan:" + objectId;
		if (!planService.isKeyPresent(key)) throw new ResourceNotFoundException("Plan not found!");

		String eTag = planService.getEtag(key);
		List<String> ifMatch;
		try {
			ifMatch = headers.getIfMatch();
		} catch (Exception e) {
			throw new ETagParseException("ETag value invalid! Make sure the ETag value is a string!");
		}

		if (ifMatch.size() == 0) throw new ETagParseException("ETag is not provided with request!");
		if (!ifMatch.contains(eTag)) return preConditionFailed(eTag);

		String updatedEtag = planService.createPlan(key, plan);
		return ResponseEntity.ok()
				.eTag(updatedEtag)
				.body(new JSONObject().put("message: ", "Plan updated successfully!!").toString());
	}

	private ResponseEntity preConditionFailed(String eTag) {
		HttpHeaders headersToSend = new HttpHeaders();
		headersToSend.setETag(eTag);
		ErrorResponse errorResponse = new ErrorResponse("Invalid ETag value", HttpStatus.PRECONDITION_FAILED.value(),
				new Date(), HttpStatus.PRECONDITION_REQUIRED.getReasonPhrase());
		return new ResponseEntity<>(errorResponse, headersToSend, HttpStatus.PRECONDITION_FAILED);
	}

}
