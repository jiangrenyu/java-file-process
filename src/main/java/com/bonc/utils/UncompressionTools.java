package com.bonc.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

public class UncompressionTools {

	private static Logger LOG = LoggerFactory.getLogger(UncompressionTools.class);

	/**
	 * 解压rar文件
	 * 
	 * @param sourceRar
	 *            输入目录
	 * @param destDir
	 *            输出目录
	 * @return
	 * @throws Exception
	 */
	public static String unRarFile(String sourceRar, String destDir, String fileFormat, List<String> logList) {

		Archive archive = null;
		FileOutputStream fos = null;

		File sourceFile = null;
		File destFileName = null;

		try {
			sourceFile = new File(sourceRar);

			try {
				archive = new Archive(sourceFile);
			} catch (RarException e) {
				LOG.info("解压rar文件:  " + sourceRar + "异常");
				logList.add("文件名称:  " + sourceRar);
				e.printStackTrace();
			}

			FileHeader fh = archive.nextFileHeader();

			while (fh != null) {

				String fileName = sourceFile.getName().substring(0, sourceFile.getName().indexOf(".rar"));
				String separator = System.getProperty("file.separator");
				String remote = new File(destDir).getAbsolutePath();
				remote = remote.endsWith(separator) ? remote : (remote + separator);
				destFileName = new File(remote + fileName + fileFormat);

				// 如果压缩的是一个文件夹
				if (fh.isDirectory()) {
					if (!destFileName.exists()) {
						destFileName.mkdirs();
					}
					fh = archive.nextFileHeader();
					continue;
				}

				fos = new FileOutputStream(destFileName);

				try {
					archive.extractFile(fh, fos);
				} catch (RarException e) {
					LOG.info("解压rar文件:  " + sourceRar + "异常");
					logList.add("文件名称:  " + sourceRar);
					e.printStackTrace();
				}

				fh = archive.nextFileHeader();
			}

		} catch (Exception e) {
			LOG.info("解压rar文件:  " + sourceRar + "异常");
			logList.add("文件名称:  " + sourceRar);
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
					fos = null;
				} catch (Exception e) {
					LOG.info("解压rar文件:  " + sourceRar + "关闭流异常");
					e.printStackTrace();
				}
			}
			if (archive != null) {
				try {
					archive.close();
					archive = null;
				} catch (Exception e) {
					LOG.info("解压rar文件:  " + sourceRar + "关闭流异常");
					e.printStackTrace();
				}
			}
		}
		return destFileName.getAbsolutePath();
	}

	/**
	 * 解压gz文件
	 * 
	 * @param inputPath
	 * @param outputPath
	 * @param oldRegex
	 * @param replaceRegex
	 * @param outputFormat
	 */
	public static void unGzipFile(String inputPath, String outputPath, String oldRegex, String replaceRegex,
			String outputFormat, List<String> logList) {

		GZIPInputStream gzin = null;
		BufferedReader br = null;
		BufferedWriter bw = null;
		String targetRegex = null;
		File inputFile = null;
		File outputFile = null;
		String outputFileName = null;
		String outputFilePath = null;
		String outputTmpname = null;

		try {
			inputFile = new File(inputPath);
			String separator = System.getProperty("file.separator");
			// 做一个判断，判断文件中有无 ".txt "
			String inputPathSub = inputPath.substring(0, inputPath.indexOf(".gz"));

			if (inputPathSub.endsWith(".txt")) {
				outputTmpname = inputFile.getName().substring(0, inputFile.getName().indexOf(".txt.gz"));
			} else {
				outputTmpname = inputFile.getName().substring(0, inputFile.getName().indexOf(".gz"));
			}

			outputFilePath = outputPath.endsWith(separator) ? outputPath : (outputPath + separator);

			outputFileName = outputFilePath + outputTmpname + outputFormat;
			outputFile = new File(outputFileName);
			try {
				gzin = new GZIPInputStream(new FileInputStream(inputFile));
			} catch (IOException e) {
				LOG.info("解压gz文件:  " + inputPath + "异常");
				logList.add("文件名称:  " + inputPath);
				e.printStackTrace();
			}

			br = new BufferedReader(new InputStreamReader(new BOMInputStream(gzin)));

			bw = new BufferedWriter(new FileWriter(outputFile));

			targetRegex = new String2Hex(replaceRegex).toString();

			String intputLine = null;
			String outputStr = null;
			int deleteLine = 1;
			int lineNum = 0;

			while ((intputLine = br.readLine()) != null) {

				boolean b1 = intputLine.trim().isEmpty();
				boolean b2 = intputLine.trim().length() == 0;

				if (b1 || b2) {
					continue;
				}

				lineNum++;

				if (lineNum == deleteLine) {
					continue;
				}

				outputStr = intputLine.replace(oldRegex, targetRegex);
				bw.write(outputStr);
				bw.newLine();
				bw.flush();
			}

		} catch (Exception e) {
			LOG.info("解压gz文件:  " + inputPath + "异常");
			logList.add("文件名称:  " + inputPath);
			e.printStackTrace();
		} finally {
			try {
				gzin.close();
				br.close();
				bw.close();
			} catch (IOException e) {
				LOG.info("解压gz文件:  " + inputPath + "关闭流异常");
				e.printStackTrace();
			}
		}
	}
}
