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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.santosh.greenzone.wapchatezee.model.ResponseHeader;
import com.santosh.greenzone.utils.ChatUtils;
import com.santosh.greenzone.wapchatezee.model.AlbumModel;
import com.santosh.greenzone.wapchatezee.model.MyCategorySubCategory;
import com.santosh.greenzone.wapchatezee.model.ResponseDTO;

@CrossOrigin
@RestController
public class UssdCatSubCatDetails {

	private static final Logger logger = LogManager.getLogger(UssdCatSubCatDetails.class);
	@Autowired
	Environment env;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@RequestMapping(value = "/ussdCatSubCatDetails", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> ussdCatSubCatInfo(@RequestParam(required = false) String aparty,
			@RequestParam(required = false) String bparty, @RequestParam("categoryId") String categoryId,
			@RequestParam("subCategoryId") String subCategoryId, @RequestParam("contentCount") String contentCount,
			HttpServletRequest req, HttpServletResponse res) {

		logger.info("crbtCatSubCatInfo1|categoryId=" + categoryId + "|subCategoryId=" + subCategoryId + "|contentCount="
				+ contentCount + "|aparty=" + aparty + "|bparty=" + bparty);

		if(aparty != null)
		{
			if (env.getProperty("APARTY_NUMBER_TRIM").equalsIgnoreCase("Y")) {
				logger.info("Aparty number trim");
				int apartyLength = Integer.parseInt(env.getProperty("APARTY_NUMBER_LENGTH"));
				if (aparty.length() > apartyLength) {
					aparty = aparty.substring(aparty.length() - apartyLength);
					logger.info("getToneIdInforXML|aparty=" + aparty + "|modify bparty=" + aparty + "|apartyLength="
							+ apartyLength);
				}
			} else {
				logger.info("No Bparty number trim");
			}
		}
		
		
		if (contentCount == null) {
			contentCount = "10";
		}

		/** Check Mapping Sub Category Name */
		ResponseDTO<List<MyCategorySubCategory>> response = new ResponseDTO<>();
		List<MyCategorySubCategory> contents = new ArrayList<MyCategorySubCategory>();
		String selectSubCatName = ChatUtils.getCatSubCatQuery(env.getProperty("SQL44_SELECT_SUB_CAT_MAPPING"),
				categoryId, subCategoryId, contentCount);
		logger.info("final Query=" + selectSubCatName);
		String newCatName = "";
		String newSubCatName = "";
		String dbError = "N";
		int counter = 0;
		String selectQuery = "";
		String name1 = "default", name2 = "default", name3 = "default", name4 = "default";
		ResponseHeader addHeader = new ResponseHeader();

		try {
			List<Map<String, Object>> queryForListInfo = jdbcTemplate.queryForList(selectSubCatName);
			if (queryForListInfo.isEmpty()) {
				/** No Record is available in the database **/

			} else {
				for (Map<String, Object> row : queryForListInfo) {
					newCatName = row.get("new_cat_name").toString();
					newSubCatName = row.get("new_sub_cat_name").toString();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (newCatName != "") {
			categoryId = newCatName;
			logger.info("categoryId Change| new categoryId=" + categoryId);
		}
		if (newSubCatName != "") {
			subCategoryId = newSubCatName;
			logger.info("SubCatName Change| new subCatName=" + newSubCatName);
		}
		/** NameTune Start ***/
		if (categoryId.equalsIgnoreCase("NameTune") || categoryId.equalsIgnoreCase("Name_Tune")
				|| categoryId.equalsIgnoreCase("Name_Tone")) {
			if (aparty == null || aparty.isEmpty()) {
				logger.info("aparty is null or missing");
			}
			String selectNameQuery = ChatUtils.getSubsNameDetailsQuery(env.getProperty("SQL36_SELECT_SUB_NAME_DETAILS"),
					aparty);
			logger.info("final selectNameQuery=" + selectNameQuery);

			try {
				List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(selectNameQuery);
				if (queryForList.isEmpty()) {
					/** No record found. Error handling here */
					logger.info("Name_Tone|No Record in Name Table|subscriberId=" + aparty);

					
				} else {
					for (Map<String, Object> row : queryForList) {

						if (row.get("name1") == null || row.get("name1").toString().isEmpty()) {

							name1 = "default";

						} else {

							name1 = row.get("name1").toString();
						}
						if (row.get("name2") == null || row.get("name2").toString().isEmpty()) {

							name2 = "default";

						} else {
							name2 = row.get("name2").toString();
						}
						if (row.get("name3") == null || row.get("name3").toString().isEmpty()) {

							name3 = "default";

						} else {
							name3 = row.get("name3").toString();
						}
						if (row.get("name4") == null || row.get("name4").toString().isEmpty()) {

							name4 = "default";
						} else {
							name4 = row.get("name4").toString();
						}
						break;
					}

				}

			} catch (Exception e) {
				logger.error("SQL Exception" + e + "Query=" + selectNameQuery);
				logger.error("No Row Found");
				
				dbError = "Y";
				name1 = "default";
				e.printStackTrace();
				

			}
			logger.info("name1=" + name1);
			selectQuery = ChatUtils.getNameTuneDetailsQuery(env.getProperty("SQL36_SELECT_NAME_TUNE_DETAILS"),
					categoryId, name1, name2, name3, name4, contentCount);
			logger.info("name selectQuery=" + selectQuery);

		} else if (categoryId.equalsIgnoreCase("Album")) {
			logger.info("Album Information");
			selectQuery = ChatUtils.getCatSubCatQuery(env.getProperty("SQL36_SELECT_ALBUM_DETAILS"), categoryId,subCategoryId,contentCount);
			logger.info("AlbumQuery="+selectQuery);
			try {
				List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(selectQuery);
				if(queryForList.isEmpty())
				{
					addHeader.setCode(1);
					addHeader.setMessage("No Content");
					response.setHeader(addHeader);
					logger.info("No records");
				}
				else
				{
					
					for (Map<String, Object> row : queryForList) {
						
						MyCategorySubCategory content1 = new MyCategorySubCategory();
						content1.setSongId(row.get("albumid").toString());
						content1.setSongName(row.get("tonename1").toString());
						contents.add(content1);
						
						MyCategorySubCategory content2 = new MyCategorySubCategory();
						content2.setSongId(row.get("albumid").toString());
						content2.setSongName(row.get("tonename2").toString());
						contents.add(content2);
						
						MyCategorySubCategory content3 = new MyCategorySubCategory();
						content3.setSongId(row.get("albumid").toString());
						content3.setSongName(row.get("tonename3").toString());
						contents.add(content3);
						
						MyCategorySubCategory content4 = new MyCategorySubCategory();
						content4.setSongId(row.get("albumid").toString());
						content4.setSongName(row.get("tonename4").toString());
						contents.add(content4);
						
					}
					
					response.setBody(contents);
					return new ResponseEntity<>(response, HttpStatus.OK);
				}
			}catch(Exception e)
			{
				logger.error("SQL Exception" + e +"Query="+selectQuery);
				logger.error("No Row Found");
				addHeader.setCode(2);
				addHeader.setMessage("DB Error");
				response.setHeader(addHeader);
				return new ResponseEntity<>(response, HttpStatus.OK);
				
			}
			
		} else {
			selectQuery = ChatUtils.getCatSubCatQuery(env.getProperty("SQL36_SELECT_CAT_SUBCAT_DETAILS"), categoryId,
					subCategoryId, contentCount);
			logger.info("final Query=" + selectQuery);
		}

		try

		{
			List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(selectQuery);
			if (queryForList.isEmpty()) {
				/** No record found. Error handling here */
				addHeader.setCode(1);
				addHeader.setMessage("No Content");
				response.setHeader(addHeader);
				logger.info("No records");
			} else {
				for (Map<String, Object> row : queryForList) {
					MyCategorySubCategory content = new MyCategorySubCategory();
					content.setSongId(row.get("songid").toString());
					content.setSongName(row.get("songname").toString());
					contents.add(content);
				}
			}

		} catch (Exception e) {

			logger.info("DB Error occurred|Exception=" + e);
			addHeader.setCode(2);
			addHeader.setMessage("DB Error");
			response.setHeader(addHeader);
		}

		response.setBody(contents);

		return new ResponseEntity<>(response, HttpStatus.OK);

	}
}
