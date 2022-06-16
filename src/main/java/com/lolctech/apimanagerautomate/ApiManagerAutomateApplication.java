package com.lolctech.apimanagerautomate;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mashape.unirest.http.HttpResponse;

import java.util.ArrayList;

@SpringBootApplication
public class ApiManagerAutomateApplication implements CommandLineRunner {

	ArrayList<String> services = new ArrayList<String>();

	public static void main(String[] args) {
		SpringApplication.run(ApiManagerAutomateApplication.class, args);
		System.out.println("\n\n----Application Running OUT----");
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("----Application Running IN----\n\n");

		// SECRETS

		// SPECIFY THE USERNAME AND PASSWORD HERE FOR AUTHENTICATE

		// METHOD CALLS
		String token = generateAccessToken(authorization, username, password, scope);
		System.out.println("TOKEN: \n" + token);

		System.out.println("----Get All Services----\n");
		HttpResponse<String> responseAllServices = getAllServices(token);
		System.out.println("RESPONSE STATUS:\n" + responseAllServices.getStatus());

		try {
			int count = 0;
			JSONObject json = new JSONObject(responseAllServices.getBody());
			org.json.JSONArray items = (org.json.JSONArray) json.get("items");
			for(int x=0; x < items.length(); x++) {
				JSONObject item = new JSONObject(items.get(x).toString());
				services.add(item.get("name").toString());
				if(item.get("name").toString().contains("-dev")) {
					System.out.println("Service " + x + 1 + " : " + item.get("name").toString());
					System.out.println("Service " + x + 1 + " ID : " + item.get("id").toString());
					Update(token, item.get("id").toString());
					count++;
				}
			}
			System.out.println("Count: " + count);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void Update(String token, String serviceId) {
		System.out.println("----Get One Service Details----\n");
		HttpResponse<String> responseOneService = getServiceDetails(token, serviceId);
		System.out.println("RESPONSE:\n" + responseOneService.getBody());
		try {
			ServiceResponse serviceResponse = new ServiceResponse();
			JSONObject json = new JSONObject(responseOneService.getBody());
			serviceResponse.setImplementation(json.get("implementation"));
			serviceResponse.setType(json.get("type").toString());
			serviceResponse.setVersion(json.get("version").toString());
			serviceResponse.setName(json.get("name").toString());
			serviceResponse.setState(json.get("state").toString());

			JSONObject implementation = new JSONObject(json.get("implementation").toString());
			JSONObject executions = new JSONObject(implementation.get("executions").toString());
			JSONArray policies = new JSONArray(implementation.get("policies").toString());

			JSONObject PolicyOne = new JSONObject(policies.get(0).toString());
			JSONObject PolicyTwo = new JSONObject(policies.get(1).toString());

			System.out.println(PolicyOne.getString("id"));

			String finalName = serviceResponse.getName().replaceAll("-dev", "");

			String updateJSON = "{\n" +
					"    \"implementation\": {\n" +
					"        \"executions\": " + executions + ",\n" +
					"        \"policies\": [\n" +
					"            {\n" +
					"                \"id\": \"" + PolicyOne.getString("id") + "\",\n" +
					"                \"type\": \"" + PolicyOne.getString("type") + "\",\n" +
					"                \"version\": \"" + PolicyOne.getString("version") + "\",\n" +
					"                \"config\": {\n" +
					"                    \"endpoints\": [\n" +
					"                        {\n" +
					"                            \"name\": " + serviceResponse.getName() + ",\n" +
					"                            \"useProxy\": false,\n" +
					"                            \"url\": \"http://10.253.128.31/" + finalName + "\"\n" +
					"                        }\n" +
					"                    ]\n" +
					"                }\n" +
					"            },\n" +
					"            " + PolicyTwo + "\n" +
					"        ]\n" +
					"    },\n" +
					"    \"type\": \"" + serviceResponse.getType() + "\",\n" +
					"    \"version\": \"" + serviceResponse.getVersion() + "\",\n" +
					"    \"name\": \"" + serviceResponse.getName() + "\",\n" +
					"    \"state\": \"" + serviceResponse.getState() + "\"\n" +
					"}";


			String res = updateService(updateJSON, token, serviceId);
			System.out.println(res);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Update Service
	public String updateService(String JSON, String token, String serviceId) {
		try {
			HttpResponse<String> response = Unirest.put("https://sl-lolc-apim-lolctech.apiplatform.ocp.oraclecloud.com/apiplatform/management/v1/services/" + serviceId)
					.header("content-type", "application/json")
					.header("Authorization", "Bearer " + token)
					.body(JSON)
					.asString();

			JSONObject json = new JSONObject(response.getStatus());
			return json.toString();
		} catch (UnirestException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	//Generate Access Token
	public String generateAccessToken(String authorization, String username, String password, String scope) {
		try {
			HttpResponse<String> response = Unirest.post("https://idcs-c152823cbaa845c8a03641bbdfbe0ad2.identity.oraclecloud.com/oauth2/v1/token")
					.header("content-type", "application/x-www-form-urlencoded;charset=UTF-8")
					.header("Authorization", "Basic " + authorization)
					.body("grant_type=password&username=" + username + "&password=" + password + "&scope=" + scope)
					.asString();

			JSONObject json = new JSONObject(response.getBody());
			String accessToken = json.getString("access_token");
			return accessToken;
		} catch (UnirestException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	// Get all Services
	public HttpResponse<String> getAllServices(String token) {
		try {
			HttpResponse<String> response = Unirest.get("https://sl-lolc-apim-lolctech.apiplatform.ocp.oraclecloud.com/apiplatform/management/v1/services")
					.header("Authorization", "Bearer " + token)
					.asString();

			return response;
		} catch (UnirestException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Get a Service Details
	public HttpResponse<String> getServiceDetails(String token, String serviceId) {
		try {
			HttpResponse<String> response = Unirest.get("https://sl-lolc-apim-lolctech.apiplatform.ocp.oraclecloud.com/apiplatform/management/v1/services/" + serviceId)
					.header("Authorization", "Bearer " + token)
					.asString();

			return response;
		} catch (UnirestException e) {
			e.printStackTrace();
			return null;
		}
	}
}
