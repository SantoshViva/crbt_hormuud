package com.santosh.greenzone.wapchatezee.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.santosh.greenzone.services.impl.PostClientServiceImpl;

@CrossOrigin
@RestController

public class CrbtSubscriberNameDetails {

	private static final Logger logger = LogManager.getLogger(CrbtSubscriberNameDetails.class);
	@Autowired
	Environment env;
	
	
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	@RequestMapping(value = "/crbtSubscriberNameDetails",method = RequestMethod.GET)
	@ResponseBody
	public String crbtCatSubCatInfo(@RequestParam(required=false) String aparty,@RequestParam("dbFlag") String dbFlag,HttpServletRequest req,
			HttpServletResponse res) {
			
		logger.info("crbtSubscriberNameDetails|dbFlag="+dbFlag);
		
		if(dbFlag == null ||dbFlag.toString().isEmpty())
		{
			dbFlag="Y";
		}
		
		
		String selectQuery="select msisdn from CRBT_USER_LIST";
		try {
				List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(selectQuery);
				if(queryForList.isEmpty())
				{
					logger.info("No row found");
				}else {
					
					for (Map<String, Object> row : queryForList)
					{
						if(row.get("msisdn")==null||row.get("msisdn").toString().isEmpty())
						{
							continue;
						}else
						{
							String nameUrl= env.getProperty("NAME_URL");
							logger.info("nameUrl="+nameUrl);
							logger.info("msisdn="+row.get("msisdn"));
							String jsonBodyData = "{\r\n" +
									" \"mobile\": \"" +row.get("msisdn") +"\"\r\n" +
									              
					                "}";
							logger.info("jsonBodyData="+jsonBodyData);
							PostClientServiceImpl postClientService = new PostClientServiceImpl();
							String postClientRes=postClientService.sendPostClientReq(nameUrl, jsonBodyData,"name");
							logger.info("third Party Profile Response="+postClientRes);
							if(!postClientRes.equalsIgnoreCase("-1"))
							{
								String[] nameArr= postClientRes.split(" ");
								logger.info("ArrLength="+nameArr.length);
								logger.info("name1="+nameArr[0]);
								String insertQuery= "insert into SUBSCRIBER_NAME_DETAILS (subscriber_id,name1) value ("+"'"+row.get("msisdn")+"'"+","+"'"+nameArr[0]+"'"+")";
								logger.info("insertQuery="+insertQuery);
								try {
										int insertQueryResult= jdbcTemplate.update(insertQuery);
										if(insertQueryResult <= 0)
										{
											logger.error("Failed to insert|query="+insertQuery);
										}else {
											logger.info("Successfully to insert|resultChangesRow="+insertQueryResult);
										}
										String deleteQuery="delete from CRBT_USER_LIST where msisdn='"+row.get("msisdn")+"'";
										logger.info("deleteQuery="+deleteQuery);
										int deleteQueryResult = jdbcTemplate.update(deleteQuery);
										if (deleteQueryResult <= 0) {
											logger.error("Failed to delete into Inbox table");
										} else {
											logger.info("Successfully to  delete into CRBT_USER_LIST table|resultChangesRow" + deleteQueryResult);
										}
									} catch (Exception e) {
										logger.error("SQL Exception" + e + "Query=" + insertQuery);
										logger.error("No Row Insert into  SQL43_INSERT_IVR_CDR");
										e.printStackTrace();
									}
								}
						}
					}
				}
			
			
		}catch(Exception e) {
			logger.error("SQL Exception" + e +"Query="+selectQuery);
			logger.error("No Row Found");
			
		}
		
		return "";
	}
}
