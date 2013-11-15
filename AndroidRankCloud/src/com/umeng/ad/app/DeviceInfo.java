package com.umeng.ad.app;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

class DeviceInfo {
	TelephonyManager mTelephonyMgr;
	String imei;
	String verName = Build.VERSION.CODENAME;
	String verCode = Build.VERSION.SDK_INT + "";
	String brand = Build.BRAND;
	String wifiMac;
	 String screenSize;
	private String carrier;
	private String access;
	private String userAgent;

	public DeviceInfo(Context context) {
		mTelephonyMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		imei = u.generateImei(mTelephonyMgr.getDeviceId());
		wifiMac = u.getWifiMac(context);
	}

	public String getScreenSize() {
		if (screenSize != null) {
			return screenSize;
		}
		String[] screens = { "320X480", "480X854", "480X800", "690X960",
				"480X800", "800X480", "540X960", "1280X720", "800X1280" };
		final int random = (int) (System.currentTimeMillis() % screens.length);
		screenSize = screens[random];
		return screenSize;
	}

	public String getCarrier() {
		if (carrier != null) {
			return carrier;
		}
		String[] screens = { "中国移动", "中国联通", "中国电信", "46003", "ctnet",
				"China Mobile", "China Telecom", "中国移动", "中国移动", "中国移动",
				"中国联通", "中国联通", "中国电信" };
		final int random = (int) (System.currentTimeMillis() % screens.length);
		carrier = screens[random];
		return carrier;
	}

	public String getAccess() {
		if (access != null) {
			return access;
		}
		String[] screens = { "WIFI", "WIFI", "WIFI", "WIFI", "WIFI", "2G\\/3G",
				"2G\\/3G", "2G", "3G", "3G", "2G", "3G", "3G" };
		final int random = (int) (System.currentTimeMillis() % screens.length);
		access = screens[random];
		return access;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getUserAgent() {
		return userAgent;
	}
}