package com.santosh.greenzone.wapchatezee.controller;

/***
 * This is for Tone Player
 * 9-Feb-2023 change for Prayer RBT
 */
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.core.env.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.santosh.greenzone.utils.ChatUtils;
import com.santosh.greenzone.wapchatezee.model.ToneInfo;


@CrossOrigin
@RestController
public class UserToneInfoXML {
	private static final Logger logger = LogManager.getLogger(UserToneInfoXML.class);
	
	@Autowired
	Environment env;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	@RequestMapping(value = "/getToneIdInfoXml",method = RequestMethod.GET)
	@ResponseBody
	public String getToneIdInforXML(@RequestParam("aparty") String aparty, @RequestParam("bparty") String bparty,@RequestParam(required=false) String apartyRbtCheck, HttpServletRequest req,
			HttpServletResponse res) {
		
		logger.info("getToneIdInforXML|aparty=" + aparty+"|bparty="+bparty+"|apartyRbtCheck="+apartyRbtCheck);
		//logger.info("Query=" + env.getProperty("SQL23_USER_TONE_INFO"));
		//logger.info("Operator=" + env.getProperty("OPERATOR_NAME"));
		String apartyRBTStatus="N";
		String apartyToneId="";
		if(env.getProperty("BPARTY_NUMBER_TRIM").equalsIgnoreCase("Y"))
		{
			logger.trace("Bparty number trim");
			int bpartyLength=Integer.parseInt(env.getProperty("BPARTY_NUMBER_LENGTH"));
			if(bparty.length()>bpartyLength)
			{
				bparty= bparty.substring(bparty.length()-bpartyLength);
				logger.info("getToneIdInforXML|aparty="+aparty+"|modify bparty="+bparty+"|bpartyLength="+bpartyLength);
			}
		}else {
			logger.info("No Bparty number trim");
		}
		logger.trace("Tone Id for No Record Found|Default_Tone_Id=" + env.getProperty("NOT_CRBT_DEFAULT_TONE_ID"));
		/*Start Check aparty Status*/
		if(env.getProperty("APARTY_NUMBER_TRIM").equalsIgnoreCase("Y"))
		{
			logger.trace("Aparty number trim");
			int apartyLength=Integer.parseInt(env.getProperty("APARTY_NUMBER_LENGTH"));
			if(aparty.length()>apartyLength)
			{
				aparty= aparty.substring(aparty.length()-apartyLength);
				logger.info("getToneIdInforXML|aparty="+aparty+"|modify aparty="+aparty+"|bpartyLength="+apartyLength);
			}
		}else {
			logger.info("No Aparty number trim");
		}
		if(apartyRbtCheck != null || apartyRbtCheck == "Y")
		{
			String apartyQuery = ChatUtils.getQuery(env.getProperty("SQL46_APARTY_CRBT_CHECK"), aparty);
			try {
				List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(apartyQuery);
				if(queryForList.isEmpty())
				{
					logger.error("No Record Found in SQL|aparty="+aparty+" is not RBT subscriber|defaultTone="+env.getProperty("NOT_CRBT_DEFAULT_TONE_ID"));
					apartyRBTStatus="N";
					
				}else
				{
					apartyRBTStatus="Y";
					for (Map<String, Object> row : queryForList)
					{
						if(row.get("tone_id")!=null)
						{
							apartyToneId=row.get("tone_id").toString();
							logger.info("getToneIdInforXML|aparty="+aparty+"|apartyToneId="+apartyToneId);
						}
					}
					
				}
			}catch(Exception e)
			{
				logger.error("Db error occurrect="+e);
			}
		}
		
		
		
		/*End***/
		// Replace Table Index & bparty 
		String query = ChatUtils.getQuery(env.getProperty("SQL23_USER_TONE_INFO"), bparty);
		
		//logger.info("final SQL Query="+query);
		ToneInfo toneInfoDetails = new ToneInfo();
		toneInfoDetails.setToneId(env.getProperty("NOT_CRBT_DEFAULT_TONE_ID"));
		toneInfoDetails.setCallingParty("D");
		toneInfoDetails.setContentType("N");
		toneInfoDetails.setSongName("No_DB");
		toneInfoDetails.setSongPath("songPath");
		toneInfoDetails.setServiceId("default");
		String breakFlag ="N";
		String albumFlag ="N";
		String specialCaller ="N";
		String prayerTimeFlag ="Y";
		String albumId ="";
		String nameTune ="N";
		String toneTypeValue="-1";
		try {
			List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(query);
			
			if(queryForList.isEmpty())
			{
				logger.error("No Record Found in SQL for bparty|aparty="+aparty+"|bparty="+bparty+"|defaultTone="+env.getProperty("NOT_CRBT_DEFAULT_TONE_ID"));
				toneInfoDetails.setStatus("N");	
				
			}else 
			{
				for (Map<String, Object> row : queryForList) {
					
					toneInfoDetails.setStatus(row.get("status").toString());
					String toneType="0";
					String callingParty="0";
					if(row.get("tone_type")!=null)
						toneType=row.get("tone_type").toString();
					if(row.get("calling_party")!=null)
						callingParty=row.get("calling_party").toString();
					toneInfoDetails.setCallingParty(row.get("calling_party").toString());
					if(row.get("service_id")!=null)
					{
						toneInfoDetails.setServiceId(row.get("service_id").toString());
					}
					if(toneType.equalsIgnoreCase(env.getProperty("PRAYER_TONE_TYPE").toString()))
					{
						
						/*For Prayer RBT*/
						/***Check current time from database*/
						String prayerStartTime1=env.getProperty("PRAYER_START_TIME_1").toString();
						String prayerStartTime2=env.getProperty("PRAYER_START_TIME_2").toString();
						String prayerStartTime3=env.getProperty("PRAYER_START_TIME_3").toString();
						String prayerStartTime4=env.getProperty("PRAYER_START_TIME_4").toString();
						String prayerStartTime5=env.getProperty("PRAYER_START_TIME_5").toString();
						String prayerEndTime1=env.getProperty("PRAYER_END_TIME_1").toString();
						String prayerEndTime2=env.getProperty("PRAYER_END_TIME_2").toString();
						String prayerEndTime3=env.getProperty("PRAYER_END_TIME_3").toString();
						String prayerEndTime4=env.getProperty("PRAYER_END_TIME_4").toString();
						String prayerEndTime5=env.getProperty("PRAYER_END_TIME_5").toString();
						
						
						
						String selectPrayerTimeInterval = env.getProperty("SQL45_SELECT_PRAYER_TIME_INTERVAL");
						
						try {
							
							List<Map<String, Object>> queryPlayerForList = jdbcTemplate.queryForList(selectPrayerTimeInterval);
							if(queryPlayerForList.isEmpty())
							{
								logger.info("no record in database");
							}else {
								for (Map<String, Object> rowPlayer : queryPlayerForList)
								{
									if(rowPlayer.get("start_time_1")==null || rowPlayer.get("end_time_1")==null)
									{
										
									}else if (rowPlayer.get("start_time_2")==null || rowPlayer.get("end_time_2")==null)
									{
										
									}else if (rowPlayer.get("start_time_3")==null || rowPlayer.get("end_time_3")==null)
									{
										
									}else if (rowPlayer.get("start_time_4")==null || rowPlayer.get("end_time_4")==null)
									{
										
									}else if (rowPlayer.get("start_time_5")==null || rowPlayer.get("end_time_5")==null)
									{
										
									}else
									{
										prayerStartTime1=rowPlayer.get("start_time_1").toString();
										prayerEndTime1=rowPlayer.get("end_time_1").toString();
										prayerStartTime2=rowPlayer.get("start_time_2").toString();
										prayerEndTime2=rowPlayer.get("end_time_2").toString();
										prayerStartTime3=rowPlayer.get("start_time_3").toString();
										prayerEndTime3=rowPlayer.get("end_time_3").toString();
										prayerStartTime4=rowPlayer.get("start_time_4").toString();
										prayerEndTime4=rowPlayer.get("end_time_4").toString();
										prayerStartTime5=rowPlayer.get("start_time_5").toString();
										prayerEndTime5=rowPlayer.get("end_time_5").toString();
										logger.info("prayerStartTime1="+prayerStartTime1+"|prayerEndTime1="+prayerEndTime1+"|prayerStartTime2"+prayerStartTime2+"|prayerEndTime2="+prayerEndTime2);
										logger.info("prayerStartTime3="+prayerStartTime3+"|prayerEndTime3="+prayerEndTime3+"|prayerStartTime4"+prayerStartTime4+"|prayerEndTime4="+prayerEndTime4);
										logger.info("prayerStartTime5="+prayerStartTime5+"|prayerEndTime5="+prayerEndTime5);
										
									}
								}
							}
						}catch(Exception e)
						{
							logger.error("Error occurred|exception="+e);
						}
						
						
						/**Check current time belong with configuration time*/
						
						
						
						LocalTime starTime1= LocalTime.parse(prayerStartTime1);
						
						LocalTime endTime1= LocalTime.parse(prayerEndTime1);
			
						
						LocalTime starTime2= LocalTime.parse(prayerStartTime2);
						
						LocalTime endTime2= LocalTime.parse(prayerEndTime2);
						
						
						LocalTime starTime3= LocalTime.parse(prayerStartTime3);
						
						LocalTime endTime3= LocalTime.parse(prayerEndTime3);
						
						
						LocalTime starTime4= LocalTime.parse(prayerStartTime4);
						
						LocalTime endTime4= LocalTime.parse(prayerEndTime4);
												
						
						LocalTime starTime5= LocalTime.parse(prayerStartTime5);
						
						LocalTime endTime5= LocalTime.parse(prayerEndTime5);
						
						DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
						LocalTime currTime = LocalTime.now();
						String currTimeDetails = dtf.format(currTime);
						LocalTime compareCurrTime =LocalTime.parse(currTimeDetails);
						
						if((compareCurrTime.isAfter(starTime1) && compareCurrTime.isBefore(endTime1))||(compareCurrTime.isAfter(starTime2) && compareCurrTime.isBefore(endTime2))||(compareCurrTime.isAfter(starTime3) && compareCurrTime.isBefore(endTime3))||(compareCurrTime.isAfter(starTime4) && compareCurrTime.isBefore(endTime4))||(compareCurrTime.isAfter(starTime5) && compareCurrTime.isBefore(endTime5)))
						{
							logger.info("This is prayer time so prayer RBT will play");
							prayerTimeFlag="Y";
							breakFlag="Y";
							toneTypeValue= toneType;
						}else
						{
							logger.info("Current Time is not prayer time|start1="+prayerStartTime1+"|end1="+prayerEndTime1+"|start2="+prayerStartTime2+"|end2="+prayerEndTime2+"|start3="+prayerStartTime3+"|end3="+prayerEndTime3+"|start4="+prayerStartTime4+"|end4="+prayerEndTime4+"|start5="+prayerStartTime5+"|end5="+prayerEndTime5+"|CurrTime="+currTimeDetails);
						}
						
						
						
					}else if(toneType.equalsIgnoreCase("3"))
					{
						toneTypeValue= toneType;
						nameTune="Y";
						breakFlag="Y";
					}
					else if(toneType.equalsIgnoreCase(env.getProperty("ALBUM_TONE_TYPE").toString()))
					{
						/*For Album*/
						albumFlag="Y";
						toneTypeValue= toneType;
						albumId = row.get("tone_id").toString();
						logger.info("User have Album subscriber|bparty="+bparty+"|albumId="+albumId);
						breakFlag="Y";
					}
					else if(toneType.equalsIgnoreCase(env.getProperty("SPECIAL_CALLER_TONE_TYPE").toString()))
					{
						/*For Special Caller*/
						logger.info("Special Caller Case|bparty="+bparty+"|toneType="+toneType+"|callingParty="+callingParty);
						if(callingParty.equalsIgnoreCase(aparty))
						{
							logger.info("User have Special Caller matched case");
							breakFlag="Y";
							specialCaller="Y";
							toneTypeValue= toneType;
						}else {
							logger.info("CallingParty is not matched");
						}
						
					}else {
						/*For Default Case and Name Tune*/
						logger.info("default case");
						toneTypeValue= toneType;
					}
					
					toneInfoDetails.setToneId(row.get("tone_id").toString());
					if((row.get("song_name") != null))
					{
						toneInfoDetails.setSongName(row.get("song_name").toString());
					}
					if(row.get("song_path")!= null)
					{
						toneInfoDetails.setSongPath(row.get("song_path").toString());
					}
					
					
					if(toneInfoDetails.getToneId().length()<10)
					{
						logger.trace("This is old 6d data migration|aparty="+aparty);
						/**this is for old content format**/
						toneInfoDetails.setContentType("O");
						
					}else
					{
						/**This is for new content format*/
						toneInfoDetails.setContentType("N");
					}
					
					logger.trace("QueryResult|msisdn="+bparty+"|status="+row.get("status").toString()+"|calling_party="+row.get("calling_party").toString()+"|tone_id="+row.get("tone_id").toString()+"|");
					if((toneInfoDetails.getStatus().equals("Y") && toneInfoDetails.getCallingParty().equals(aparty))||breakFlag.equals("Y"))
					{
						logger.info("Calling Party Matched");
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.error("SQL Exception" + e +"|Query="+query);
			logger.error("No Row Found");
			toneInfoDetails.setStatus("D");	
			toneInfoDetails.setToneId(env.getProperty("NOT_CRBT_DEFAULT_TONE_ID"));
			e.printStackTrace();
		}
		if(albumFlag.equalsIgnoreCase("Y")&&specialCaller.equalsIgnoreCase("N"))
		{
			logger.info("Use have album subscriber");
			String albumQuery = ChatUtils.getAlbumQuery(env.getProperty("SQL23_USER_ALBUM_INFO"), albumId);
			logger.info("albumQuery="+albumQuery);
			try 
			{
				List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(albumQuery);
				if(queryForList.isEmpty())
				{
					logger.info("Album Syncing issue | Cantent is not available");
				}
				for (Map<String, Object> row : queryForList) {
					String toneId="";
					String songName="";
					int randValue=(int)(Math.random()*(4-1+1)+1);
					logger.info("Random Number ="+randValue);
					toneInfoDetails.setContentType("N");
					//toneInfoDetails.setServiceId("Album");
					
					switch(randValue)
					{
						case 1:
							toneId=row.get("toneid1").toString();
							songName=row.get("tonename1").toString();
							break;
						case 2:
							toneId=row.get("toneid2").toString();
							songName=row.get("tonename2").toString();
							break;
						case 3:
							toneId=row.get("toneid3").toString();
							songName=row.get("tonename3").toString();
							break;
						case 4:
							toneId=row.get("toneid4").toString();
							songName=row.get("tonename4").toString();
							break;
						default :
							logger.info("dafault case");
							break;
							
					}
					toneInfoDetails.setToneId(toneId);
					toneInfoDetails.setSongName(songName);
					String songPath=toneId;
					songPath=songPath.substring(0,2)+"/"+songPath.substring(2,4)+"/"+songPath.substring(4,7)+"/"+songPath+"/"+songPath+"_audio.wav";
					toneInfoDetails.setSongPath(songPath);
					
				
				}
			}catch(Exception e)
			{
				logger.error("SQL Exception" + e +"|Query="+query);
				logger.error("No Row Found");
				toneInfoDetails.setStatus("D");	
				toneInfoDetails.setToneId(env.getProperty("NOT_CRBT_DEFAULT_TONE_ID"));
				e.printStackTrace();	
			}
			
			
		}
		
//		toneInfo.setSubscriberId("4444444444");
//		toneInfo.setToneId("1234567890");
		
		String responseString = new String();
		
		responseString = responseString.concat("RBT_RES.toneId=\'"+toneInfoDetails.getToneId()+"\';");
		responseString = responseString.concat("RBT_RES.setStatus=\'"+toneInfoDetails.getStatus()+"\';");
		responseString = responseString.concat("RBT_RES.callingParty=\'"+toneInfoDetails.getCallingParty()+"\';");
		responseString = responseString.concat("RBT_RES.status=\'"+toneInfoDetails.getStatus()+"\';");
		responseString = responseString.concat("RBT_RES.songName=\'"+toneInfoDetails.getSongName()+"\';");
		responseString = responseString.concat("RBT_RES.songPath=\'"+toneInfoDetails.getSongPath()+"\';");
		responseString = responseString.concat("RBT_RES.contentType=\'"+toneInfoDetails.getContentType()+"\';");
		responseString = responseString.concat("RBT_RES.serviceId=\'"+toneInfoDetails.getServiceId()+"\';");
		responseString = responseString.concat("RBT_RES.toneType=\'"+toneTypeValue+"\';");
		responseString = responseString.concat("RBT_RES.apartyRbt=\'"+apartyRBTStatus+"\';");
		responseString = responseString.concat("RBT_RES.apartyToneId=\'"+apartyToneId+"\';");
		Date date = new Date();
		SimpleDateFormat DateFor = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		logger.info("TP_RES|date="+DateFor.format(date)+"|aparty="+aparty+"|bparty="+bparty+"|toneType="+toneTypeValue+"|apartyRBTStatus="+apartyRBTStatus+"|responseString="+responseString);
		return responseString;
		
	}
}
