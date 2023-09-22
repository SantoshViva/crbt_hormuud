package com.santosh.greenzone.wapchatezee.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.santosh.greenzone.services.impl.PostClientCrbtServiceImpl;
import com.santosh.greenzone.utils.ChatUtils;


@CrossOrigin
@RestController
public class StarToCopyAsynHandler {
	private static final Logger logger = LogManager.getLogger(StarToCopyAsynHandler.class);
	@Autowired
	Environment env;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	@RequestMapping(value = "/starToCopyAsynHandler",method = RequestMethod.GET)
	@ResponseBody
	public String starToCopyAsynHandler(@RequestParam("aparty") String aparty, @RequestParam("bparty") String bparty,@RequestParam("serviceId") String serviceId,@RequestParam("productId") String productId,@RequestParam("toneId") String toneId,
			@RequestParam("callStartTime") String callStartTime,@RequestParam("callEndTime") String callEndTime,@RequestParam("digits") String digits,@RequestParam("apartyRbtFlag") String apartyRbtFlag,@RequestPart(required = false) String status,@RequestPart(required = false) String toneServiceId,@RequestPart(required = false) String toneProductId,@RequestPart(required = false) String isToneCharge,HttpServletRequest req,
			HttpServletResponse res) {
		//http://127.0.01:9077/starToCopyAsynHandler?aparty=9810594999&bparty=810594996&
		//toneId=&serviceId=crbt&productId=DAILY_RBT&status=&apartyRbtFlag=
		//&digits=*&callStartTime=2023-2-8%2023:41:47&callEndTime=2023-2-8%2023:41:53
		logger.info("starToCopyAsynHandler|aparty="+aparty+"|bparty="+bparty+"|serviceId="+serviceId+"|productId="+productId+"|toneId="+toneId+"|callStartTime="+callStartTime+"|callEndTime="+callEndTime+"|digits="+digits+"|apartyRbtFlag="+apartyRbtFlag+"|status="+status+"|toneServiceId="+toneServiceId+"|toneProductId="+toneProductId+"|isToneCharge="+isToneCharge);
		
		if(env.getProperty("BPARTY_NUMBER_TRIM").equalsIgnoreCase("Y"))
		{
			logger.trace("Bparty number trim");
			int bpartyLength=Integer.parseInt(env.getProperty("BPARTY_NUMBER_LENGTH"));
			if(bparty.length()>bpartyLength)
			{
				bparty= bparty.substring(bparty.length()-bpartyLength);
				logger.info("starToCopyAsynHandler|aparty="+aparty+"|modify bparty="+bparty+"|bpartyLength="+bpartyLength);
			}
		}else {
			logger.info("No Bparty number trim");
		}
		
		/*Start Check aparty Status*/
		if(env.getProperty("APARTY_NUMBER_TRIM").equalsIgnoreCase("Y"))
		{
			logger.trace("Aparty number trim");
			int apartyLength=Integer.parseInt(env.getProperty("APARTY_NUMBER_LENGTH"));
			if(aparty.length()>apartyLength)
			{
				aparty= aparty.substring(aparty.length()-apartyLength);
				logger.info("starToCopyAsynHandler|aparty="+aparty+"|modify aparty="+aparty+"|bpartyLength="+apartyLength);
			}
		}else {
			logger.info("No Aparty number trim");
		}
		
		
		
		String defaultLocalToneId="";
		String responseString="";
		if(status == null || status.isEmpty() || status=="")
		{
			status="A";
		}
		if(toneServiceId == null || toneServiceId.isEmpty() || toneServiceId=="")
		{
			toneServiceId="A";
		}
		if(toneProductId == null || toneProductId.isEmpty() || toneProductId=="")
		{
			toneProductId="A";
		}
		if(isToneCharge == null || isToneCharge.isEmpty() || isToneCharge=="")
		{
			isToneCharge="N";
		}
		
		/** Check toneId is null or not*/
		if(toneId == null || toneId.isEmpty()||toneId=="")
		{
			/*Find toneId from database for Bparty Number*/
			String defaultQuery = ChatUtils.getQuery(env.getProperty("SQL47_USER_DEFAULT_TONE_INFO"), bparty);
			logger.info("final SQL Query="+defaultQuery);
			try {
				List<Map<String, Object>> queryForDefaultList = jdbcTemplate.queryForList(defaultQuery);
				if(queryForDefaultList.isEmpty())
				{
					logger.error("No Default Tone Record Found in Database for bparty |aparty="+aparty+"|bparty="+bparty);
					toneId="-1";				
					toneId=env.getProperty("COPYTONE_DEFAULT_TONE_ID").toString();
					logger.error("No Default Tone Record Found in Database for bparty |aparty="+aparty+"|bparty="+bparty+"|default Tone for Bparty toneId="+toneId);
				}else
				{
					for (Map<String, Object> row : queryForDefaultList) {
						
						if(row.get("tone_id")!=null)
						{
							defaultLocalToneId= row.get("tone_id").toString();
							toneId=defaultLocalToneId;
							logger.info("starToCopyAsynHandler|aparty="+aparty+"|bparty="+bparty+"|bparty dbToneId="+toneId);
						}
						else
						{
							logger.error("starToCopyAsynHandler|Null toneId  in Database for bparty tonetype 0|aparty="+aparty+"|bparty="+bparty);
							toneId="-1";
							toneId=env.getProperty("COPYTONE_DEFAULT_TONE_ID").toString();
						}
					}
				}
			}catch(Exception e) {
				logger.error("SQL Exception" + e+"|aparty="+aparty+"|bparty="+bparty);
				toneId="-1";
			}
			
		}
		String starToCopy="N";
		/**To check current star to copy in queue or not**/
		String currentCount="-1";
		String selectQuery=ChatUtils.getTonePlayerAsyncDtmfInsertQuery(env.getProperty("SQL49_SELECT_ASYNC_TONE_PLAYER_DTMF"), aparty,bparty,toneId,digits,callStartTime,callEndTime,starToCopy,apartyRbtFlag,status,serviceId,productId,toneProductId,toneServiceId,isToneCharge);
		logger.info("selectQuery="+selectQuery);
		try {
			List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(selectQuery);
			if(queryForList.isEmpty())
			{
				logger.info("No row found on async tone table|allow this request|aparty="+aparty+"|baparty="+bparty+"|toneId="+toneId);
				currentCount="0";
			}else {
				for (Map<String, Object> row : queryForList)
				{
					if(row.get("count(1)")!=null)
					{
						currentCount=row.get("count(1)").toString();
						if(currentCount.equalsIgnoreCase("0"))
							logger.info("Allow this request|aparty="+aparty+"|baparty="+bparty+"|toneId="+toneId+"|currentCount="+currentCount);
						else
							logger.info("Not allow this request|already exist old request|aparty="+aparty+"|baparty="+bparty+"|toneId="+toneId+"|currentCount="+currentCount);
					}else {
						currentCount="0";
					}
					
				}
			}
			
		}catch(Exception e) {
			logger.error("SQL Exception" + e +"Query="+selectQuery);
			currentCount="-2";
		}
		/****/
		String apartyToneId="";
		if(currentCount.equalsIgnoreCase("0"))
		{
			
			logger.info("Allow this start to copy|aparty="+aparty+"|bparty="+bparty);
			
			String apartyQuery = ChatUtils.getQuery(env.getProperty("SQL46_APARTY_CRBT_CHECK"), aparty);
			try {
				List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(apartyQuery);
				if(queryForList.isEmpty())
				{
					logger.error("No Record Found in SQL|aparty="+aparty+" is not RBT subscriber|defaultTone="+env.getProperty("NOT_CRBT_DEFAULT_TONE_ID"));
					apartyRbtFlag="N";
					
				}else
				{
					apartyRbtFlag="Y";
					for (Map<String, Object> row : queryForList)
					{
						if(row.get("tone_id")!=null)
						{
							apartyToneId=row.get("tone_id").toString();
							logger.info("starToCopyAsynHandler|aparty="+aparty+"|apartyToneId="+apartyToneId+"|bparty="+bparty+"|apartyRbtFlag="+apartyRbtFlag);
						}
					}
					
				}
			}catch(Exception e)
			{
				logger.error("Db error occurrect="+e);
				apartyRbtFlag="-1";
				responseString = new String("DTFM_Res.result=\'Not Ok DB error \';");
				return responseString;
			}
			
			
		}else {
			logger.info("Not Allow this start to copy|aparty="+aparty+"|bparty="+bparty+"|toneId="+toneId);
			responseString = new String("DTFM_Res.result=\'Not Ok pending request \';");
			return responseString;
		}
		
		if(apartyRbtFlag.equalsIgnoreCase("Y"))
		{
			//Aparty is already CRBT Subscriber
			isToneCharge="Y";
		}
		if(toneId.equalsIgnoreCase(env.getProperty("COPYTONE_DEFAULT_TONE_ID").toString()))
		{
			isToneCharge="N";
		}else {
			isToneCharge="Y";
		}
			
		//Insert request in tone table 
		String insertQuery = ChatUtils.getTonePlayerAsyncDtmfInsertQuery(env.getProperty("SQL48_INSERT_ASYNC_TONE_PLAYER_DTMF"), aparty,bparty,toneId,digits,callStartTime,callEndTime,starToCopy,apartyRbtFlag,status,serviceId,productId,toneProductId,toneServiceId,isToneCharge);
		
		logger.info("final SQL Query="+insertQuery);
				
		try {
			int insertQueryResult= jdbcTemplate.update(insertQuery);
			if(insertQueryResult <= 0)
			{
				logger.error("Failed to insert into accout table");
			}else {
				logger.info("Successfully to insert into SQL48_ASYNC_TONE_PLAYER_DTMF |resultChangesRow="+insertQueryResult);
			}
			
		} catch (Exception e) {
			logger.error("SQL Exception" + e + "Query=" + insertQuery);
			logger.error("No Row Insert into  CRBT_SUBS_TONE_DTMF_INFO");
			e.printStackTrace();
		}
			
	
		responseString = new String("DTFM_Res.result=\'Ok Accepted\';");
		
	
		return responseString;
	}
}
