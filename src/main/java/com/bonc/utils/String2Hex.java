package com.bonc.utils;

/**
 * create by johen(jing) on 2016-05-31:9:06 project_name SeparatorMR
 * package_name com.bonc.SeparatorMR JDK 1.7
 *
 */
public class String2Hex {
	private static final String HEX = "0x";
	private boolean isH;
	private String stIn;
	private int stInLong;

	public String2Hex(String source) {
		source = source.toLowerCase();
		if(source.startsWith("\\u")){
			source = HEX+source.substring(2, source.length());
		}
		if (source.contains("\\")) {
			source = source.replaceAll("\\\\", "");
		}
		if (source.startsWith(HEX)) {
			isH = true;
			stIn = source.substring(2);
		} else {
			stIn = source;
		}
		stInLong = stIn.length();
	}

	/**
	 * 该类用于返回转换后的值
	 * 
	 * @return 结果
	 */
	public String getHexString() {
		if (isH) {
			int charInt = 0;
			for (char c : stIn.toCharArray()) {
				int tmp = getChar2Int(c);
				if ((stInLong--) == -1) {
					charInt += tmp;
					break;
				}
				charInt += Math.pow(16, stInLong) * tmp;
			}
			return String.valueOf((char) charInt);
		}
		return stIn;
	}

	/**
	 * 该类用于获取字符并将其转换为16进制对应的整数
	 * 
	 * @param c
	 *            需要转换的字符啊
	 * @return 通过十六进制转换为字符对应的10进制编码
	 */
	private int getChar2Int(char c) {
		if (c >= 48 && c <= 59) {
			return (int) c - 48;
		}
		if (c >= 65 && c <= 70) {
			return (int) c - 55;
		}
		if (c >= 97 && c <= 102) {
			return (int) c - 87;
		}
		throw new RuntimeException("source:" + stIn + " is not hex");
	}

	@Override
	public String toString() {
		return getHexString();
	}

	public static void main(String[] args) {
		String2Hex confH = new String2Hex("\\u007c");
		System.out.println("zhang" + confH.getHexString() + "jing");
		System.out.println("zhang" + confH + "jing");
	}
}
