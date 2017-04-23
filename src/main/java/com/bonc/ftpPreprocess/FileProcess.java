package com.bonc.ftpPreprocess;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.utils.DateUtils;
import com.bonc.utils.FileUtils;
import com.bonc.utils.FtpUtils;
import com.bonc.utils.PropertiesConfig;
import com.bonc.utils.ResponseUtils;
import com.jcraft.jsch.ChannelSftp;

/**
 * 预处理文件
 *
 */
public class FileProcess {

	private static Logger LOG = LoggerFactory.getLogger(FileProcess.class);

	public static void main(String args[]) {

		String inputFileFormatStr = ""; // 输入文件格式

		String inputFileFormat[] = null;

		String outputFileFormat = ""; // 输出文件格式

		String oldRegex = "";

		String targetRegex = "";

		String ftp_ip = "";

		String ftp_port_Str = "";

		String ftp_user = "";

		String ftp_pwd = "";

		String responseFile = "";

		String ftp_path = "";

		String localPath = "";

		String logDirPath = "";

		String usage = "Usage:FTPprocess -inputPath <inputpath> -outputPath <outPath> -confPath <configPath>";

		PropertiesConfig config = null;
		String inputPath = "";
		String outputPath = "";
		String confPath = "";

		for (int i = 0; i < args.length; i++) {
			try {
				switch (args[i]) {
				case "-inputPath":
					inputPath = args[i + 1];
					break;
				case "-outputPath":
					outputPath = args[i + 1];
					break;
				case "-confPath":
					confPath = args[i + 1];
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("缺少启动参数:" + usage);
			}
		}

		try {
			config = new PropertiesConfig(confPath);
		} catch (Exception e1) {
			System.err.println("can't found configPath" + confPath);
			System.exit(-1);
		}

		inputFileFormatStr = config.getValue("inputFileFormat");

		if (inputFileFormatStr == null || inputFileFormatStr.length() == 0
				|| inputFileFormatStr.split(",").length == 0) {
			System.err.println("请设置输入文件格式： inputFileFormat");
			System.exit(-1);
		} else {
			inputFileFormat = inputFileFormatStr.split(",");
		}

		try {
			outputFileFormat = config.getValue("outputFileFormat");
		} catch (Exception e) {
			LOG.info("请在配置文件中设置输出文件格式:outputFileFormat");
			System.exit(-1);
		}

		try {
			oldRegex = config.getValue("sourceDelimiter");
		} catch (Exception e) {
			LOG.info("请在配置文件中设置原始列分隔符:sourceDelimiter");
			System.exit(-1);
		}

		try {
			targetRegex = config.getValue("objectDelimiter");
		} catch (Exception e) {
			LOG.info("请在配置文件中设置目标列分隔符:objectDelimiter");
			System.exit(-1);
		}

		try {
			ftp_ip = config.getValue("ftp_ip");
		} catch (Exception e) {
			LOG.info("请在配置文件中设置远端ftp_ip:ftp_ip");
			System.exit(-1);
		}

		try {
			ftp_port_Str = config.getValue("ftp_port");
		} catch (Exception e) {
			LOG.info("请在配置文件中设置远端ftp_port:ftp_port");
			System.exit(-1);
		}

		try {
			ftp_user = config.getValue("ftp_user");
		} catch (Exception e) {
			LOG.info("请在配置文件中设置远端ftp_user:ftp_user");
			System.exit(-1);
		}

		try {
			ftp_pwd = config.getValue("ftp_pwd");
		} catch (Exception e) {
			LOG.info("请在配置文件中设置远端ftp_pwd:ftp_pwd");
			System.exit(-1);
		}

		try {
			responseFile = config.getValue("responseFile");
		} catch (Exception e) {
			LOG.info("请在配置文件中设置应答文件名称：responseFile");
			System.exit(-1);
		}

		try {
			ftp_path = config.getValue("ftp_path");
		} catch (Exception e) {
			LOG.info("请在配置文件中设置远端ftp_path:ftp_path");
			System.exit(-1);
		}

		try {
			localPath = config.getValue("local_ResponsePath");
		} catch (Exception e) {
			LOG.info("请在配置文件中设置本地存放应答文件路径:local_ResponsePath");
			System.exit(-1);
		}

		logDirPath = config.getValue("log_Dir");

		if (logDirPath == null || logDirPath.indexOf("log4j.properties") == -1) {
			throw new RuntimeException("请在配置文件中设置 log4j.properties的路径：logDir");
		} else {
			System.setProperty("logName", DateUtils.getDate(new Date()));
			PropertyConfigurator.configure(logDirPath);
		}

		// 保存目录下文件信息
		List<String> fileList = new ArrayList<String>();

		// 获取输入路径下的列表文件
		try {
			FileUtils.listFile(fileList, inputPath);

		} catch (Exception e) {
			LOG.info("获取输入目录:" + inputPath + "下文件信息失败");
			System.exit(-1);
		}

		// 保存应答文件信息
		List<String> logLists = new ArrayList<String>();
		// 对文件进行解压,解压到当前目录
		for (String fileFormat : inputFileFormat) {
			for (String filePath : fileList) {
				// .txt .]\ .gz .xlx
				if (filePath.endsWith(fileFormat)) {
					FileUtils.process(filePath, outputPath, oldRegex, targetRegex, outputFileFormat, logLists);
				}
			}
		}

		// 将应答文件上传到ftp
		boolean end;
		String name = null;
		try {
			name = ResponseUtils.write(logLists, localPath, responseFile);
			int ftp_port = Integer.parseInt(ftp_port_Str);
			ChannelSftp sftp = FtpUtils.connect(ftp_ip, ftp_port, ftp_user, ftp_pwd);
			end = FtpUtils.upload(ftp_path, name, sftp);
			if (end) {
				LOG.info("上传应答文件: " + name + "成功");
			} else {
				LOG.info("上传应答文件: " + name + "失败");
			}
			sftp.disconnect();
			LOG.info("程序运行结束");
			System.exit(-1);
		} catch (Exception e) {
			LOG.info("上传应答文件: " + name + "出现异常");
			e.printStackTrace();
		}

	}
}
