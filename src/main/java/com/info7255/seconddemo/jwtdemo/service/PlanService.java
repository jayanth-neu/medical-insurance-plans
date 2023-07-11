package com.info7255.seconddemo.jwtdemo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

@Service
public class PlanService {

	private Jedis jedis;
	private EtagService etagService;

	public PlanService(Jedis jedis, EtagService etagService) {

		this.jedis = jedis;
		this.etagService = etagService;
	}

	public boolean isKeyPresent(String key) {
		// TODO Auto-generated method stub
		Map<String, String> value = jedis.hgetAll(key);
		jedis.close();
		return !(value == null || value.isEmpty());
	}

	public String createPlan(String key, JSONObject plan) {
		// TODO Auto-generated method stub
		jsonToMap(plan);
		return SetEtag(key, plan);
	}

	public String getEtag(String key) {

		return jedis.hget(key, "eTag");
	}

	public String SetEtag(String key, JSONObject plan) {
		// TODO Auto-generated method stub
		String eTag = etagService.getEtag(plan);
		jedis.hset(key, "eTag", eTag);

		return eTag;
	}

	public Map<String, Map<String, Object>> jsonToMap(JSONObject jsonObject) {
		// TODO Auto-generated method stub

		Map<String, Map<String, Object>> map = new HashMap<>();
		Map<String, Object> content = new HashMap<>();
		for (String key : jsonObject.keySet()) {
			String redisKey = jsonObject.get("objectType") + ":" + jsonObject.get("objectId");
			Object value = jsonObject.get(key);

			if (value instanceof JSONObject) {
				value = jsonToMap((JSONObject) value);
				jedis.sadd(redisKey + ":" + key,
						((Map<String, Map<String, Object>>) value).entrySet().iterator().next().getKey());
			} else if (value instanceof JSONArray) {
				value = jsonToList((JSONArray) value);
				((List<Map<String, Map<String, Object>>>) value).forEach((entry) -> {
					entry.keySet().forEach((listKey) -> {
						jedis.sadd(redisKey + ":" + key, listKey);
					});
				});
			} else {
				jedis.hset(redisKey, key, value.toString());
				content.put(key, value);
				map.put(redisKey, content);
			}

		}
		return map;

	}

	public List<Object> jsonToList(JSONArray jsonArray) {
		// TODO Auto-generated method stub
		List<Object> result = new ArrayList<>();
		for (Object value : jsonArray) {
			if (value instanceof JSONArray)
				value = jsonToList((JSONArray) value);
			else if (value instanceof JSONObject)
				value = jsonToMap((JSONObject) value);
			result.add(value);

		}
		return result;
	}

	public Map<String, Object> getPlan(String key) {
		Map<String,Object> result=new HashMap<>();
		getOrDelete(key,result,false);
		return result;
	}

	 private Map<String, Object> getOrDelete(String redisKey, Map<String, Object> resultMap, boolean isDelete) {
	        Set<String> keys = jedis.keys(redisKey + ":*");
	        keys.add(redisKey);

	        for (String key : keys) {
	            if (key.equals(redisKey)) {
	                if (isDelete) jedis.del(new String[]{key});
	                else {
	                    Map<String, String> object = jedis.hgetAll(key);
	                    for (String attrKey : object.keySet()) {
	                        if (!attrKey.equalsIgnoreCase("eTag")) {
	                            resultMap.put(attrKey, isInteger(object.get(attrKey)) ? Integer.parseInt(object.get(attrKey)) : object.get(attrKey));
	                        }
	                    }
	                }
	            } else {
	                String newKey = key.substring((redisKey + ":").length());
	                Set<String> members = jedis.smembers(key);
	                if (members.size() > 1 || newKey.equals("linkedPlanServices")) {
	                    List<Object> listObj = new ArrayList<>();
	                    for (String member : members) {
	                        if (isDelete) {
	                            getOrDelete(member, null, true);
	                        } else {
	                            Map<String, Object> listMap = new HashMap<>();
	                            listObj.add(getOrDelete(member, listMap, false));
	                        }
	                    }
	                    if (isDelete) jedis.del(new String[]{key});
	                    else resultMap.put(newKey, listObj);
	                } else {
	                    if (isDelete) {
	                        jedis.del(new String[]{members.iterator().next(), key});
	                    } else {
	                        Map<String, String> object = jedis.hgetAll(members.iterator().next());
	                        Map<String, Object> nestedMap = new HashMap<>();
	                        for (String attrKey : object.keySet()) {
	                            nestedMap.put(attrKey,
	                                    isInteger(object.get(attrKey)) ? Integer.parseInt(object.get(attrKey)) : object.get(attrKey));
	                        }
	                        resultMap.put(newKey, nestedMap);
	                    }
	                }
	            }
	        }
	        return resultMap;
	    }
	 
	 private boolean isInteger(String str) {
	        try {
	            Integer.parseInt(str);
	        } catch (Exception e) {
	            return false;
	        }
	        return true;
	    }

	public void deletePlan(String key) {
		// TODO Auto-generated method stub
		getOrDelete(key,null, true);
		
	}

}
