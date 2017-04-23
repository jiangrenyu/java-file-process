package com.bonc.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ResponseUtils {

	private static Logger LOG = LoggerFactory.getLogger(ResponseUtils.class);

	/**
	 * 将应答文件上传到ftp服务器
	 * 
	 * @param list
	 * @param responsePath
	 * @return
	 */
	public static String write(List<String> list, String responsePath, String responseName) {

		String separator = System.getProperty("file.separator");

		String pathname = responsePath.endsWith(separator) ? responsePath : (responsePath + separator);

		File file = new File(pathname + responseName + DateUtils.getDate(new Date()) + ".txt");
		try {

			Writer is = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(is);
			if (list.size() > 0 && list != null) {
				for (String str : list) {
					bw.write(str);
					bw.newLine();
					bw.flush();
				}
			} else {
				bw.write("");
				bw.flush();
			}
			bw.close();
		} catch (Exception e) {
			LOG.info("创建应答文件: " + file.getAbsolutePath() + "异常");
			e.printStackTrace();
		}

		return file.getAbsolutePath();
	}
}
