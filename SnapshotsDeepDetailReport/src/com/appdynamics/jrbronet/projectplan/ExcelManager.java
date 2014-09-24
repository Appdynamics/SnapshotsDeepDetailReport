package com.appdynamics.jrbronet.projectplan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelManager {
	private File excelFile = null;
	private XSSFWorkbook book = null;
	FileInputStream fsIP = null;
	
	public ExcelManager(String fileName){
		excelFile = new File(fileName);			
		try {
			fsIP = new FileInputStream(this.excelFile);
			book = new XSSFWorkbook(fsIP); //Access the workbook
		} catch (Exception e) {			
			e.printStackTrace();
		} //Read the spreadsheet that needs to be updated        
       
	}	
	
	public void saveChangesAndClose(String fileName){
		try {
			// First refresh all calculated formulas and cells
			XSSFFormulaEvaluator.evaluateAllFormulaCells(this.book);
			// Set the date to today save and close
			Date myDate = new Date();
			XSSFCell myCell = book.getSheetAt(1).getRow(3).getCell(5);
			// code that assigns a cell from an HSSFSheet to 'myCell' would go here...
			myCell.setCellValue(myDate);
			
			fsIP.close(); //Close the InputStream        
	        FileOutputStream output_file =new FileOutputStream(fileName); //Open FileOutputStream to write updates	         
	        book.write(output_file); //write changes	          
	        output_file.close();  //close the stream 
			} catch (Exception e) {			
				e.printStackTrace();
		}     
       
	}	
	
	private static void copyRow(XSSFWorkbook workbook, XSSFSheet sourceWorksheet, int sourceRowNum, XSSFSheet destinationWorksheet, int destinationRowNum) {
        // Get the source / new row
		XSSFRow newRow = destinationWorksheet.getRow(destinationRowNum);
        XSSFRow sourceRow = sourceWorksheet.getRow(sourceRowNum);
        
        // If the row exist in destination, push down all rows by 1 else create a new row
        
        if (newRow != null) {
        	destinationWorksheet.shiftRows(destinationRowNum, destinationWorksheet.getLastRowNum(), 1);
        } else {
            newRow = destinationWorksheet.createRow(destinationRowNum);
        }
        

        // Loop through source columns to add to new row
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
        	// Grab a copy of the old/new cell
            XSSFCell oldCell = sourceRow.getCell(i);
            XSSFCell newCell = newRow.createCell(i);
            
            // If the old cell is null jump to next cell
            if (oldCell == null) {
                newCell = null;
                continue;
            }
                       

            // Copy style from old cell and apply to new cell
            XSSFCellStyle newCellStyle = workbook.createCellStyle();
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
            ;
            newCell.setCellStyle(newCellStyle);

            // If there is a cell comment, copy
            if (oldCell.getCellComment() != null) {
                newCell.setCellComment(oldCell.getCellComment());
            }

            // If there is a cell hyperlink, copy
            if (oldCell.getHyperlink() != null) {
                newCell.setHyperlink(oldCell.getHyperlink());
            }

            // Set the cell data type
            newCell.setCellType(oldCell.getCellType());
            
            //newCell.setCellValue(oldCell.getRawValue());
            //newCell.setCellType(oldCell.getCellType());                        
            
            // Set the cell data value
            switch (oldCell.getCellType()) {
                case Cell.CELL_TYPE_BLANK:
                    newCell.setCellValue(oldCell.getStringCellValue());
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    newCell.setCellValue(oldCell.getBooleanCellValue());
                    break;
                case Cell.CELL_TYPE_ERROR:
                    newCell.setCellErrorValue(oldCell.getErrorCellValue());
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    newCell.setCellFormula(oldCell.getCellFormula());
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    newCell.setCellValue(oldCell.getNumericCellValue());
                    break;
                case Cell.CELL_TYPE_STRING:
                    newCell.setCellValue(oldCell.getRichStringCellValue());
                    break;
            }

        }

        // If there are are any merged regions in the source row, copy to new row
        /*
        for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = worksheet.getMergedRegion(i);
            if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
                CellRangeAddress newCellRangeAddress = new CellRangeAddress(newRow.getRowNum(),
                        (newRow.getRowNum() +
                                (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow()
                                        )),
                        cellRangeAddress.getFirstColumn(),
                        cellRangeAddress.getLastColumn());
                worksheet.addMergedRegion(newCellRangeAddress);
            }            
        }
        */
    }
	
	public void AddUpdateAppLicenseData(String appName, HashMap licCount){
		// Locate where to write, if you find a cell with the same name on the app that this one the update, if not, as you get an empty cell create it
		// For copying cells use copyRow() capable of copying full rows
		// A new app can be copied from the worksheet DataValidationValues rows 50-57
		
		try{
			
	          
	        XSSFSheet worksheet = book.getSheetAt(1); //Accesing the second tab
	        XSSFSheet dataValworksheet = book.getSheetAt(2); //Accesing the second tab
	          
	        Cell cell = null; // declare a Cell object
	        
	        int i=0;		       
	        boolean found = false;	        	        
	        
	        if(worksheet.getRow(13) != null){
		        cell = worksheet.getRow(13).getCell(0);   // Access the second cell in second row to update the value	          
		        String thisAppName = cell.getStringCellValue();	        
		        //System.out.println("Cell Contents: "+thisAppName);
		        
		        
		        for(i = 0 ; worksheet.getRow(13+(i*8))!=null ; i++){	        	
		        	cell = worksheet.getRow(13+(i*8)).getCell(0);   // Access the second cell in second row to update the value	          
			        thisAppName = cell.getStringCellValue();	        
			        //System.out.println("Cell Contents: "+thisAppName);
			        if(thisAppName.equals(appName)){
			        	// Update agent lic usage
			        	System.out.println("Updating "+appName);
			        	found=true;
			        	for(int k=0;k<8;k++){
			        		String type =  worksheet.getRow(13+(i*8)+k).getCell(3).getStringCellValue();
			        		Integer lics = (Integer)licCount.get(type);
			        		if(lics!=null)
			        			worksheet.getRow(13+(i*8)+k).getCell(4).setCellValue(lics);
			        	}
			        }
		        }
	        }
	        
	        if(!found){
	        	// Create a new entry
	        	System.out.println("Creating "+appName);	        	
	        	for(int j = 0; j< 8; j++){
	        		copyRow(book, dataValworksheet, 50+j, worksheet,(13+i*8)+j);	
	        		//worksheet.getRow(13+(i*8)+j).getCell(2).setCellValue("=B"+(13+(i*8)));
	        		worksheet.getRow(13+(i*8)+j).getCell(2).setCellFormula("B"+(14+(i*8)));
	        		String type =  worksheet.getRow(13+(i*8)+j).getCell(3).getStringCellValue();
	        		//System.out.println("type: "+type);
	        		Integer lics = (Integer)licCount.get(type);
	        		//System.out.println("ExcelManager - "+type+": "+lics+" "+licCount.size());
	        		if(lics!=null)
	        			worksheet.getRow(13+(i*8)+j).getCell(4).setCellValue(lics);
	        	}
	        	worksheet.getRow(13+(i*8)).getCell(0).setCellValue(appName);	        		        	
	        }
	        	        	        
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
