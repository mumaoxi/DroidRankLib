package com.umeng.ad.app;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.umeng.ad.app.u.TimeExtra;

class MarketNDuoa extends Market {

	private String APP_ID = "";
	private String APP_DOWNLOAD_URL = "http://nmarket.nduoa.com/apk/download/%1$s";
	private String cookie = "";
	private String key;
	private String versionCode;
	private String versionName;
	private DeviceInfo deviceInfo;

	private static final String ACTION_URL = "http://nmarket.nduoa.com/open/api3/index";

	public MarketNDuoa() {
	}

	public MarketNDuoa(String packageName) {
		MARKET_NAME = MARKET_NDUOA;
		setPackageName(packageName);
	}

	@Override
	protected boolean prepareToRank(Context context) throws Exception {
		deviceInfo = new DeviceInfo(context);
		String userAgent = u.getInstance().getAgent(
				(int) (System.currentTimeMillis() % 14));
		deviceInfo.setUserAgent(userAgent);
		APP_DOWNLOAD_URL = String.format(APP_DOWNLOAD_URL, APP_ID);
		MLog.i("app_keyword:" + getAppKeyword());

		this.action01ConnetServer();
		this.action02DownloadAPK();

		return this.actionPostDataToServer();
	}

	private void action01ConnetServer() {
		try {
			MLog.v("action01ConnetServer");
			String postData = u.getInstance().getReqestContent(
					"nduoa_connect.json");
			MLog.i("postData:" + postData);
			HttpPost httpPost = new HttpPost(ACTION_URL);
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");
			HttpEntity entity2 = new StringEntity(postData, HTTP.UTF_8);
			httpPost.setEntity(entity2);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			//
			HttpConnectionParams.setConnectionTimeout(params,
					10 * TimeExtra.ONE_SECOND);
			HttpConnectionParams
					.setSoTimeout(params, 10 * TimeExtra.ONE_SECOND);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, false);
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			/**
			 * Proxy
			 */
			if (MLog.logEnable) {
				HttpHost proxy = new HttpHost("175.25.243.22", 80);
				client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY,
						proxy);
				client.getCredentialsProvider().setCredentials(
						new AuthScope("175.25.243.22", 80),
						new UsernamePasswordCredentials("", ""));
			}

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpPost);
			int statuscode = response.getStatusLine().getStatusCode();
			MLog.i("statusCode:" + statuscode);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("connetServer=>" + body);
			JSONObject object = new JSONArray(body).getJSONObject(0);
			if ("connect".equals(object.getString("action"))) {
				cookie = object.getString("data");
				MLog.i("cookie:" + cookie);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void action02DownloadAPK() {
		try {
			MLog.v("DownloadAPK");
			MLog.i("downloadURL:" + APP_DOWNLOAD_URL);
			// Request
			HttpGet httpGet = new HttpGet(APP_DOWNLOAD_URL);
			int range = ((int) (Math.random() * 300));
			MLog.i("range:" + range + "-" + (range + 2));
			httpGet.addHeader("Range", "bytes=" + range + "-" + (range + 2));

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpProtocolParams.setUserAgent(params, "");
			client.setParams(params);

			/**
			 * Proxy server
			 */
			if (MLog.logEnable) {
				HttpHost proxy = new HttpHost("175.25.243.22", 80);
				client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY,
						proxy);
				client.getCredentialsProvider().setCredentials(
						new AuthScope("175.25.243.22", 80),
						new UsernamePasswordCredentials("", ""));

			}
			// Response
			HttpResponse response = client.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);

			String content = u
					.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("content:" + content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean actionPostDataToServer() {
		try {
			MLog.v("actionPostDataToServer");
			HttpPost httpPost = new HttpPost(ACTION_URL);
			httpPost.addHeader("Cookie", cookie);
			MLog.i("cookie:" + cookie);
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");
			/**
			 * sign={"id":1,"key":"a240aadddd2c3eeae40819a717a36219"}&
			 * code={"action"
			 * :"listUpdateSoft","params":[{"packageName":"com.blsm.mm"
			 * ,"versionCode":10,"versionName":"1.43"}]}
			 */
			// JSON code
			JSONObject codeObject = new JSONObject();
			codeObject.put("action", "listUpdateSoft");
			JSONObject appObject = new JSONObject();
			appObject.put("packageName", getPackageName());
			appObject.put("versionCode", Integer.valueOf(getVersionCode()));
			appObject.put("versionName", getVersionName());
			JSONArray paramsArray = new JSONArray();
			paramsArray.put(appObject);
			codeObject.put("params", paramsArray);

			// JSON sign
			JSONObject signObject = new JSONObject();
			signObject.put("id", 1);
			signObject.put("key", key);

			List<NameValuePair> httpParams = new ArrayList<NameValuePair>();
			httpParams
					.add(new BasicNameValuePair("sign", signObject.toString()));
			httpParams
					.add(new BasicNameValuePair("code", codeObject.toString()));
			httpPost.setEntity(new UrlEncodedFormEntity(httpParams, HTTP.UTF_8));

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
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			/**
			 * Proxy
			 */
			if (MLog.logEnable) {
				HttpHost proxy = new HttpHost("175.25.243.22", 80);
				client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY,
						proxy);
				client.getCredentialsProvider().setCredentials(
						new AuthScope("175.25.243.22", 80),
						new UsernamePasswordCredentials("", ""));
			}

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpPost);
			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			// MLog.i("body:" + body);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject object = new JSONObject(body);
				if (object.getInt("code") == 0) {
					JSONArray lists = object.getJSONObject("data")
							.getJSONArray("lists");
					for (int i = 0; i < lists.length(); i++) {
						JSONObject appObj = lists.getJSONObject(i);
						if (getPackageName().equals(
								appObj.getString("packageName"))) {
							String label = appObj.getString("label");
							String download = appObj.getInt("downloadCount")
									+ "/" + appObj.getInt("totalDownloadCount");
							MLog.i(label + " v" + versionName + " download:"
									+ download);
						}
					}
					return true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 */
	private boolean getAnalysis() {
		try {
			long mills = System.currentTimeMillis();
			String userAgent = u.getInstance().getAgent(
					(int) (System.currentTimeMillis() % 14));
			String url = String
					.format("http://app.meizu.com/service/ms_versionAct/getFreeAppDownloadUrl.jsonp?p0=%1$s&%2$s",
							APP_ID, mills);
			MLog.v("Meizu:" + url);
			HttpGet httpGet = new HttpGet(url);

			/**
			 * Logger.i(this,curlString);
			 */
			httpGet.addHeader("Cookie", cookie);
			MLog.v("Meizu,cookie:" + cookie);
			httpGet.addHeader("Referer", String.format(
					"http://app.meizu.com/phone/apps/%1$s", APP_ID));

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
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params, userAgent);
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("getAnalysis=>status_code:" + code);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("body:" + body);
			JSONObject object = new JSONObject(body);
			if (object.getString("reply") != null
					&& object.getString("reply").startsWith("http://")) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
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
			setKey(params.get("a_key"));
			setVersionCode(params.get("a_versionCode"));
			setVersionName(params.get("a_versionName"));
		} catch (Exception e) {
		}
	}

}
