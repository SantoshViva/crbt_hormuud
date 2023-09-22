package com.santosh.greenzone.wapchatezee.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.PostRemove;
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

import com.santosh.greenzone.services.impl.PostClientCrbtServiceImpl;
import com.santosh.greenzone.services.impl.PostClientServiceImpl;
import com.santosh.greenzone.utils.ChatUtils;



@CrossOrigin
@RestController
public class PrayerTimeController {

	private static final Logger logger = LogManager.getLogger(PrayerTimeController.class);
	@Autowired
	Environment env;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	@RequestMapping(value = "/prayerTimeUpdate",method = RequestMethod.GET)
	@ResponseBody
	public String sdpToneChangeHandler(@RequestParam("startTime1") String startTime1,@RequestParam("endTime1") String endTime1,
			@RequestParam(required = false) String startTime2,@RequestParam(required = false) String endTime2,
			@RequestParam(required = false) String startTime3,@RequestParam(required = false) String endTime3,
			@RequestParam(required = false) String startTime4,@RequestParam(required = false) String endTime4,
			@RequestParam(required = false) String startTime5,@RequestParam(required = false) String endTime5,
			HttpServletRequest req,
			HttpServletResponse res) {
		
			logger.info("prayerTimeUpdate|startTime1="+startTime1+"|endTime1="+endTime1+"|startTime2="+startTime2+"|endTime2="+endTime2
					+"|startTime3="+startTime3+"|endTime3="+endTime3+
					"|startTime4="+startTime4+"|endTime4="+endTime4+
					"|startTime5="+startTime5+"|endTime5="+endTime5);
			String queryType="1";
			String returnValue="fail";
			if(startTime1 == null || endTime1 == null || startTime2 == null || endTime2 == null || startTime3 == null || endTime3 == null || startTime4 == null || endTime4 == null || startTime5 == null || endTime5 == null)
			{
				logger.info("Null value is coming ");
			}
			
		    try {
		    	String updateQuery = ChatUtils.updatePrayerTimeQuery(env.getProperty("SQL45_UPDATE_PRAYER_TIME_INTERVAL"),startTime1,endTime1,startTime2,endTime2,startTime3,endTime3,startTime4,endTime4,startTime5,endTime5);
			    logger.info("updateQuery="+updateQuery);	
		    	int updateResult=jdbcTemplate.update(updateQuery);
		    	if(updateResult<=0)
		    	{
		    		logger.error("Fail to update subscriber profile|result="+updateResult);
		    		returnValue="fail";
		    	}else
		    	{
		    		logger.info("Successful update prayer time interval|result="+updateResult);
		    		returnValue="success";
		    	}
		    }catch(Exception e)
		    {
		    	logger.info("Exception="+e);
		    }
			
		    
			return returnValue;
		
	}
	
	
}
