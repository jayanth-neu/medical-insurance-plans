package com.info7255.demo1.controllers;

import com.info7255.demo1.service.PlanService;
import com.info7255.demo1.validator.JsonValidator;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

    @RestController
    //@RequestMapping(path = "/medicalplan")
    public class MedicalPlanController {

        @Autowired
        JsonValidator validator;

        @Autowired
        PlanService planservice ;

        Map<String, Object> m = new HashMap<String, Object>();

        @PostMapping(path = "/plan/", produces = "application/json")
        public ResponseEntity<Object> createPlan( @RequestBody(required = false) String medicalPlan, @RequestHeader HttpHeaders headers) throws JSONException, Exception {
            m.clear();
            if (medicalPlan == null || medicalPlan.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONObject().put("Error", "Body is Empty.Kindly provide the JSON").toString());
            }


            JSONObject json = new JSONObject(medicalPlan);
            try{
                validator.validateJson(json);
            }catch(ValidationException ex){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONObject().put("Error",ex.getErrorMessage()).toString());

            }

            String key = json.get("objectType").toString() + "_" + json.get("objectId").toString();
            if(planservice.checkIfKeyExists(key)){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new JSONObject().put("Message", "Plan already exist").toString());
            }

            String newEtag = planservice.savePlanToRedisAndMQ(json, key);

            return ResponseEntity.status(HttpStatus.CREATED).eTag(newEtag).body(" {\"message\": \"Created data with key: " + json.get("objectId") + "\" }");

        }


        @GetMapping(path = "/{type}/{objectId}", produces = "application/json")
        public ResponseEntity<Object> getPlan(@RequestHeader HttpHeaders headers, @PathVariable String objectId,@PathVariable String type) throws JSONException, Exception {

            if (!planservice.checkIfKeyExists(type + "_" + objectId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new JSONObject().put("Message", "ObjectId does not exist").toString());
            }

            String actualEtag = null;
            if (type.equals("plan")) {
                actualEtag = planservice.getEtag(type + "_" + objectId, "eTag");
                String eTag = headers.getFirst("If-None-Match");
                if (eTag != null && eTag.equals(actualEtag)) {
                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(actualEtag).build();
                }
            }

            String key = type + "_" + objectId;
            Map<String, Object> plan = planservice.getPlan(key);

            if (type.equals("plan")) {
                return ResponseEntity.ok().eTag(actualEtag).body(new JSONObject(plan).toString());
            }

            return ResponseEntity.ok().body(new JSONObject(plan).toString());
        }


        @DeleteMapping(path = "/plan/{objectId}", produces = "application/json")
        public ResponseEntity<Object> getPlan(@RequestHeader HttpHeaders headers, @PathVariable String objectId){

            if (!planservice.checkIfKeyExists("plan"+ "_" + objectId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new JSONObject().put("Message", "ObjectId does not exist").toString());
            }

            planservice.deletePlan("plan" + "_" + objectId);

            return ResponseEntity
                    .noContent()
                    .build();


        }

        @GetMapping(path = "/test")
        public String test(){
            return "Success";
        }
        }

