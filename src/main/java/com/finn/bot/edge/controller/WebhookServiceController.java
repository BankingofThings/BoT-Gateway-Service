package com.finn.bot.edge.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookServiceController {
	//Class Logger Instance
	private static final Logger LOGGER = Logger.getLogger(WebhookServiceController.class.getName());
	
	//Smart Vending Machine available options
	public static enum SVM_Options { coffee, tea, milk, coke, pepsi, fanta};
	
	//IP Address and Port number details for devices Webservice
	private static final String SVM_IP_ADDRESS = "192.168.1.5";
	private static final int PORT_NUMBER = 3001;
	
	//Smart Vending Machine URLs details
	private static final String SVM_Actions_URL = "http://" + SVM_IP_ADDRESS + ":" + PORT_NUMBER + "/actions";
	private static final String SVM_QRCode_URL = "http://" + SVM_IP_ADDRESS + ":" + PORT_NUMBER + "/qrcode";
	private static final String SVM_Pair_URL = "http://" + SVM_IP_ADDRESS + ":" + PORT_NUMBER + "/pairing";
		
	//Smart Vending Machine actions details
	private static final String SVM_Coffee_ActionID = "902642BD-802D-4BCB-9F0F-BC192A70915D";
	private static final String SVM_Tea_ActionID = "63636817-6AFF-465B-B04D-1A21A750A515";
	private static final String SVM_Milk_ActionID = "C8384E76-74CE-4DC8-8EFD-6A455BAA3786";
	private static final String SVM_Coke_ActionID = "03AC499F-E07E-4A7C-91F5-83ECEA8111AC";
	private static final String SVM_Pepsi_ActionID = "8AA95AC9-DB6A-4253-BCF0-154E8EE37584";
	private static final String SVM_Fanta_ActionID = "485B3172-DB36-4DFB-98F9-EC947A10D823";
	
	//SVM Payments counters
	private static Map<SVM_Options, Integer> SVM_PAYMENTS = new HashMap<SVM_Options, Integer>();
	
	//Class Static Block
	{
		SVM_PAYMENTS.put(SVM_Options.coffee, 0);
		SVM_PAYMENTS.put(SVM_Options.tea, 0);
		SVM_PAYMENTS.put(SVM_Options.milk, 0);
		SVM_PAYMENTS.put(SVM_Options.coke, 0);
		SVM_PAYMENTS.put(SVM_Options.pepsi, 0);
		SVM_PAYMENTS.put(SVM_Options.fanta, 0);
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getSupportedEndPoints() {

      return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body("BoT Webhook Service: \n Supported End Points: /webhook");
    }

	@RequestMapping(value = "/qrcode", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQRCodeBytes() throws IOException {

    	CloseableHttpClient httpclient = HttpClients.createDefault();
    	byte [] qrcodeBytes = null;
		try {
			//Instantiate HTTP Get
            HttpGet httpget = new HttpGet(SVM_QRCode_URL);
            
            //Execute HTTP GET
            String requestStr = String.format("Executing request %s" , httpget.getRequestLine());
            LOGGER.config(requestStr);
            CloseableHttpResponse response = httpclient.execute(httpget);
            
            //Extract Response Contents
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity != null){
            	qrcodeBytes = EntityUtils.toByteArray(responseEntity);
            	return ResponseEntity
                        .ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(qrcodeBytes);
            }
		}
		catch(Exception e){
			String exceptionMsg = String.format("Exception caught during performing GET Call with URL: %s " , SVM_QRCode_URL);
			LOGGER.severe(exceptionMsg);
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		finally {
            httpclient.close();
        }
		
		return null;

    }
    
	@RequestMapping(value = "/webhook", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
	produces = MediaType.APPLICATION_JSON_VALUE) 
	public ResponseEntity<String> processWebhookRequest(@RequestBody String jsonActionString) throws IOException{
		//LOGGER.config(jsonActionString);
		String responseBody = "";
		if(jsonActionString.contains("\"displayName\": \"configure\"")) {
			LOGGER.info("Received request to pair and activate device");
			if (pairAndActivateSVM())
				responseBody = buildWebhookResponseString("Device Pairing Successfull");
			else 
				responseBody = buildWebhookResponseString("Device Pairing Failed");
		 } else if(jsonActionString.contains("\"displayName\": \"coffee\"")) {
			LOGGER.info("Received order for coffee");
			responseBody = placeOrderAndTriggerPayment(SVM_Options.coffee);
		 } else if(jsonActionString.contains("\"displayName\": \"tea\"")) {
		    LOGGER.info("Received order for tea");
		    responseBody = placeOrderAndTriggerPayment(SVM_Options.tea);
		 } else if(jsonActionString.contains("\"displayName\": \"milk\"")) { 
		    LOGGER.info("Received order for milk");
		    responseBody = placeOrderAndTriggerPayment(SVM_Options.milk);
		 } else if(jsonActionString.contains("\"displayName\": \"coke\"")) { 
		    LOGGER.info("Received order for coke");
		    responseBody = placeOrderAndTriggerPayment(SVM_Options.coke);
		 } else if(jsonActionString.contains("\"displayName\": \"pepsi\"")) {
		    LOGGER.info("Received order for pepsi");
		    responseBody = placeOrderAndTriggerPayment(SVM_Options.pepsi);
		 } else if(jsonActionString.contains("\"displayName\": \"fanta\"")) {
		    LOGGER.info("Received order for fanta");
		    responseBody = placeOrderAndTriggerPayment(SVM_Options.fanta);
		 } else {
		    LOGGER.config("Received order for nothing");
		    responseBody = buildWebhookResponseString("Nothing");
		}
		    		
		//LOGGER.info(responseBody);
		    		
		return ResponseEntity.ok()
							.contentType(MediaType.APPLICATION_JSON)
							.body(responseBody);
	}
	
	//Static method to pair and activate the device
	private Boolean pairAndActivateSVM() throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String responseBody = "";
		Boolean pairingStatus = false;
		try {
			//Instantiate HTTP Get
            HttpGet httpget = new HttpGet(SVM_Pair_URL);
            
            //Execute HTTP GET
            String requestStr = String.format("Executing request %s" , httpget.getRequestLine());
            LOGGER.config(requestStr);
            CloseableHttpResponse response = httpclient.execute(httpget);
            
            //Extract Response Contents
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity != null){
            	responseBody = EntityUtils.toString(responseEntity);
            	String resBodyContents = String.format("GET Response Body Contents: %s", responseBody);
	            LOGGER.info(resBodyContents);
	            pairingStatus = (response.getStatusLine().getStatusCode() == 200) || 
	            		        (responseBody.contains("Device is already paired")) ? true : false;
            }
		}
		catch(Exception e){
			String exceptionMsg = String.format("Exception caught during performing GET Call with URL: %s " , SVM_QRCode_URL);
			LOGGER.severe(exceptionMsg);
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		finally {
            httpclient.close();
        }
		return pairingStatus;
	}
	
	//Static method to place given order and initiate payment, return response string
	private String placeOrderAndTriggerPayment(final SVM_Options action) throws IOException {
		String responseString = "";
		Integer paymentCounter = 0;
		//Identify placed order, trigger payment and send back response
		switch(action) {
			case coffee:  	//Place order for Coffee, needs to be handled by Vending machine
							//Trigger payment for coffee
							if(triggerPayment(SVM_Coffee_ActionID)) {
								paymentCounter = SVM_PAYMENTS.get(SVM_Options.coffee);
								SVM_PAYMENTS.put(SVM_Options.coffee, ++paymentCounter);
								LOGGER.info("Total payments triggered for Coffee: " +SVM_PAYMENTS.get(SVM_Options.coffee));
								responseString = buildWebhookResponseString("Coffee Successfull");
								new ProcessPushNitifications(SVM_Options.coffee).start();
							} else {
								responseString = buildWebhookResponseString("Coffee Failed");
							}
							break;
			case tea:  		//Place order for Tea, needs to be handled by Vending machine
							//Trigger payment for tea
							if(triggerPayment(SVM_Tea_ActionID)) {
								paymentCounter = SVM_PAYMENTS.get(SVM_Options.tea);
								SVM_PAYMENTS.put(SVM_Options.tea, ++paymentCounter);
								LOGGER.info("Total payments triggered for Tea: " +SVM_PAYMENTS.get(SVM_Options.tea));
								responseString = buildWebhookResponseString("Tea Successfull");
								new ProcessPushNitifications(SVM_Options.tea).start();
							} else {
								responseString = buildWebhookResponseString("Tea Failed");
							}
							break;
			case milk:  	//Place order for Milk, needs to be handled by Vending machine
							//Trigger payment for milk
							if(triggerPayment(SVM_Milk_ActionID)) {
								paymentCounter = SVM_PAYMENTS.get(SVM_Options.milk);
								SVM_PAYMENTS.put(SVM_Options.milk, ++paymentCounter);
								LOGGER.info("Total payments triggered for Milk: " +SVM_PAYMENTS.get(SVM_Options.milk));
								responseString = buildWebhookResponseString("Milk Successfull");
								new ProcessPushNitifications(SVM_Options.milk).start();
							} else {
								responseString = buildWebhookResponseString("Milk Failed");
							}
							break;
			case coke:  	//Place order for Coke, needs to be handled by Vending machine
							//Trigger payment for coke
							if(triggerPayment(SVM_Coke_ActionID)) {
								paymentCounter = SVM_PAYMENTS.get(SVM_Options.coke);
								SVM_PAYMENTS.put(SVM_Options.coke, ++paymentCounter);
								LOGGER.info("Total payments triggered for Coke: " +SVM_PAYMENTS.get(SVM_Options.coke));
								responseString = buildWebhookResponseString("Coke Successfull");
								new ProcessPushNitifications(SVM_Options.coke).start();
							} else {
								responseString = buildWebhookResponseString("Coke Failed");
							}
							break;
			case pepsi:  	//Place order for Pepsi, needs to be handled by Vending machine
							//Trigger payment for pepsi
							if(triggerPayment(SVM_Pepsi_ActionID)) {
								paymentCounter = SVM_PAYMENTS.get(SVM_Options.pepsi);
								SVM_PAYMENTS.put(SVM_Options.pepsi, ++paymentCounter);
								LOGGER.info("Total payments triggered for Pepsi: " +SVM_PAYMENTS.get(SVM_Options.pepsi));
								responseString = buildWebhookResponseString("Pepsi Successfull");
								new ProcessPushNitifications(SVM_Options.pepsi).start();
							} else {
								responseString = buildWebhookResponseString("Pepsi Failed");
							}
							break;
			case fanta:  	//Place order for Fanta, needs to be handled by Vending machine
							//Trigger payment for fanta
							if(triggerPayment(SVM_Fanta_ActionID)) {
								paymentCounter = SVM_PAYMENTS.get(SVM_Options.fanta);
								SVM_PAYMENTS.put(SVM_Options.fanta, ++paymentCounter);
								LOGGER.info("Total payments triggered for Fanta: " +SVM_PAYMENTS.get(SVM_Options.fanta));
								responseString = buildWebhookResponseString("Fanta Successfull");
								new ProcessPushNitifications(SVM_Options.fanta).start();
							} else {
								responseString = buildWebhookResponseString("Fanta Failed");
							}
							break;
			default:  	//Misplaced order
						//Return meaning full response
						responseString = buildWebhookResponseString("Nothing");
		}
		
		//Return the built response string based on payment trigger result
		return responseString;
	}
	
	//Static method to build response string using given beverage
	private String buildWebhookResponseString(final String message) {
    	String responseString = "{\r\n" + 
    			"  \"payload\": {\r\n" + 
    			"    \"google\": {\r\n" + 
    			"      \"expectUserResponse\": true,\r\n" + 
    			"      \"richResponse\": {\r\n" + 
    			"        \"items\": [\r\n" + 
    			"          {\r\n" + 
    			"            \"simpleResponse\": {\r\n" + 
    			"              \"textToSpeech\": \"Request for " + message +" \",\r\n" + 
    			"              \"displayText\": \"Request for " + message +" \" \r\n" + 
    			"            }\r\n" + 
    			"          }\r\n" + 
    			"        ]\r\n" + 
    			"      }\r\n" + 
    			"    }\r\n" + 
    			"  }\r\n" + 
    			"}";
    	
    	return responseString;
    }
	
	//Static Method to initiate payment for the given order
	private static Boolean triggerPayment(final String actionID) throws IOException{
		String responseBody = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		Boolean triggerResult = false;
		try {
			//Instantiate HTTP Post
	        HttpPost httpPost = new HttpPost(SVM_Actions_URL);
	            
	        //Prepare Post Body
	        String actionString = "{\" actionID \" : \"" + actionID + "\" } ";
	        StringEntity entity = new StringEntity(actionString);
			httpPost.setEntity(entity);
				
			//Add required HTTP headers
	        httpPost.addHeader("Content-Type", "application/json");
	            
	        //Execute HTTP Post
	        String requestStr = String.format("Executing request %s" , httpPost.getRequestLine());
	        LOGGER.info(requestStr);
	        CloseableHttpResponse response = httpclient.execute(httpPost);
	            
	        //Extract Response Contents
	        HttpEntity responseEntity = response.getEntity();
	        if(responseEntity != null){
	        	responseBody = EntityUtils.toString(responseEntity);
	            String resBodyContents = String.format("Post Response Body Contents: %s", responseBody);
	            LOGGER.info(resBodyContents);
	            triggerResult = response.getStatusLine().getStatusCode() == 200;
	        }
		}
		catch(Exception e){
			String exceptionMsg = String.format("Exception caught during performing POST Call with URL: %s " , SVM_Actions_URL);
			LOGGER.severe(exceptionMsg);
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		finally {
	        httpclient.close();
	    }
			
		return triggerResult;
	}
	
	static class ProcessPushNitifications extends Thread {
		//Class Logger Instance
		private static final Logger LOGGER = Logger.getLogger(ProcessPushNitifications.class.getName());
		
		//Smart Vending Machine push notifications details
		private static final String SVM_Coffee_Push_NotifyID = "CE88E63D-D54A-4F13-A4C6-C5DD9FCF26E3";
		private static final String SVM_Tea_Push_NotifyID = "7FDBBF40-D661-4BC8-A324-FCE6C681A53A";
		private static final String SVM_Milk_Push_NotifyID = "132D708E-2D85-420D-A540-920A9658D74A";
		private static final String SVM_Coke_Push_NotifyID = "43349934-9748-452E-AE42-7395A102AC0F";
		private static final String SVM_Pepsi_Push_NotifyID = "CC7ABC65-8BD2-42EB-8D3F-2921CD89F5F7";
		private static final String SVM_Fanta_Push_NotifyID = "28DC87A5-A0E3-4C8D-9625-2ACDDAF6029A";
		
		private WebhookServiceController.SVM_Options selectedOption;
		
		public ProcessPushNitifications(WebhookServiceController.SVM_Options option) {
			selectedOption = option;
		}
		
		public void run() {
			switch(selectedOption) {
			case coffee :	try {
								 if(ProcessPushNitifications.triggerPushNotification(SVM_Coffee_Push_NotifyID))
									 LOGGER.info("Processed the push notification for Coffee order payment...");
								 else
									 LOGGER.warning("Push notification for Coffee order payment not processed!!!");
							 } catch (Exception e){
								 LOGGER.severe(e.toString());
							 }
							break;
			case tea :	 try {
				 				if(ProcessPushNitifications.triggerPushNotification(SVM_Tea_Push_NotifyID))
				 					LOGGER.info("Processed the push notification for Tea order payment...");
				 				else
				 					LOGGER.warning("Push notification for Tea order payment not processed!!!");
			 				} catch (Exception e){
			 					LOGGER.severe(e.toString());
			 				}
							break;
			case milk :	 try {
 								if(ProcessPushNitifications.triggerPushNotification(SVM_Milk_Push_NotifyID))
 									LOGGER.info("Processed the push notification for Milk order payment...");
 								else
 									LOGGER.warning("Push notification for Milk order payment not processed!!!");
							} catch (Exception e){
								LOGGER.severe(e.toString());
							}
							break;
			case coke :	 try {
								if(ProcessPushNitifications.triggerPushNotification(SVM_Coke_Push_NotifyID))
									LOGGER.info("Processed the push notification for 500ml Coke order payment...");
								else
									LOGGER.warning("Push notification for 500ml Coke order payment not processed!!!");
							} catch (Exception e){
								LOGGER.severe(e.toString());
							}
							break;
			case pepsi :	 try {
								if(ProcessPushNitifications.triggerPushNotification(SVM_Pepsi_Push_NotifyID))
									LOGGER.info("Processed the push notification for 500ml Pepsi order payment...");
								else
									LOGGER.warning("Push notification for 500ml Pepsi order payment not processed!!!");
							} catch (Exception e){
								LOGGER.severe(e.toString());
							}
							break;
			case fanta :	 try {
								if(ProcessPushNitifications.triggerPushNotification(SVM_Fanta_Push_NotifyID))
									LOGGER.info("Processed the push notification for 500ml Fanta order payment...");
								else
									LOGGER.warning("Push notification for 500ml Fanta order payment not processed!!!");
							} catch (Exception e){
								LOGGER.severe(e.toString());
							}
							break;
			}
		}
		
		//Static Method to initiate payment for the given order
		private static Boolean triggerPushNotification(final String notificationID) throws IOException{
			String responseBody = null;
			CloseableHttpClient httpclient = HttpClients.createDefault();
			Boolean triggerResult = false;
			try {
				//Instantiate HTTP Post
		        HttpPost httpPost = new HttpPost(SVM_Actions_URL);
		            
		        //Prepare Post Body
		        String actionString = "{\" actionID \" : \"" + notificationID + "\" } ";
		        StringEntity entity = new StringEntity(actionString);
				httpPost.setEntity(entity);
					
				//Add required HTTP headers
		        httpPost.addHeader("Content-Type", "application/json");
		            
		        //Execute HTTP Post
		        String requestStr = String.format("Executing request %s" , httpPost.getRequestLine());
		        LOGGER.info(requestStr);
		        CloseableHttpResponse response = httpclient.execute(httpPost);
		            
		        //Extract Response Contents
		        HttpEntity responseEntity = response.getEntity();
		        if(responseEntity != null){
		        	responseBody = EntityUtils.toString(responseEntity);
		            String resBodyContents = String.format("Post Response Body Contents: %s", responseBody);
		            LOGGER.info(resBodyContents);
		            triggerResult = response.getStatusLine().getStatusCode() == 200;
		        }
			}
			catch(Exception e){
				String exceptionMsg = String.format("Exception caught during performing POST Call with URL: %s " , SVM_Actions_URL);
				LOGGER.severe(exceptionMsg);
				LOGGER.severe(ExceptionUtils.getStackTrace(e));
			}
			finally {
		        httpclient.close();
		    }
				
			return triggerResult;
		}
	}
}
