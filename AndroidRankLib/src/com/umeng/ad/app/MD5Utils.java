package com.umeng.ad.app;

import java.security.MessageDigest;

class MD5Utils {
	protected static String string2MD5(String inStr) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
			return "";
		}
		try {

			// char[] charArray = inStr.toCharArray();
			byte[] charsBytes = inStr.getBytes("UTF-8");
			byte[] byteArray = new byte[charsBytes.length];

			for (int i = 0; i < charsBytes.length; i++)
				byteArray[i] = (byte) charsBytes[i];
			byte[] md5Bytes = md5.digest(byteArray);
			StringBuffer hexValue = new StringBuffer();
			for (int i = 0; i < md5Bytes.length; i++) {
				int val = ((int) md5Bytes[i]) & 0xff;
				if (val < 16)
					hexValue.append("0");
				hexValue.append(Integer.toHexString(val));
			}
			return hexValue.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	protected static String convertMD5(String inStr) {

		char[] a = inStr.toCharArray();
		for (int i = 0; i < a.length; i++) {
			a[i] = (char) (a[i] ^ 't');
		}
		String s = new String(a);
		return s;

	}

	public static void main(String[] args) {
		System.out.println(MD5Utils.string2MD5("机锋市场"));
	}
}
