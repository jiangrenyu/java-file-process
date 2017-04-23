package com.bonc.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftpPreprocess.FileProcess;

public class FileUtils {

	private static Logger LOG = LoggerFactory.getLogger(FileUtils.class);

	/**
	 * 对文件进行处理
	 * 
	 * @param inputpath
	 * @param outPath
	 * @param regex
	 * @param replaceRegex
	 */
	public static void process(String inputpath, String outputPath, String regex, String replaceRegex,
			String outputFormat, List<String> logList) {
		if (inputpath.endsWith(".rar")) {
			try {
				String tmp = "tmp";

				String name1 = UncompressionTools.unRarFile(inputpath, outputPath, outputFormat, logList);// 得到一个中间文件名称

				String name2 = FileUtils.deleteAndReplace(name1, outputPath, regex, replaceRegex, tmp);// 中间文件

				FileUtils.deleteFile(name1);

				FileUtils.rename(name2, name1);
			} catch (Exception e) {
				LOG.info("文件" + inputpath + "处理异常");
				e.printStackTrace();
			}
		} else if (inputpath.endsWith(".gz")) {
			UncompressionTools.unGzipFile(inputpath, outputPath, regex, replaceRegex, outputFormat, logList);
		} else {
			try {
				FileUtils.deleteAndReplace(inputpath, outputPath, regex, replaceRegex, "");
			} catch (Exception e) {
				LOG.info("文件" + inputpath + "处理异常");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 删除文件首行，替换文件分割符
	 * 
	 * @param inputPath
	 *            输入路径
	 * @param outPath
	 *            输出路径
	 * @param regex
	 *            被替换的分隔符
	 * @param replaceRegex
	 *            替换的分隔符
	 * @throws Exception
	 */
	public static String deleteAndReplace(String inputPath, String destPath, String regex, String replaceRegex,
			String tmp) throws Exception {

		BufferedReader br = null;
		BufferedWriter bw = null;
		String outputPath = null;
		File inputFile = null;
		File outFile = null;
		String targetRegex = null;

		if (inputPath != null && destPath != null) {
			try {
				String separator = System.getProperty("file.separator");
				inputFile = new File(inputPath);
				outputPath = destPath.endsWith(separator) ? destPath : (destPath + separator);
				outFile = new File(outputPath + tmp + inputFile.getName());
				targetRegex = new String2Hex(replaceRegex).toString();

				br = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(inputFile))));
				bw = new BufferedWriter(new FileWriter(outFile));
				int deleteLine = 1;
				int lineNum = 0;
				String inputStr = null;
				String outStr = null;
				while ((inputStr = br.readLine())!= null) {
					boolean b1 = inputStr.trim().isEmpty();
					boolean b2 = inputStr.trim().length() == 0;

					if (b1 || b2) {
						continue;
					}

					lineNum++;

					if (lineNum == deleteLine) {
						continue;
					}

					outStr = inputStr.replace(regex, targetRegex);
					bw.write(outStr);
					bw.newLine();
					bw.flush();
				}
			} catch (Exception e) {
				LOG.info("处理: " + inputPath + "文件内容出现异常");
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						LOG.info("处理文件" + inputPath + "内容时关闭流出现异常");
						e.printStackTrace();
					}
				}
				if (bw != null) {
					try {
						bw.close();
					} catch (IOException e) {
						LOG.info("处理文件" + inputPath + "内容时关闭流出现异常");
						e.printStackTrace();
					}
				}

			}

		} else {
			throw new Exception("处理文件内容时无法找到输入路径：" + inputPath + "无法找到输出路径：" + destPath);
		}

		return outFile.getAbsolutePath();

	}

	/**
	 * 获取目录下的文件信息(无法实现文件后缀过滤)
	 * 
	 * @param srcPath
	 * @return
	 * @throws Exception
	 */
	public static void listFile(List<String> fileList, String srcPath) {
		File file = new File(srcPath);
		if (file.exists() && file.isDirectory() && file != null) {
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				for (File f : files) {
					if (f.isDirectory()) {
						listFile(fileList, f.getAbsolutePath());
					} else {
						fileList.add(f.getAbsolutePath());
					}
				}
			}
		} else {
			LOG.info("获取输入目录:" + srcPath + "下文件信息失败");
			throw new RuntimeException();
		}
	}

	/**
	 * 删除文件，修改文件名字
	 * 
	 * @param inputPath
	 *            中间文件
	 * @param outPath
	 *            最终文件
	 */
	public static void rename(String inputPath, String outPath) {
		File srcFile = new File(inputPath);
		File desFile = new File(outPath);

		if (!srcFile.exists()) {
			LOG.error("文件" + inputPath + "不存在");
		}

		if (desFile.exists()) {
			LOG.error("文件" + outPath + "存在,无法改名");
		}

		if (srcFile.renameTo(desFile)) {
			LOG.info("修改文件名称:" + inputPath + "成功");
		} else {
			LOG.info("修改文件名称:" + inputPath + "失败");
		}
	}

	/**
	 * 删除中间文件
	 * 
	 * @param inputPath
	 * @param outPath
	 */
	public static void deleteFile(String inputPath) {
		File file = new File(inputPath);
		if (file.exists()) {
			if (file.delete()) {
				LOG.info("删除中间文件:" + inputPath + "成功");
			} else {
				LOG.info("删除中间文件:" + inputPath + "失败");
			}
		} else {
			LOG.info("中间文件" + inputPath + "不存在");
		}

	}

}
