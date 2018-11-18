/*******************************************************************************
 * Copyright (c) 2018 Martin Karpisek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Martin Karpisek <martin.karpisek@gmail.com> - initial API and implementation 
 *******************************************************************************/
package net.karpisek.ospr.report;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import net.karpisek.ospr.net.ISpFileVisitor;
import net.karpisek.ospr.net.ISpObjectProvider;
import net.karpisek.ospr.net.SpFile;
import net.karpisek.ospr.net.SpFiles;
import net.karpisek.ospr.net.SpFolder;

public class SpFileTreeReporter{
	private static final Logger LOG = LoggerFactory.getLogger(SpFileTreeReporter.class);
	
	private Path outputPath;
	private ISpObjectProvider objectProvider;
	private String path;
	private int maxDepth;

	public SpFileTreeReporter(ISpObjectProvider objectProvider, String path, int maxDepth, Path outputPath) {
		this.outputPath = outputPath;
		this.objectProvider = objectProvider;
		this.maxDepth = maxDepth;
		this.path = path;
	}
	
	public void execute() throws IOException, InterruptedException, TimeoutException, ExecutionException, JDOMException {
		LOG.info("fileTreeWalk dir={} maxDepth={}", path, maxDepth);
		AtomicInteger folders = new AtomicInteger(0);
			
		try(XSSFWorkbook  workbook = new XSSFWorkbook()){
	        XSSFSheet sheet = workbook.createSheet("files");
	        AtomicInteger rowNum = new AtomicInteger(0);
	        {
	        	Row row = sheet.createRow(rowNum.getAndIncrement());
	        	int colNumber = 0;
	        	for (String colName : Lists.newArrayList("sp.name", "sp.serverRelativeUrl", "sp.timeLastModified", "sp.timeCreated", "sp.length", "sp.version")) {
	        		row.createCell(colNumber++).setCellValue(colName);
				}
	        }
			SpFiles.walkFileTree(objectProvider, path, 0, maxDepth, new ISpFileVisitor() {
				@Override
				public void visitFile(SpFile file) throws IOException {
					int colNumber = 0;
					Row row = sheet.createRow(rowNum.getAndIncrement());
					row.createCell(colNumber++).setCellValue(file.getName());
					row.createCell(colNumber++).setCellValue(file.getServerRelativeUrl());
					createTimestampCell(row, colNumber++, file.getTimeLastModified());
					createTimestampCell(row, colNumber++, file.getTimeCreated());
					row.createCell(colNumber++).setCellValue(file.getLength());
					row.createCell(colNumber++).setCellValue(file.getVersion().toString());
				}
	
				@Override
				public void preVisitFolder(SpFolder folder) throws IOException {
					//TODO: counts of files and folders could be cached already on SpFolder instance creation time
					LOG.info(
							"{}. {} (folders={} files={})", 
							folders.getAndIncrement(), 
							folder.getServerRelativeUrl(), 
							folder.getChildren(each -> each.isFolder()).count(),
							folder.getChildren(each -> each.isFile()).count()
							);
				}
	
				@Override
				public void postVisitFolder(SpFolder folder) throws IOException {
					//nothing to do
				}
			});
			
			for (int i = sheet.getRow(0).getFirstCellNum(); i < sheet.getRow(0).getLastCellNum(); i++) {
				sheet.autoSizeColumn(i);				
			}
			
	        try (OutputStream outputStream = Files.newOutputStream(outputPath)){
	            workbook.write(outputStream);
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        }
		LOG.info("fileTreeWalkDone output={} folders={}", outputPath.toAbsolutePath(), folders.get());
	}
	
	private Cell createTimestampCell(Row row, int colNumber, Instant timestamp) {
		Workbook wb = row.getSheet().getWorkbook();
		CellStyle cellStyle = wb.createCellStyle();
		CreationHelper createHelper = wb.getCreationHelper();
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
		Cell cell = row.createCell(colNumber);
		cell.setCellValue(Date.from(timestamp));
		cell.setCellStyle(cellStyle);
		return cell;
	}
}
