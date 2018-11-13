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
package net.karpisek.ospr.net;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.jdom2.JDOMException;
import org.junit.Test;

public class SpFilesTest {
	static String toUnixString(Path path) {
		//just get rid of all windows back slashes, use for testing normal forward slashes in paths
		return path.toString().replace("\\", "/");
	}
	
	@Test
	public void test1() throws IOException, JDOMException, InterruptedException, TimeoutException, ExecutionException {
		StringWriter expected = new StringWriter();
		PrintWriter expectedPrintWriter = new PrintWriter(expected);
		expectedPrintWriter.println("preVisitFolder=src/test/resources/test1site/documents");
		expectedPrintWriter.println("preVisitFolder=src/test/resources/test1site/documents/test11");
		expectedPrintWriter.println("preVisitFolder=src/test/resources/test1site/documents/test11/test111");
		expectedPrintWriter.println("visitFile=src/test/resources/test1site/documents/test11/test11a.txt");
		expectedPrintWriter.println("preVisitFolder=src/test/resources/test1site/documents/test12");
		expectedPrintWriter.println("visitFile=src/test/resources/test1site/documents/test1a.txt");
		expectedPrintWriter.println("visitFile=src/test/resources/test1site/documents/test1b.txt");
		expectedPrintWriter.println("visitFile=src/test/resources/test1site/documents/test1c.docx");
		expectedPrintWriter.println("visitFile=src/test/resources/test1site/documents/test1d.xlsx");
		expectedPrintWriter.println("visitFile=src/test/resources/test1site/documents/test1e.pptx");
		
		StringWriter actual = new StringWriter();
		PrintWriter actualPrintWriter = new PrintWriter(actual);
		SpFiles.walkFileTree(new LocalSpObjectProvider(), "src/test/resources/test1site/documents", new ISpFileVisitor() {
			
			@Override
			public void visitFile(SpFile file) throws IOException {
				actualPrintWriter.println("visitFile="+file.getServerRelativeUrl());
			}
			
			@Override
			public void preVisitFolder(SpFolder folder) throws IOException {
				actualPrintWriter.println("preVisitFolder="+folder.getServerRelativeUrl());
			}
			
			@Override
			public void postVisitFolder(SpFolder folder) throws IOException {
				
			}
		});
		assertEquals(expected.toString(), actual.toString());
	}	
	
	@Test
	public void testFiles() throws IOException, JDOMException {
		StringWriter expected = new StringWriter();
		PrintWriter expectedPrintWriter = new PrintWriter(expected);
		expectedPrintWriter.println("preVisitDirectory=src/test/resources/test1site");
		expectedPrintWriter.println("preVisitDirectory=src/test/resources/test1site/documents");
		expectedPrintWriter.println("preVisitDirectory=src/test/resources/test1site/documents/test11");
		expectedPrintWriter.println("preVisitDirectory=src/test/resources/test1site/documents/test11/test111");
		expectedPrintWriter.println("visitFile=src/test/resources/test1site/documents/test11/test11a.txt");
		expectedPrintWriter.println("preVisitDirectory=src/test/resources/test1site/documents/test12");
		expectedPrintWriter.println("visitFile=src/test/resources/test1site/documents/test1a.txt");
		expectedPrintWriter.println("visitFile=src/test/resources/test1site/documents/test1b.txt");
		expectedPrintWriter.println("visitFile=src/test/resources/test1site/documents/test1c.docx");
		expectedPrintWriter.println("visitFile=src/test/resources/test1site/documents/test1d.xlsx");
		expectedPrintWriter.println("visitFile=src/test/resources/test1site/documents/test1e.pptx");

		StringWriter actual = new StringWriter();
		PrintWriter actualPrintWriter = new PrintWriter(actual);
		Files.walkFileTree(Paths.get("src/test/resources/test1site"), new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				actualPrintWriter.println("preVisitDirectory="+toUnixString(dir));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				actualPrintWriter.println("visitFile="+toUnixString(file));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
		
		assertEquals(expected.toString(), actual.toString());
	}	
}
