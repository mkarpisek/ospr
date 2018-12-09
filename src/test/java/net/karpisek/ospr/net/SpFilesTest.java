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
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.jdom2.JDOMException;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.karpisek.ospr.Ospr;

public class SpFilesTest {
	static String toUnixString(Path path) {
		//just get rid of all windows back slashes, use for testing normal forward slashes in paths
		return path.toString().replace("\\", "/");
	}
	
	@Test
	public void test1() throws IOException, JDOMException, InterruptedException, TimeoutException, ExecutionException {
		List<String> expected = Lists.newArrayList();
		expected.add("preVisitFolder=documents");
		expected.add("visitFile=test.docx");
		expected.add("visitFile=test.pdf");
		expected.add("visitFile=test.pptx");
		expected.add("visitFile=test.txt");
		expected.add("visitFile=test.xlsx");
		expected.add("preVisitFolder=test1");
		expected.add("preVisitFolder=test11");
		expected.add("visitFile=test11a.txt");
		expected.add("visitFile=test1a.txt");
		expected.add("visitFile=test1b.txt");
		expected.add("visitFile=test1c.docx");
		expected.add("visitFile=test1d.xlsx");
		expected.add("visitFile=test1e.pptx");
		expected.add("preVisitFolder=test2");
		expected.add("visitFile=test2a.txt");
		
		List<String> actual = Lists.newArrayList();
		SpFiles.walkFileTree(new LocalSpObjectProvider(), "src/test/resources/test1site/documents", 0, Ospr.UNLIMITED_DEPTH, new ISpFileVisitor() {
			
			@Override
			public void visitFile(SpFile file) throws IOException {
				actual.add("visitFile="+file.getName());
			}
			
			@Override
			public void preVisitFolder(SpFolder folder) throws IOException {
				actual.add("preVisitFolder="+folder.getName());
			}
			
			@Override
			public void postVisitFolder(SpFolder folder) throws IOException {
				
			}
		});
		assertEquals(Joiner.on("\n").join(expected), Joiner.on("\n").join(actual));
	}	
	
	@Test
	public void testLimitedDepth_0() throws IOException, JDOMException, InterruptedException, TimeoutException, ExecutionException {
		List<String> expected = Lists.newArrayList();
		expected.add("preVisitFolder=documents");
		expected.add("visitFile=test.docx");
		expected.add("visitFile=test.pdf");
		expected.add("visitFile=test.pptx");
		expected.add("visitFile=test.txt");
		expected.add("visitFile=test.xlsx");
		
		List<String> actual = Lists.newArrayList();
		SpFiles.walkFileTree(new LocalSpObjectProvider(), "src/test/resources/test1site/documents", 0, 0, new ISpFileVisitor() {
			
			@Override
			public void visitFile(SpFile file) throws IOException {
				actual.add("visitFile="+file.getName());
			}
			
			@Override
			public void preVisitFolder(SpFolder folder) throws IOException {
				actual.add("preVisitFolder="+folder.getName());
			}
			
			@Override
			public void postVisitFolder(SpFolder folder) throws IOException {
				
			}
		});
		assertEquals(Joiner.on("\n").join(expected), Joiner.on("\n").join(actual));
	}
	
	@Test
	public void testLimitedDepth_1() throws IOException, JDOMException, InterruptedException, TimeoutException, ExecutionException {
		List<String> expected = Lists.newArrayList();
		expected.add("preVisitFolder=documents");
		expected.add("visitFile=test.docx");
		expected.add("visitFile=test.pdf");
		expected.add("visitFile=test.pptx");
		expected.add("visitFile=test.txt");
		expected.add("visitFile=test.xlsx");
		expected.add("preVisitFolder=test1");
		expected.add("visitFile=test1a.txt");
		expected.add("visitFile=test1b.txt");
		expected.add("visitFile=test1c.docx");
		expected.add("visitFile=test1d.xlsx");
		expected.add("visitFile=test1e.pptx");
		expected.add("preVisitFolder=test2");
		expected.add("visitFile=test2a.txt");
		
		List<String> actual = Lists.newArrayList();
		SpFiles.walkFileTree(new LocalSpObjectProvider(), "src/test/resources/test1site/documents", 0, 1, new ISpFileVisitor() {
			
			@Override
			public void visitFile(SpFile file) throws IOException {
				actual.add("visitFile="+file.getName());
			}
			
			@Override
			public void preVisitFolder(SpFolder folder) throws IOException {
				actual.add("preVisitFolder="+folder.getName());
			}
			
			@Override
			public void postVisitFolder(SpFolder folder) throws IOException {
				
			}
		});
		assertEquals(Joiner.on("\n").join(expected), Joiner.on("\n").join(actual));
	}	
}