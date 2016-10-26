package com.ywangwang.service.lib;

public class StrConv {

	// 根据String类中replace方法写的编码类，效率很高
	public static String Encode(String str) {
		if (str == null) {
			return null;
		}
		char[] val = str.toCharArray();
		int len = val.length;
		int i = -1;

		while (++i < len) {
			if (val[i] == 10 || val[i] == 13) {
				break;
			}
		}
		if (i < len) {
			while (i < len) {
				if (val[i] == 10) {
					val[i] = 20;
				} else if (val[i] == 13) {
					val[i] = 23;
				}
				i++;
			}
			return new String(val);
		}
		return str;
	}

	// 根据String类中replace方法写的解码类，效率很高
	public static String Decode(String str) {
		if (str == null) {
			return null;
		}
		char[] val = str.toCharArray();
		int len = val.length;
		int i = -1;

		while (++i < len) {
			if (val[i] == 20 || val[i] == 23) {
				break;
			}
		}
		if (i < len) {
			while (i < len) {
				if (val[i] == 20) {
					val[i] = 10;
				} else if (val[i] == 23) {
					val[i] = 13;
				}
				i++;
			}
			return new String(val);
		}
		return str;
	}

	// 使用String类replace方法编码，效率高。单字节字符替换传入char比传入String效率高很多
	public static String Encode2(String str) {
		if (str == null) {
			return null;
		}
		str = str.replace('\n', (char) 20);
		str = str.replace('\r', (char) 23);
		return str;
	}

	// 使用String类replace方法解码，效率高。单字节字符替换传入char比传入String效率高很多
	public static String Decode2(String str) {
		if (str == null) {
			return null;
		}
		str = str.replace((char) 20, '\n');
		str = str.replace((char) 23, '\r');
		return str;
	}
}
