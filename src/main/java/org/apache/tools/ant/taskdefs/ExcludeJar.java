package org.apache.tools.ant.taskdefs;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.util.FileUtils;

/**
 * This task excludes files from JAR file and re-package JAR file. You can set
 * JAR file for exclude file list.
 * 
 * @author wadahiro
 */
public class ExcludeJar extends Task {
	private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

	private File destFile;
	private File baseFile;
	private File excludeFile;
	private File workDir;
	private boolean autoclean;

	public void setDestFile(File destFile) {
		this.destFile = destFile;
	}

	public void setBaseFile(File baseFile) {
		this.baseFile = baseFile;
	}

	public void setExcludeFile(File excludeFile) {
		this.excludeFile = excludeFile;
	}

	public void setWork(File workDir) {
		this.workDir = workDir;
	}

	public void setAutoclean(boolean autoclean) {
		this.autoclean = autoclean;
	}

	public void execute() throws BuildException {
		initClassload();

		File tmpDir = null;
		try {
			tmpDir = createTmpDir();
			log("Creating work directory " + tmpDir.getAbsolutePath());

			String excludeList = getExcludeList();
			log("Exclude list from " + excludeFile.getName() + ": ["
					+ excludeList + "]");

			// Use ant's Expand class for extract JAR file.
			PatternSet set = new PatternSet();
			set.setExcludes(excludeList);
			Expand expand = new Expand();
			expand.addPatternset(set);
			expand.expandFile(FILE_UTILS, baseFile, tmpDir);

			// Use ant's Jar class for packaging JAR file.
			Jar jar = new Jar();
			jar.setTaskName(getTaskName());
			jar.setProject(getProject());
			jar.setBasedir(tmpDir);
			jar.setDestFile(destFile);
			jar.execute();

		} catch (Throwable e) {
			throw new BuildException("File IO Error: " + e.getMessage(), e);
		} finally {
			if (autoclean && tmpDir != null && tmpDir.exists()) {
				// Use ant's Delete class for delete temp directory.
				Delete delete = new Delete();
				delete.setProject(getProject());
				delete.setTaskName(getTaskName());
				delete.setDir(tmpDir);
				delete.execute();
			}
		}
	}

	private void initClassload() {
		ClassLoader classLoader = getClass().getClassLoader();
		if (!(classLoader instanceof AntClassLoader)) {
			return;
		}

		try {
			AntClassLoader antClassLoader = (AntClassLoader) getClass()
					.getClassLoader();
			antClassLoader
					.forceLoadClass("org.apache.tools.ant.taskdefs.Expand");
			antClassLoader.forceLoadClass("org.apache.tools.ant.taskdefs.Jar");
			antClassLoader
					.forceLoadClass("org.apache.tools.ant.taskdefs.Delete");
		} catch (ClassNotFoundException e) {
			throw new BuildException(
					"Please set ant.jar to classpath of taskdef.", e);
		}
	}

	/**
	 * Create temp directory for extract "baseFile".
	 * 
	 * @return
	 */
	private File createTmpDir() {
		if (workDir == null) {
			return createDefaultTmpDir();
		}
		boolean mkdir = workDir.mkdirs();
		if (!mkdir) {
			return createDefaultTmpDir();
		} else {
			return workDir;
		}
	}

	private File createDefaultTmpDir() {
		String tmpDirName = System.getProperty("java.io.tmpdir");
		File tmpDir = new File(tmpDirName + File.separator
				+ ExcludeJar.class.getName() + "." + System.currentTimeMillis());
		return tmpDir;
	}

	/**
	 * Create exclude list from "excludeFile". "MANIFEST.MF" is excluded.
	 * 
	 * @return
	 * @throws IOException
	 */
	private String getExcludeList() throws IOException {
		JarFile jarFile = new JarFile(excludeFile);

		StringBuilder sb = new StringBuilder();

		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry jarEntry = entries.nextElement();
			if (!jarEntry.isDirectory()) {
				if (jarEntry.getName().endsWith("MANIFEST.MF")) {
					continue;
				}
				sb.append(jarEntry.getName() + ",");
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
}