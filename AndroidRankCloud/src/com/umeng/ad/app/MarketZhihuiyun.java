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

		return this.searchAPP(searchURL.toString(), searchParams.toString(),
				paramHash);

	}

	private boolean searchAPP(String url, String httpparams,
			HashMap<String, String> httpParamHash) {
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
				return this.download(object, httpParamHash);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 */
	private boolean download(JSONObject jsoobject,
			HashMap<String, String> hashparams) {
		try {
			/**
			 * data
			 */
			MLog.i("download...." + jsoobject.getString("name"));
			String url = jsoobject.getString("downurl");
			url += "cno=" + hashparams.get(("cno")) + "&";
			url += "net=" + hashparams.get(("net"));

			String range = (Long.valueOf(jsoobject.getString("size")) - 2)
					+ "-";

			HttpGet httpGet = new HttpGet(url);
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
			this.getAppAction(jsoobject, hashparams);
			return this.getGameArea(jsoobject, hashparams);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 */
	private void getAppAction(JSONObject jsoobject,
			HashMap<String, String> hashparams) {
		try {
			/**
			 * data
			 */
			String url = "http://hispaceclt.hicloud.com:8080/hwmarket/client/appAction-shelves.do?";
			url += "sign=" + hashparams.get(("sign")) + "&";
			url += "type=down_sucess&";
			url += "appId=" + jsoobject.getString("id") + "&";
			url += "version=" + jsoobject.get("versionCode") + "&";
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
	private boolean getGameArea(JSONObject jsoobject,
			HashMap<String, String> hashparams) {
		try {
			String url = "http://hispaceclt.hicloud.com:8080/hwmarket/client/gameAreaDownload.do?";
			url += "sign=" + hashparams.get(("sign")) + "&";
			url += "areaName=Hi_Space&";
			url += "appName="
					+ URLEncoder.encode(jsoobject.getString("name"), "UTF-8")
					+ "&";
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
			if (code == HttpStatus.SC_OK
					&& PACKAGE_NAME.equals(jsoobject.getString("package"))) {
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
		
	}

}
