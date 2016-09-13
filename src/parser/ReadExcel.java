package parser;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.log4j.Logger;

public class ReadExcel {
	
	static final String ExcelFilePath = "D:\\eclipse\\workspace\\Assesment project\\";
	static final String ExcelFileExtension = ".xls";
	static final String RatesPropFilePath = "D:\\eclipse\\workspace\\Assesment project\\rates.properties";
	static final String TotalCostLogFilePath = "D:\\eclipse\\workspace\\Assesment project\\Total_Cost_Log.txt";
	static final String BackupDirPath = "D:\\eclipse\\workspace\\Assesment project\\Archives\\";
	static final int SheetNum = 0;
	static final int CellLookUp = 2;
	static final String AvoidText = "CC";
	static final Logger logger = Logger.getLogger(ReadExcel.class);

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		// Create an instance of FileFilter class
	    FileFilter ff = new FileFilter();
	    
	    // Ask for the file name
	    System.out.println("Enter file name: ");
	    
	    @SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
	    
	    String filename= scanner.nextLine();
	    
	    // use finder method to get array of Files
	    File[] files = ff.finder(ExcelFilePath, filename);
	    
	    // If 'files' is empty
	    if (files == null || files.length == 0) {
	          logger.fatal("File not found.");
	    } else {
	        // Iterate over all found files
	        for (@SuppressWarnings("unused") File f : files) {
	            // f is pointing to excel file
	        	String newFileName = filename + ExcelFileExtension;
		
				// Create an object of File class that points to the Excel file
				File excel = new File(ExcelFilePath + newFileName);		
				
				// From SQLInsert
				SQLInsert sql = new  SQLInsert();
				Connection conn = sql.getConnection(); 
				
		        // Initialize Date for logging
		        Date myDate = new Date();
		        
		        // Format for logging
		        FastDateFormat fdf = FastDateFormat.getInstance("yyy-MM-dd HH:mm:ss");
				String myDateString = fdf.format(myDate);
				
				// FOrmat for backups
				FastDateFormat fdf1 = FastDateFormat.getInstance("yyy-MM-dd");
				String dirDateString = fdf1.format(myDate);
		
				// Create an object for File input stream (pipeline to the xls file)
				// Since I'm using Java io, this requires me to do try/catch statement on the below code block
				FileInputStream fis = null;
				
				try {
					fis = new FileInputStream(excel);
					logger.debug("Excel file opened.");
				} catch (FileNotFoundException e) {
					logger.fatal("Cannot open file. Are you sure the file is in directory?", e);
				}
				
				if(fis == null){
					logger.error("Unable to obtain file handle.");
				}
		
				// Initialize workbook
				HSSFWorkbook wb = null;
				
				// Define the Excel Workbook
				// Since I'm using Java io, this requires me to do try/catch statement on the below code block
				try {
					wb = new HSSFWorkbook(fis);
					logger.debug("Excel Workbook opened.");
				} catch (IOException e) {
					logger.error("Unable to open Excel Workbook.", e);
				}
				// Define the Excel Sheet (sheet 0 = first sheet; doesn't matter what name it has)
				HSSFSheet ws = wb.getSheetAt(SheetNum);
				if(ws == null){
					logger.error("No sheet found.");
				}
			
				// Find the number of rows (+1) because the first row is blank.
				int rowNum = ws.getLastRowNum() + 1; 
				
				// Initialize Hash map
				HashMap<String, String> excelMap = new HashMap<String, String>();
					
				// Iterate through all rows
				for (int i = 6; i < rowNum-1; i++){
					HSSFRow row = ws.getRow(i);
		
					// Iterate through all columns
					HSSFCell cCol = ws.getRow(i).getCell(CellLookUp); //COL C in Excel file
					
					// If cell is NOT null then
					if(cCol != null){
						// Set cell type as String
						cCol.setCellType(Cell.CELL_TYPE_STRING);
						
						// Do a string compare, if the cell contains whatever the "AvoidText" contains then advance to the next row
						if(cCol.getStringCellValue().equals(AvoidText)){
							i++;
						}else{ // else put the values of Column A and Column C in the hash map
							excelMap.put(row.getCell(0).getStringCellValue() , cCol.getStringCellValue());
						}
					}
				}
				
				// "key" contains the names and map.get(key) contains the hours
				for (Map.Entry<String, String> entry : excelMap.entrySet()){
					@SuppressWarnings("unused")
					String key = entry.getKey();
				}
				
				// Open the rates file
				FileInputStream fstream = null;
				try {
					fstream = new FileInputStream(RatesPropFilePath);
					logger.debug("Properties file opened.");
				} catch (FileNotFoundException e) {
					logger.error("Cannot open file. Are you sure the file is in directory?", e);
				}
				
				@SuppressWarnings("unused")
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				
				/* Initialize the properties file I can use the Properties type because it automatically will format it as a 
				 * key/value which will be easier for me to compare with the key/value from the excel file */
				Properties properties = new Properties();
				
		        // Initialize EmployeeInfo class to store the values there (Used a class because it's centralized and much easier to comprehend with the set and get).
		        EmployeeInfo employee = new EmployeeInfo();
		        
		        // Define SQL Statement
		        
		        String batchDate = "INSERT INTO batchdate (Date) VALUES " 
		        		+ "(?)" 
		        		+ " ON DUPLICATE KEY UPDATE " 
		        		+ "Date=(?);";
		        
		        // Insert a rows into the BatchDate table for archive purposes
		        PreparedStatement pstmt;
		        ResultSet generatedKeys = null;
		        
		        try{
		        	pstmt = conn.prepareStatement(batchDate, Statement.RETURN_GENERATED_KEYS);
		        	pstmt.setString(1, dirDateString);
		        	pstmt.setString(2, dirDateString);
		        	
			        // Execute the statement
					pstmt.executeUpdate();
					logger.debug("Inserting to batchdate table... SUCCESS!");
					
					generatedKeys = pstmt.getGeneratedKeys();
					generatedKeys.next();
					int key = generatedKeys.getInt(1);
					generatedKeys.close();
					employee.setGeneratedKey(key);
					
				} catch (SQLException e) {
					logger.error("Inserting to batchdate table... FAILED!", e);
				}		
		        
		        // Get last generated key from the previous SQL Statement and save it temporarily into the program
		        //PreparedStatement bId = conn.prepareStatement(, Statement.RETURN_GENERATED_KEYS);
		        
				
				// Initialize hours, rates, totalCostPerEmployee, and a temp variable
				Double hours;
				Double rates;
				Double totalCostPerEmployee;
				Double temp = 0.0;
				
				try {
					properties.load(fstream);
					Iterator<Object> it = properties.keySet().iterator();
					
					// If there's another entry then iterate to it
					while(it.hasNext()){
						String key = (String) it.next();
						
						// Initialize a comparison iterator to go through the excel map to compare with the properties keys/values
						Iterator<String> comparisonIt = excelMap.keySet().iterator();
						
						while(comparisonIt.hasNext()){
							String excelKey = (String) comparisonIt.next();
							
							// Split the whole string using the delimiter ", "
							String[] parts = excelKey.split(", ");
							
							// String comparison between excel and properties file (if match then multiply numbers).
							if(parts[parts.length - 1].equals(key)){
								
								// Parts[0] is the first name, and Parts[1] is the last name
								hours = Double.parseDouble(excelMap.get(excelKey)); // Convert hours from Strings to Double
								//System.out.println("Hours = " + hours);
								rates = Double.parseDouble(properties.getProperty(key)); // Convert rates from Strings to Double
								totalCostPerEmployee = hours * rates; // Get total cost per employee
								
								// Iterate and add total cost per employee to calculate overall total cost
								temp = temp + totalCostPerEmployee;
						        
						        // Store first name
						        employee.setFirstName(parts[0]);
						        // Store last name
						        employee.setLastName(parts[1]);
						        // Store hours
						        employee.setHours(hours);
						        // Store rates
						        employee.setRate(rates);
						        // Store total cost per employee
						        employee.setTotalCost(totalCostPerEmployee);
						        
						        // Initialize MySQL statement
						        PreparedStatement pstmt2 = null;
						        
						        // Create Statements that will get executed
						        String employeeInfo = "INSERT INTO employeeinfo (FirstName, LastName, Hours, Rate, TotalCost, BatchDateId) "
						        		+ "VALUES(?, ?, ?, ?, ?, ?) "
						        		+ "ON DUPLICATE KEY UPDATE "
						        		+ "Hours=(?), Rate=(?), TotalCost=(?), BatchDateId=(?);";
						        
						        try{
						        	pstmt2 = conn.prepareStatement(employeeInfo);
							        pstmt2.setString(1, employee.getFirstName());
							        pstmt2.setString(2, employee.getLastName());
							        pstmt2.setDouble(3, employee.getHours());
							        pstmt2.setDouble(4, employee.getRate());
							        pstmt2.setDouble(5, employee.getTotalCost());
							        if (employee.getGeneratedKey() != 0){
							        	 pstmt2.setInt(6, employee.getGeneratedKey());
							        	 pstmt2.setInt(10, employee.getGeneratedKey());
							        }
							        pstmt2.setDouble(7, employee.getHours());
							        pstmt2.setDouble(8, employee.getRate());
							        pstmt2.setDouble(9, employee.getTotalCost());
							       
						        
							        // Execute the statement
									pstmt2.executeUpdate();
									logger.debug("Inserting to employeeinfo table... SUCCESS!");
								} catch (SQLException e) {
									logger.error("Inserting to employeeinfo table... FAILED!", e);
								}
						        
						        // Insert a rows into the BatchDate table for archive purposes
						        //PreparedStatement pstmt3 = null;
						        
/*						        String updateBatchDateId = "UPDATE "
						        		+ "FROM employeeinfo ei "
						        		+ "INNER JOIN batchdate bd "
						        			+ "ON ei.BatchDateId = bd.Id "
					        			+ "SET ei.BatchDateId = bd.Id "
					        			+ "WHERE "
					        			+ "FirstName='?', "
					        			+ "LastName='?';";*/
						        //String updateBatchDateID = ""
						        
/*						        try{
						        	pstmt3 = conn.prepareStatement(updateBatchDateId);
							        pstmt3.setString(1, employee.getFirstName());
							        pstmt3.setString(2, employee.getLastName());
						        
							        System.out.println("SQLStatement: " + pstmt3);
							        // Execute the statement
									pstmt3.executeUpdate();
									logger.debug("Updating employeeinfo table... SUCCESS!");
								} catch (SQLException e) {
									logger.error("Updating batchdate table... FAILED!", e);
								}*/
							}
						}
					}
					
			        // Store total cost for all employees
			        employee.setTotalCostAllEmployees(temp);
			        
			        // Check if the total cost file already exists
			        File TCFile = new File(TotalCostLogFilePath);
			        if(!TCFile.exists()) {
			        	TCFile.createNewFile();
			        }
			        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(TCFile, true), "utf-8"))) {
			        	logger.debug("Creating text file... SUCCESS!");
			        	
			        	// Write to file
			        	writer.write("(" + myDateString + ")" + "(" + newFileName + ") Total cost for all employees: " + employee.getTotalCostAllEmployees());
			        	writer.newLine();
			        	
			        	logger.debug("Writing to file... SUCCESS!");
			        	
			        	// Close file
			        	writer.close();
			        	} catch (IOException e){
			        		logger.error("Writing to file... FAILED!", e);
			        	}
					
				} catch (IOException e1) {
					logger.error("Couldn't execute program.", e1);
				}
				
				// Close connection
				finally {
					if (conn != null){
						try {
							conn.close();
							logger.debug("Closing connection to database... SUCCESS!");
							}catch (SQLException e) {
								logger.error("Closing connection to database... FAILED!", e);
							}
						} 
					}
				
				// Rename and move the Excel file to a backup folder for archive purposes
				Path moveFrom = FileSystems.getDefault().getPath(ExcelFilePath + filename + ExcelFileExtension);
		        Path target = FileSystems.getDefault().getPath(BackupDirPath + filename + " (" + dirDateString + ")" + ExcelFileExtension);
		        
		        try {
		        	Files.move(moveFrom, target, StandardCopyOption.REPLACE_EXISTING);
		            logger.debug("Successfully moved file to: " + target);
		        } catch (IOException e) {
		            logger.error("Something went wrong moving the file.", e);
		        }
	        }
	    }
		System.out.println("End of program.");
		}
}