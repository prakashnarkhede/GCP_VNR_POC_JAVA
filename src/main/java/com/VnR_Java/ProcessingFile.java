package com.VnR_Java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class ProcessingFile {

	////
	//// check about parsing the json file from  google cloud pubsub
	/////
	/////
	public String[] getBucketConsumerAndFileName(String JsonString) throws FileNotFoundException, IOException, ParseException {
       // Object obj = new JSONParser().parse(new FileReader(jsonFile)); 
		
		JSONParser parser = new JSONParser();
		JSONObject jo = (JSONObject) parser.parse(JsonString);        
        //name attribute returns value in format: folderName (if any)/fileName (with extension)
        String nameAttribute = (String) jo.get("name"); 

      //name attribute returns value in format: folderName (if any)/fileName (with extension)
        String bucket = (String) jo.get("bucket"); 
        
        
        //split folderName and file name
        String[] nameArray = nameAttribute.split("/");
        String ConsumerFolderName = nameArray[0];
        String IO_FolderName = nameArray[1];
        String fileName = nameArray[2];
        
        String[] returnArray = new String[4];
        returnArray[0] = bucket;       
        returnArray[1] = ConsumerFolderName;
        returnArray[2] = IO_FolderName;
        returnArray[3] = fileName;


		return returnArray;
		
	}  
	@SuppressWarnings("deprecation")
	public void readAndProcessFile(String[] BucketConsumerFileName) throws Exception {
		String bucketName = BucketConsumerFileName[0];
		String consumerFolderName = BucketConsumerFileName[1];
		String IO_folderName = BucketConsumerFileName[2];
		String fileName = BucketConsumerFileName[3];
		System.out.println("BucketName is: "+bucketName);
		System.out.println("consumerFolderName is: "+consumerFolderName);
		System.out.println("IO_FolderName is: "+IO_folderName);
		System.out.println("fileName is: "+fileName);
		
		//*********************************************************************************//
		//Get the file to be processed in temp folder, delete it from google cloud bucket
		//*********************************************************************************//

		//////was not sure about what is directory structure on after deployment on kubernates, so creating directry
		//
		//******* improvement --- clean both directories after processing
		 new File("/tempFolder/InputFile").mkdirs();
		 new File("/tempFolder/OutputFile").mkdirs();

		
		
		Storage storage = null;
		Blob blob;
		BlobId blobId = null;
		try{
		// Instantiate a Google Cloud Storage client
		 storage = StorageOptions.getDefaultInstance().getService();
		// Get specific file from specified bucket
		 blobId = BlobId.of(bucketName, consumerFolderName+"/"+IO_folderName+"/"+fileName);
		 blob = storage.get(blobId);
		// Download file to specified path
		blob.downloadTo(Paths.get(System.getProperty("user.dir")+"//tempFolder/InputFile"+"/"+fileName));
		} catch(Exception e) {
			System.out.println("Exception while getting file from google cloud bucket. More details ---- "+e.getMessage());
		}
		finally {
			//delete file from GCP bucket after recieved for processing
			try{
		    	storage.delete(blobId);	    	
			} catch(Exception e) {
				System.out.println("Exception while deleting file from google cloud bucket. More details ---- "+e.getMessage());
			}
		}
		
		
		ProcessingFile c = new ProcessingFile();
		
		File OutPutfile = new File(System.getProperty("user.dir")+"//tempFolder/OutputFile/Out_"+fileName);
		BufferedWriter writer = new BufferedWriter(new FileWriter(OutPutfile));

		
		
		
		try {
			List<String> allLines = Files.readAllLines(Paths.get(System.getProperty("user.dir")+"//tempFolder/InputFile"+"/"+fileName));
			for (String line : allLines) {
				//getCountryCodeValue("IN");
				System.out.println(line.toUpperCase());
			    writer.write(line.toUpperCase());
			    writer.write("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}			
		    if (writer != null) writer.close();		
		    
		    
		    //upload the processed output file to GCP Bucket
		    InputStream content = new FileInputStream(new File(System.getProperty("user.dir")+"//tempFolder/OutputFile/Out_"+fileName));
		    BlobId blobId1 = BlobId.of("ctct_vnr_bucket_output", consumerFolderName+"/"+"OutputFile"+"/Out_"+fileName);
		    BlobInfo blobInfo = BlobInfo.newBuilder(blobId1).setContentType("text/plain").build();
		    storage.create(blobInfo, content);
		    
	}
	
		
		
	String JoTR1 = "";
	String JoTR2 = "";	
	String JoTR3 = "";	
	String JoTR4 = "";	
	String JoTR5 = "";
	String JoTR1_warning = "    ";
	String JoTR2_warning = "    ";
	String JoTR3_warning = "    ";
	String JoTR4_warning = "    ";
	String JoTR5_warning = "    ";
	
	public Boolean getCountryCodeValue(String country) throws Exception {
		String jdbcUrl = String.format(
			    "jdbc:mysql://%s/%s?cloudSqlInstance=%s"
			        + "&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false",
			"35.222.70.99",
			"ctcuDB", "maximal-ship-242013:us-central1:ctcu-vnr-tables");

			//	Class.forName("com.mysql.jdbc.Driver"); 
				Connection connection = DriverManager.getConnection(jdbcUrl, "root", "root");
			 String query = "SELECT * FROM ctry_cde where Country_Code = '"+country+"';";
			      Statement st = connection.createStatement();
			      ResultSet rs = st.executeQuery(query);
			      while (rs.next())
			      {
			        // return true if country code found in table
			        return true;
			      }
			      //returns false if country is invalid / not present in table
				return false;

	}
	
	public StringBuilder jotrValidation(String record) throws SQLException, Exception {
		ProcessingFile c = new ProcessingFile();

		StringBuilder respData = new StringBuilder(record);
		
		//delete file from google storage
		//bucket name--> ctct_vnr_bucket       blobName (Item to delete) --> Consumer1/Batch_Input_File_C1.txt
		
		if(record.startsWith("A")) {			//header processing
		}
		
		if(record.startsWith("B")) {
			JoTR1 = record.substring(167, 170);
			JoTR2 = record.substring(213, 216);
			JoTR3 = record.substring(259, 262);
			JoTR4 = record.substring(305, 308);
			JoTR5 = record.substring(351, 354);
			System.out.println(JoTR1);

			if(JoTR1 != "    ") {
				if(!c.getCountryCodeValue(JoTR1)) {
					JoTR1_warning = "V202";
					respData.replace(170, 173, JoTR1_warning);
					JoTR1_warning = "    ";
				}
			} else {
				JoTR1_warning = "V201";
				respData.replace(170, 173, JoTR1_warning);
			}
			
			if(JoTR2 != "    ") {
				if(!c.getCountryCodeValue(JoTR2)) {
					JoTR2_warning = "V202";
					respData.replace(217, 220, JoTR2_warning);
					JoTR2_warning = "    ";
				}
			} else {
				JoTR2_warning = "V201";
				respData.replace(217, 220, JoTR2_warning);
			}
			if(StringUtils.isNotBlank(JoTR3.trim())) {
				if(!c.getCountryCodeValue(JoTR3)) {
					JoTR3_warning = "V202";
					respData.replace(264, 267, JoTR3_warning);
					JoTR3_warning = "    ";
				}
			} else {
				JoTR3_warning = "V201";
				respData.replace(264, 267, JoTR3_warning);
			}			
		}
		if(record.startsWith("Z")) {			
		}
		return respData;			
	}
	
	public void writeOutputFileToBucket(String bucketName, String ConsumerFolderName) {
		
	}
	public static void main(String[] args) {

	}
	public void sendOutputFileToBucket() {
		
	}

}
