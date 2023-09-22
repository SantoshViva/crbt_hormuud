package com.santosh.greenzone.wapchatezee.controller;


import java.util.ArrayList;

import java.util.List;
import java.util.Map;



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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;



import com.santosh.greenzone.utils.ChatUtils;
import com.santosh.greenzone.wapchatezee.model.MyCategorySubCategory;
import com.santosh.greenzone.wapchatezee.model.ResponseDTO;



@CrossOrigin
@RestController
public class CrbtCatSubCatDetails {

	private static final Logger logger = LogManager.getLogger(CrbtCatSubCatDetails.class);
	@Autowired
	Environment env;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	@RequestMapping(value = "/crbtCatSubCatDetails",method = RequestMethod.GET)
	@ResponseBody
	public String crbtCatSubCatInfo(@RequestParam("aparty") String aparty,@RequestParam("bparty") String bparty,@RequestParam("categoryId") String categoryId,@RequestParam("subCategoryId") String subCategoryId,@RequestParam("contentCount") String contentCount,HttpServletRequest req,
			HttpServletResponse res) {
		
		    
			logger.info("crbtCatSubCatInfo|aparty="+aparty+"|bparty="+bparty+"|categoryId="+categoryId+"|subCategoryId="+subCategoryId+"|contentCount="+contentCount);
			String responseString = new String();
			int counter=0;
			String dbError ="N";
			String selectQuery="";
			String name1="default",name2="default",name3="default",name4="default";
			if(contentCount==null)
			{
				contentCount="10";
			}
			if(env.getProperty("APARTY_NUMBER_TRIM").equalsIgnoreCase("Y"))
			{
				logger.info("Aparty number trim");
				int apartyLength=Integer.parseInt(env.getProperty("APARTY_NUMBER_LENGTH"));
				if(aparty.length()>apartyLength)
				{
					aparty= aparty.substring(aparty.length()-apartyLength);
					logger.info("getToneIdInforXML|aparty="+aparty+"|modify aparty="+aparty+"|bpartyLength="+apartyLength);
				}
			}else {
				logger.info("No Aparty number trim");
			}
			
			
			if(categoryId.equalsIgnoreCase("NameTune") || categoryId.equalsIgnoreCase("Name_Tune")||categoryId.equalsIgnoreCase("Name_Tone"))
			{
				String selectNameQuery = ChatUtils.getSubsNameDetailsQuery(env.getProperty("SQL36_SELECT_SUB_NAME_DETAILS"),aparty);
				logger.info("final Query="+selectNameQuery);
				
				try {
					List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(selectNameQuery);
					if(queryForList.isEmpty())
					{
							/**No record found. Error handling here*/
						logger.info("Name_Tone|No Record in Name Table|subscriberId="+aparty);
						responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songCount=\'"+"0"+"\';");
						responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.dbError=\'"+dbError+"\';");
						return responseString;
					}else 
					{
						for (Map<String, Object> row : queryForList) 
						{
							
							
						    if(row.get("name1")==null||row.get("name1").toString().isEmpty())
							{
								
								name1="default";
								
							}else
							{
								
								name1=row.get("name1").toString();
							}
						    if(row.get("name2")==null||row.get("name2").toString().isEmpty())
							{
								
								
								name2="default";
								
							}else
							{
								name2=row.get("name2").toString();
							}
						    if(row.get("name3")==null||row.get("name3").toString().isEmpty())
							{
						    	
								
								name3="default";
								
							}else
							{
								name3=row.get("name3").toString();
							}
						    if(row.get("name4")==null||row.get("name4").toString().isEmpty())
							{
						    	
								
								name4="default";
							}else
							{
								name4=row.get("name4").toString();
							}
						    break;
						}
						
						
					}
					
				}catch(Exception e)
				{
					logger.error("SQL Exception" + e +"Query="+selectNameQuery);
					logger.error("No Row Found");
					responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songCount=\'"+"0"+"\';");
					dbError="Y";
					name1="default";
					e.printStackTrace();
					responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.dbError=\'"+dbError+"\';");
					return responseString;
					
				}
				
				selectQuery = ChatUtils.getNameTuneDetailsQuery(env.getProperty("SQL36_SELECT_NAME_TUNE_DETAILS"),categoryId,name1,name2,name3,name4,contentCount);
				
			}
			else if(categoryId.equalsIgnoreCase("Album"))
			{
				logger.info("Album Information");
				selectQuery = ChatUtils.getCatSubCatQuery(env.getProperty("SQL36_SELECT_ALBUM_DETAILS"), categoryId,subCategoryId,contentCount);
				logger.info("AlbumQuery="+selectQuery);
				try {
					List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(selectQuery);
					if(queryForList.isEmpty())
					{
						responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songCount=\'"+"0"+"\';");
					}
					else
					{
						int innerCounter=0;
						String albumId="";
						for (Map<String, Object> row : queryForList) {
							Integer intCounterLocal = new Integer(counter);
							Integer intInnerCounter= new Integer(innerCounter);
							albumId=row.get("albumid").toString();
							String songPath=row.get("toneid1").toString();
							songPath=songPath.substring(0,2)+"/"+songPath.substring(2,4)+"/"+songPath.substring(4,7)+"/"+songPath+"/"+songPath+"_audio.wav";
							logger.info("songid="+row.get("songid")+"|songname="+row.get("songname")+"|songpath="+songPath);
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songId["+intInnerCounter.toString()+"]"+"=\'"+row.get("toneid1")+"\';");
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songName["+intInnerCounter.toString()+"]"+"=\'"+row.get("tonename1")+"\';");
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songPath["+intInnerCounter.toString()+"]"+"=\'"+songPath+"\';");
							innerCounter++;
							intInnerCounter= new Integer(innerCounter);
							songPath=row.get("toneid2").toString();
							songPath=songPath.substring(0,2)+"/"+songPath.substring(2,4)+"/"+songPath.substring(4,7)+"/"+songPath+"/"+songPath+"_audio.wav";
							logger.info("songid="+row.get("songid")+"|songname="+row.get("songname")+"|songpath="+songPath);
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songId["+intInnerCounter.toString()+"]"+"=\'"+row.get("toneid2")+"\';");
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songName["+intInnerCounter.toString()+"]"+"=\'"+row.get("tonename2")+"\';");
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songPath["+intInnerCounter.toString()+"]"+"=\'"+songPath+"\';");
							innerCounter++;
							intInnerCounter= new Integer(innerCounter);
							songPath=row.get("toneid3").toString();
							songPath=songPath.substring(0,2)+"/"+songPath.substring(2,4)+"/"+songPath.substring(4,7)+"/"+songPath+"/"+songPath+"_audio.wav";
							logger.info("songid="+row.get("songid")+"|songname="+row.get("songname")+"|songpath="+songPath);
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songId["+intInnerCounter.toString()+"]"+"=\'"+row.get("toneid3")+"\';");
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songName["+intInnerCounter.toString()+"]"+"=\'"+row.get("tonename3")+"\';");
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songPath["+intInnerCounter.toString()+"]"+"=\'"+songPath+"\';");
							innerCounter++;
							intInnerCounter= new Integer(innerCounter);
							songPath=row.get("toneid4").toString();
							songPath=songPath.substring(0,2)+"/"+songPath.substring(2,4)+"/"+songPath.substring(4,7)+"/"+songPath+"/"+songPath+"_audio.wav";
							logger.info("songid="+row.get("songid")+"|songname="+row.get("songname")+"|songpath="+songPath);
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songId["+intInnerCounter.toString()+"]"+"=\'"+row.get("toneid4")+"\';");
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songName["+intInnerCounter.toString()+"]"+"=\'"+row.get("tonename4")+"\';");
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songPath["+intInnerCounter.toString()+"]"+"=\'"+songPath+"\';");
							innerCounter++;
							counter++;
						}
						Integer intCounter = new Integer(innerCounter);
						responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.albumId=\'"+albumId+"\';");
						responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songCount=\'"+intCounter.toString()+"\';");
						}
					}catch(Exception e)
					{
						logger.error("SQL Exception" + e +"Query="+selectQuery);
						logger.error("No Row Found");
						responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songCount=\'"+"0"+"\';");
						dbError="Y";
						e.printStackTrace();
					}
				responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.dbError=\'"+dbError+"\';");
				return responseString;
				}
			else {
				selectQuery = ChatUtils.getCatSubCatQuery(env.getProperty("SQL36_SELECT_CAT_SUBCAT_DETAILS"), categoryId,subCategoryId,contentCount);
			}
			
			
			
			
			//logger.info("final Query="+selectQuery);
			
			try {
				List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(selectQuery);
				if(queryForList.isEmpty())
				{
						/**No record found. Error handling here*/
					if(categoryId.equalsIgnoreCase("NameTune") || categoryId.equalsIgnoreCase("Name_Tune")||categoryId.equalsIgnoreCase("Name_Tone"))
					{
						logger.info("Name_Tone|No Name Tune Available|Name="+name1);
					}	
					responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songCount=\'"+"0"+"\';");
				}
				else 
				{
					for (Map<String, Object> row : queryForList) {
						Integer intCounterLocal = new Integer(counter);
						
					    if(row.get("songid")==null||row.get("songname")== null||row.get("songid").toString().isEmpty()||row.get("songname").toString().isEmpty())
						{
							logger.error("Category & sub category songs are null in database|aparty="+aparty);
							continue;
						}else
						{	
							String songPath=row.get("songid").toString();
							songPath=songPath.substring(0,2)+"/"+songPath.substring(2,4)+"/"+songPath.substring(4,7)+"/"+songPath+"/"+songPath+"_audio.wav";
							logger.info("songid="+row.get("songid")+"|songname="+row.get("songname")+"|songpath="+songPath);
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songId["+intCounterLocal.toString()+"]"+"=\'"+row.get("songid")+"\';");
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songName["+intCounterLocal.toString()+"]"+"=\'"+row.get("songname")+"\';");
							responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songPath["+intCounterLocal.toString()+"]"+"=\'"+songPath+"\';");
							
						}
					    counter++;
					   }
						Integer intCounter = new Integer(counter);
						responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songCount=\'"+intCounter.toString()+"\';");
				}
				
			}catch(Exception e) {
				
				logger.error("SQL Exception" + e +"Query="+selectQuery);
				logger.error("No Row Found");
				responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.songCount=\'"+"0"+"\';");
				dbError="Y";
				e.printStackTrace();
			}
			
			responseString = responseString.concat("CRBT_CAT_SUBCAT_RES.dbError=\'"+dbError+"\';");
			return responseString;
		
	}
	
	
}
