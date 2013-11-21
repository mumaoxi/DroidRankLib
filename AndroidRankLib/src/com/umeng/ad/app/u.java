package com.umeng.ad.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.umeng.ad.app.utils.IOUtils;

class u {

	public enum CompressType {
		GZIP, DEFLATE
	}

	/** Secret key from JNI when decoding the XML file */
	static final int SECRET_KEY = u.getInstance().getSignatureIntKey();
	/** Get the default keyword from JNI */
	static final String DEFAULT_KEYWORD = u.getInstance().getDefaultKeyword();

	/**
	 * MD5constant
	 */
	static final String MD5_CONSTANT = u.getInstance().getMd5constant();

	/**
	 * TimeExtra
	 */
	interface TimeExtra {
		static final int ONE_SECOND = 1000;
		static final int ONE_MINUTE = 60 * ONE_SECOND;
		static final int ONE_HOUR = 60 * ONE_MINUTE;
		static final int ONE_DAY = 24 * ONE_HOUR;
	}

	private static final List<String> agents = new ArrayList<String>();

	static {
		for (int i = 0; i < 14; i++) {
			agents.add(u.getInstance().getAgent(i));
		}
	}

	/**
	 * 
	 * @return Return a agent for web randomly.
	 */
	static String randomAgent() {
		return agents.get((int) (Math.random() * agents.size()));
	}

	/**
	 * Check if the network is avaliable.
	 * 
	 * @param context
	 * @return
	 */
	static boolean checkNet(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info != null) {
			return true;
		}
		return false;
	}

	/**
	 * Get the current phone network type.
	 * 
	 * @param context
	 * @return
	 */
	static String getAPN(Context context) {
		String apn = "";
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();

		if (info != null) {
			if (ConnectivityManager.TYPE_WIFI == info.getType()) {
				apn = info.getTypeName();
				if (apn == null) {
					apn = "wifi";
				}
			} else {

				if (info.getExtraInfo() != null) {
					apn = info.getExtraInfo().toLowerCase();
				}

				if (apn == null) {
					apn = "mobile";
				}
			}
		}
		return apn;
	}

	static String getModel(Context context) {
		return Build.MODEL;
	}

	static String getManufacturer(Context context) {
		return Build.MANUFACTURER;
	}

	static String getFirmware(Context context) {
		return Build.VERSION.RELEASE;
	}

	static String getSDKVer() {
		return Integer.valueOf(Build.VERSION.SDK_INT).toString();
	}

	static String getLanguage() {
		Locale locale = Locale.getDefault();
		String languageCode = locale.getLanguage();
		if (TextUtils.isEmpty(languageCode)) {
			languageCode = "";
		}
		return languageCode;
	}

	static String getCountry() {
		Locale locale = Locale.getDefault();
		String countryCode = locale.getCountry();
		if (TextUtils.isEmpty(countryCode)) {
			countryCode = "";
		}
		return countryCode;
	}

	/**
	 * 获取device_id
	 * 
	 * @param context
	 * @return
	 */
	static String getIMEI(Context context) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei;
		if ((Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"))) {
			SharedPreferences sp = context.getSharedPreferences(".umeng",
					Context.MODE_PRIVATE);
			imei = sp.getString("android.os.SystemProperties.DeviceId", "");
			if (imei == null || imei.length() < 1) {
				imei = UUID.randomUUID().toString();
				Editor editor = sp.edit();
				editor.putString("android.os.SystemProperties.DeviceId", imei);
				editor.commit();
			}
		} else {
			imei = mTelephonyMgr.getDeviceId();
			if (TextUtils.isEmpty(imei) || imei.equals("000000000000000")) {
				SharedPreferences sp = context.getSharedPreferences(".umeng",
						Context.MODE_PRIVATE);
				imei = sp.getString("android.os.SystemProperties.DeviceId", "");
				if (imei.length() < 1) {
					imei = UUID.randomUUID().toString();
					Editor editor = sp.edit();
					editor.putString("android.os.SystemProperties.DeviceId",
							imei);
					editor.commit();
				}
			}
		}
		return imei;
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	static String getIpAddress(Context context) {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception ex) {
			MLog.e("WifiPreference IpAddress" + ex.toString());
		}
		try {
			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int ipAddress = wifiInfo.getIpAddress();

			return ipAddress + "";
		} catch (Exception e) {
			MLog.e("error while getting wifi ip address");
		}

		return null;
	}

	/**
	 * Encode string with secret key.
	 * 
	 * @param origString
	 * @param secret
	 * @return
	 */
	static String encodeString(String origString, char secret) {
		try {
			String secretString = "";
			Integer[] mdString = new Integer[origString.length()];
			if (origString != null) {
				for (int i = 0; i < origString.length(); i++) {
					char c = origString.charAt(i);
					mdString[i] = (secret ^ c);
					secretString += mdString[i] + "\t";
				}
			}
			return secretString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Decode String by secrect key.
	 * 
	 * @param secrectString
	 * @param secret
	 * @return
	 */
	static String decodeString(String secrectString, char secret) {
		try {
			String origString = "";
			String[] mdString = secrectString.split("\t");
			for (String string : mdString) {
				if (string != null && string.length() > 0
						&& !string.equals("\t")) {
					Integer integer = Integer.valueOf(string);
					origString = origString + (char) (integer ^ secret);
				}
			}
			return origString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static String encodeS(String origString, int secret) {
		try {
			Integer[] mdString = new Integer[origString.length()];
			byte[] bytes = new byte[mdString.length];
			if (origString != null) {
				for (int i = 0; i < origString.length(); i++) {
					char c = origString.charAt(i);
					mdString[i] = (secret ^ c);
					bytes[i] = (byte) (secret ^ c);
				}
			}
			return Arrays.toString(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static String decodeS(String secrectString, int secret) {
		try {
			return decodeS(new JSONArray(secrectString), secret);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			secrectString = secrectString.replace("[", "");
			secrectString = secrectString.replace("]", "");

			String[] mdString = secrectString.split(", ");
			byte[] bytes = new byte[mdString.length];
			int i = 0;
			for (String string : mdString) {
				if (string != null && string.length() > 0
						&& !string.equals(", ")) {
					string = string.trim();
					Integer integer = Integer.valueOf(string);
					bytes[i] = (byte) (integer ^ secret);
					i++;
				}
			}
			return new String(bytes, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static String decodeS(JSONArray secrectString, int secret) {
		try {
			byte[] bytes = new byte[secrectString.length()];
			int index = 0;
			for (int i = 0; i < secrectString.length(); i++) {
				Integer integer = secrectString.getInt(i);
				bytes[index] = (byte) (integer ^ secret);
				index++;
			}
			return new String(bytes, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Generate the imei by the original imei.
	 * 
	 * @param imei
	 * @return
	 */
	static String generateImei(String imei) {
		try {
			if (imei==null||imei.startsWith("00")) {
				imei = "8627640101";
				for (int i = 0; i < 5; i++) {
					imei = imei + (int) (Math.random() * 10);
				}
			} else if (imei.startsWith("86")) {
				imei = imei.substring(0, 12);
				for (int i = 0; i < 3; i++) {
					imei = imei + (int) (Math.random() * 10);
				}
			} else if (!imei.startsWith("86")) {
				try {
					imei = imei.substring(0, imei.length() - 3);
					for (int i = 0; i < 3; i++) {
						// char x = (char) (97 + (int) (Math.random() * 36));
						// if (x > 122) {
						// imei = imei + (x - 122) % 10;
						// } else {
						// imei = imei + x;
						// }
						imei = imei + (int) (Math.random() * 10);
					}
				} catch (Exception e) {
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return imei;
	}

	/**
	 * Get the mobile wifi mac address
	 * 
	 * @param context
	 * @return
	 */
	static String getWifiMac(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		String mac = generateRandCharNum(10);
		if (wm.getConnectionInfo() != null) {
			mac = wm.getConnectionInfo().getMacAddress();
		}
		return mac;
	}

	/**
	 * Generate a serial number of chars randomly.
	 * 
	 * @param stringLength
	 * @return
	 */
	static String generateRandCharNum(int stringLength) {
		String charNum = "";
		try {
			for (int i = 0; i < stringLength; i++) {
				int random = (int) (Math.random() * 35);
				if (random > 25) {
					charNum = charNum + (35 - random);
				} else {
					char theChar = (char) (97 + random);
					charNum = charNum + (theChar + "").toUpperCase();
				}
			}
		} catch (Exception e) {
		}
		return charNum;
	}

	/**
	 * 
	 * @param params
	 */
	public static void setParamsForHttpConnection(HttpParams params) {
		HttpConnectionParams.setConnectionTimeout(params,
				20 * TimeExtra.ONE_SECOND);
		HttpConnectionParams.setSoTimeout(params, 20 * TimeExtra.ONE_SECOND);
		HttpConnectionParams.setSocketBufferSize(params, 8192);
		HttpClientParams.setRedirecting(params, true);
	}

	/**
	 * Read content from httpResponse
	 * 
	 * @param response
	 * @return
	 */
	public static String readContentFromHttpResponse(HttpResponse response,
			String charSet) {
		try {
			HttpEntity entity = response.getEntity();

			// Response Headers
			boolean gzip = false;
			Header[] headers = response.getAllHeaders();
			if (headers != null && headers.length > 0) {
				for (Header header : headers) {
					if ("content-encoding".equals(header.getName()
							.toLowerCase())
							&& "gzip".equals(header.getValue().trim()
									.toLowerCase())) {
						gzip = true;
						break;
					}
				}
			}
			String body = gzip ? body = u.uncompressDataWithGzip(
					entity.getContent(), charSet) : IOUtils.toString(
					entity.getContent(), charSet);
			return body;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Compresss post body content when using gzip to post data
	 * 
	 * @param body
	 * @return
	 */
	public static InputStreamEntity compressPostBodyWithGzip(String body) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(
					body.getBytes(HTTP.UTF_8));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gos = new GZIPOutputStream(bos);
			for (int c = bis.read(); c != -1; c = bis.read()) {
				gos.write(c);
			}
			gos.flush();
			bis.close();
			gos.close();

			InputStreamEntity entity = new InputStreamEntity(
					new ByteArrayInputStream(bos.toByteArray()),
					bos.toByteArray().length);
			bos.close();
			return entity;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param body
	 * @return
	 */
	public static String uncompressDataWithGzip(InputStream inputStream,
			String charset) {
		try {
			GZIPInputStream gzi = new GZIPInputStream(inputStream);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			for (int c = gzi.read(); c != -1; c = gzi.read()) {
				bos.write(c);
			}
			bos.flush();
			gzi.close();
			bos.close();

			return new String(bos.toByteArray(), charset);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param body
	 * @return
	 */
	public static InputStreamEntity compressPostBodyWithDeflate(String body) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(
					body.getBytes(HTTP.UTF_8));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DeflaterOutputStream gos = new DeflaterOutputStream(bos);
			for (int c = bis.read(); c != -1; c = bis.read()) {
				gos.write(c);
			}
			gos.flush();
			bis.close();
			gos.close();

			InputStreamEntity entity = new InputStreamEntity(
					new ByteArrayInputStream(bos.toByteArray()),
					bos.toByteArray().length);
			bos.close();
			return entity;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Use md5Key to get the Orignal Key
	 */
	String getKeyByMD5Key(String md5Key) {
		List<String> keys = new ArrayList<String>();
		keys.add("method");
		keys.add("package");
		keys.add("market");
		keys.add("count");

		for (String key : keys) {
			if (md5Key == MD5Utils.string2MD5(key + MD5_CONSTANT))
				return key;
		}
		return "";
	}

	/**
	 * Use md5Value to compare with the value library to get the real value.
	 */
	String getValueFromDataArrayByMD5Value(String[] dataarray, String md5Value) {

		for (int i = 0; i < dataarray.length; i++) {
			if (md5Value.equals(MD5Utils
					.string2MD5(dataarray[i] + MD5_CONSTANT)))
				return dataarray[i];
		}

		return "";
	}

	/**
	 * 获取友盟刷量参数
	 * 
	 * @param context
	 * @return
	 */
	static String getRankControlURL(Context context) {

		String defValue = "http://umeng.sinaapp.com/enter.php/Api/RankConfig/rankconfig";
		try {
			SharedPreferences sp = context.getSharedPreferences(".umeng",
					Context.MODE_PRIVATE);
			return sp.getString("umeng_index_url", defValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defValue;
	}

	private static u mUtils;

	private u() {
	}

	static u getInstance() {
		if (mUtils == null) {
			mUtils = new u();
		}
		return mUtils;
	}

	/**
	 * Get signature String key from jni
	 * 
	 * @return
	 */
	String getSignatureKey() {
		return "24";
	}

	/**
	 * Get signature Int key from jni
	 * 
	 * @return
	 */
	int getSignatureIntKey() {
		return 24;
	}

	/**
	 * Get the market name from jni by serial number.
	 * 
	 * @param serial
	 * @return market name
	 */
	String getMName(int serial) {
		if (serial == 0) {
			return "百度移动市场";
		}
		if (serial == 1) {
			return "安卓市场";
		}
		if (serial == 2) {
			return "华为智汇云";
		}
		if (serial == 3) {
			return "魅族商店";
		}
		if (serial == 4) {
			return "oppo应用商店";
		}
		if (serial == 5) {
			return "QQ腾讯应用宝";
		}
		if (serial == 6) {
			return "N多";
		}
		if (serial == 7) {
			return "机锋市场";
		}
		if (serial == 8) {
			return "应用汇";
		}
		if (serial == 9) {
			return "91手机助手";
		}
		if (serial == 10) {
			return "阿里云应用中心";
		}
		if (serial == 11) {
			return "联想开发者社区";
		}
		if (serial == 12) {
			return "米UI";
		}
		if (serial == 13) {
			return "360软件管家";
		}
		if (serial == 14) {
			return "木蚂蚁";
		}
		if (serial == 15) {
			return "酷派应用商店";
		}
		if (serial == 16) {
			return "中兴手机应用";
		}
		if (serial == 17) {
			return "搜狐应用中心";
		}
		if (serial == 18) {
			return "搜狗手机助手";
		}
		return "百度移动市场";
	}

	/**
	 * Get a agent from jni.
	 * 
	 * @param serial
	 * @return Agent
	 */
	String getAgent(int serial) {
		if (serial == 0) {
			return "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:15.0) Gecko/20100101 Firefox/15.0.1 FirePHP/0.7.1";
		}
		if (serial == 1) {
			return "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.202 Safari/535.1";
		}
		if (serial == 2) {
			return "Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.0.9) Gecko/20061206 Firefox/1.5.0.9";
		}
		if (serial == 3) {
			return "Mozilla/4.0 (compatible; MSIE 6.3; Windows NT 5.12; SV1; .NET CLR 2.0.60727";
		}
		if (serial == 4) {
			return "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322";
		}
		if (serial == 5) {
			return "Mozilla/4.0 (compatible; MSIE 6.2; Windows NT 5.12; SV1; .NET CLR 1.0.6021";
		}
		if (serial == 6) {
			return "Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.0.8) Gecko/2009032711 Ubuntu/8.10 (intrepid) Firefox/3.0.8";
		}
		if (serial == 7) {
			return "Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.0.8) Gecko/2009032711 Ubuntu/8.11 (intrepid) Firefox/3.0.9";
		}
		if (serial == 8) {
			return "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7";
		}
		if (serial == 9) {
			return "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)";
		}
		if (serial == 10) {
			return "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13";
		}
		if (serial == 11) {
			return "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11";
		}
		if (serial == 12) {
			return "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)";
		}
		if (serial == 13) {
			return "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/537.10 (KHTML, like Gecko) Chrome/23.0.1271.63 Safari/537.11";
		}
		return "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/537.10 (KHTML, like Gecko) Chrome/23.0.1271.63 Safari/537.12";
	}

	/**
	 * Get the connect url from jni
	 * 
	 * @return
	 */
	String getApiUrlConnect() {
		return "http://www.yepcolor.com/adultshop/api/v1/get_config?version=";
	}

	/**
	 * Get default keyword from jni
	 * 
	 * @return
	 */
	String getDefaultKeyword() {
		return "秘蜜情趣";
	}

	/**
	 * Get the md5 constant from jni
	 * 
	 * @return
	 */
	String getMd5constant() {
		return "umeng";
	}

	/**
	 * Get request content
	 * 
	 * @param routePath
	 * @return
	 */
	String getReqestContent(String routePath) {
		// Gfan
		if ("gfan_home_recommend.xml".equals(routePath))
			return "<request version=\"2\"><platform>%1$s</platform><feature_type>cpu</feature_type><match_type>1</match_type><recommend_type></recommend_type><screen_size>%2$s</screen_size><start_position>51</start_position><feature></feature><size>100</size></request>";
		if ("gfan_search_app.xml".equals(routePath))
			return "<request version=\"2\"><platform>%1$s</platform><feature_type>cpu</feature_type><match_type>1</match_type><keyword>%2$s</keyword><screen_size>%3$s</screen_size><start_position>0</start_position><feature></feature><orderby>0</orderby><size>20</size></request>";
		if ("gfan_search_app.xml".equals(routePath))
			return "<request version=\"2\" local_version=\"-1\"><p_id>%1$s</p_id><source_type>0</source_type></request>";
		if ("gfan_download_url.xml".equals(routePath))
			return "<request version=\"2\"><p_id>%1$s</p_id><uid></uid><source_type>0</source_type></request>";
		if ("gfan_download_report.xml".equals(routePath))
			return "<request version=\"2\"><uid>-1</uid><package_name>%1$s</package_name><report_type>0</report_type><source_type>0</source_type><cpid>web26%2$s</cpid><p_id>%3$s</p_id><url>%4$s</url><size>%5$s</size><net_context>network is mobile [Carrier 2.0]-&gt;epc.tmobile.com</net_context><ip>%6$s</ip></request>";
		if ("gfan_download_report.xml".equals(routePath))
			return "sign={\"id\":1,\"key\":\"f299d30e2c27996b766189e128e01888\"}&code=[{\"action\":\"connect\",\"params\":{\"cpuPlatform\":\"armeabi-v7a\",\"marketVersionCode\":1110,\"model\":\"google_sdk\",\"client\":\"nduoa\",\"density\":240,\"manufacturer\":\"unknown\",\"deviceType\":\"generic\",\"imei\":\"000000000000000\",\"sdkVersionCode\":16,\"resolution\":\"\",\"channel\":\"baidu\",\"connectType\":\"epc.tmobile.com\"}},{\"action\":\"splash\",\"params\":\"\"}]";
		if ("xiaomi_connect.json".equals(routePath))
			return "{\"model\":\"%1$s\",\"resolution\":\"%2$s\",\"net\":\"%3$s\",\"marketVersionName\":\"%4$s\",\"miui\":true,\"version\":\"%5$s\",\"imei\":\"%6$s\"}";
		if ("coolmart_post.json".equals(routePath))
			return "<?xml version=\"1.0\" encoding=\"utf-8\"?><request username=\"\" sn=\"%1$s\" platform=\"1\" platver=\"%2$s\" density=\"240\" screensize=\"%3$s\" language=\"zh\" mobiletype=\"%4$s\" version=\"1\" seq=\"0\" appversion=\"203048\" currentnet=\"%5$s\" channelid=\"Coolmart\" networkoperator=\"310260\" simserianumber=\"%6$s\" ><rid>%7$s</rid></request>";
		return "nothing";
	}

	/**
	 * 获取当前软件版本
	 * 
	 * @param context
	 * @return
	 */
	public static String getAppVersionName(Context context) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			String versionName = pi.versionName;
			return versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取当前软件版本号
	 * 
	 * @param context
	 * @return
	 */
	public static int getAppVersionCode(Context context) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			int versionCode = pi.versionCode;
			return versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 
	 * @param context
	 * @param keyName
	 * @return
	 */
	public static String getMetaData(Context context, String keyName) {
		try {
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);

			Bundle bundle = info.metaData;
			Object value = bundle.get(keyName);
			return String.valueOf(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
