package com.umeng.ad.app;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
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
import android.util.Base64;

import com.umeng.ad.app.u.TimeExtra;

class MarketXiaomi extends Market {

	private String APP_ID = "";
	private String APP_DOWNLOAD_URL = "";
	private String APP_KEYWORD = "";
	private String APP_VERSION_CODE;
	private String marketVersionName;
	private String version;
	private String sessionId;
	private String clientId;

	// from search list to app detail index
	private int refPosition;

	private DeviceInfo deviceInfo;

	private static final String ACTION_CONNECT_SERVER = "http://market.xiaomi.com/apm/user/device";
	private static final String ACTION_GET_SUGGEST = "http://58.68.235.171/apm/search/suggest?clientId=%1$s&co=CN&imei=%2$s&keyword=%3$s&la=zh&os=%4$s&sdk=%5$s&session=%6$s";
	private static final String ACTION_SEARCH = "http://58.68.235.171/apm/search?clientId=%1$s&co=CN&imei=%2$s&keyword=%3$s&la=zh&os=%4$s&page=0&ref=input&sdk=%5$s&session=%6$s";
	private static final String ACTION_APP_DETAIL = "http://58.68.235.171/apm/app/id/%1$s?clientId=%2$s&co=CN&imei=%3$s&la=zh&os=%4$s&ref=search&refPosition=%5$s&sdk=%6$s&session=%7$s";
	private static final String ACTION_GET_DOWNLOAD_URL = "http://58.68.235.171/apm/download/%1$s?clientId=%2$s&co=CN&imei=%3$s&la=zh&net=wifi&os=%4$s&ref=search&refPosition=%5$s&sdk=%6$s&session=%7$s&signature=%8$s";
	private static final String ACTION_UPDATE_INFOV2 = "http://58.68.235.171/apm/updateinfo/v2";
	private static final String ACTION_UPDATE_INFO = "http://58.68.235.171/apm/updateinfo";
	private static final String DATA_UPDATE_INFO = "clientId=%1$s&co=CN&imei=%2$s&la=zh&os=%3$s&packageName=%4$s&sdk=%5$s&session=%6$s";

	public MarketXiaomi() {
	}

	public MarketXiaomi(String packageName) {
		MARKET_NAME = MARKET_XIAOMI;
		setPackageName(packageName);
	}

	@Override
	protected boolean prepareToRank(Context context) throws Exception {
		deviceInfo = new DeviceInfo(context);

		String[] models = { "MI 1S", "MI 1S", "MI 1S", "MI 2", "MI 2", "MI 2S",
				"MI 2S", "MI 2S", "MI 2S", "MI 2S", "小米MI-ONE Plus", "MI 2A",
				"MI 2SC", "MI 1SC", "MI-ONE C1", "MI 2C", "MI 1S" };
		deviceInfo.brand = models[((int) (System.currentTimeMillis() % models.length))];
		String[] sizes = { "1280*720", "480*854", "1920*1080" };
		deviceInfo.screenSize = sizes[((int) (System.currentTimeMillis() % sizes.length))];
		String userAgent = String.format(
				"Dalvik/1.6.0 (Linux; U; Android %1$s; %2$s MIUI/%3$s)",
				deviceInfo.brand, deviceInfo.verName, version);
		String imei = "860310024890651";
		imei = imei.substring(0, 5);
		for (int i = 0; i < 10; i++) {
			imei = imei + (int) (Math.random() * 10);
		}
		deviceInfo.imei = imei;
		clientId = MD5Utils.string2MD5(System.currentTimeMillis() + "");
		deviceInfo.setUserAgent(userAgent);
		APP_DOWNLOAD_URL = String.format(APP_DOWNLOAD_URL, APP_ID);
		APP_KEYWORD = getAppKeyword();
		MLog.i("app_keyword:" + APP_KEYWORD);

		this.action01ConnetServer(context);
		this.action02GetSuggest();
		this.action03Search();
		this.action04AppDetail();
		this.action05GetDownloadURL();
		this.action06DownloadAPK();
		return this.action07UpdateDataV2(context)
				&& this.action08UpdateData(context);
	}

	private void action01ConnetServer(Context context) {
		try {
			MLog.v("action01ConnetServer");
			HttpPost httpPost = new HttpPost(ACTION_CONNECT_SERVER);

			httpPost.addHeader("Accept-Encoding", "gzip");
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");

			// Post data
			String postData = getPostData(context);
			sessionId = Base64.encodeToString(
					UUID.randomUUID().toString().getBytes(), Base64.DEFAULT)
					.substring(0, 16);
			MLog.i("postData:info=" + postData + "&sessionId=" + sessionId);

			List<NameValuePair> httpParams = new ArrayList<NameValuePair>();
			httpParams.add(new BasicNameValuePair("info", postData));
			httpParams.add(new BasicNameValuePair("sessionId", sessionId));
			httpPost.setEntity(new UrlEncodedFormEntity(httpParams, HTTP.UTF_8));

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
			HttpResponse response = client.execute(httpPost);
			int statuscode = response.getStatusLine().getStatusCode();
			MLog.i("statusCode:" + statuscode);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("connetServer=>" + body);
			JSONObject object = new JSONObject(body);
			if (1 == object.getInt("code")) {
				sessionId = object.getString("message");
				MLog.i("sessionId:" + sessionId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void action02GetSuggest() {
		try {
			MLog.v("action02GetSuggest");
			String url = String.format(ACTION_GET_SUGGEST, clientId,
					MD5Utils.string2MD5(deviceInfo.imei),
					URLEncoder.encode(APP_KEYWORD, HTTP.UTF_8), version,
					deviceInfo.verCode, sessionId);

			MLog.i("action02GetSuggest:" + url);
			// Request
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Accept-Encoding", "gzip");
			// int range = ((int) (Math.random() * 1000));
			// MLog.i("range:" + range + "-" + (range + 2));
			// httpGet.addHeader("Range", "bytes=" + range + "-" + (range + 2));

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

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

	private void action03Search() {
		try {
			MLog.v("action03Search");
			String url = String.format(ACTION_SEARCH, clientId,
					MD5Utils.string2MD5(deviceInfo.imei),
					URLEncoder.encode(APP_KEYWORD, HTTP.UTF_8), version,
					deviceInfo.verCode, sessionId);
			MLog.i("action03Search:" + url);
			// Request
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Accept-Encoding", "gzip");

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			// Response
			HttpResponse response = client.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);

			// String content = u
			// .readContentFromHttpResponse(response, HTTP.UTF_8);
			// MLog.i("content:" + content);
			String content = u
					.readContentFromHttpResponse(response, HTTP.UTF_8);
			JSONObject object = new JSONObject(content);
			JSONArray array = object.getJSONArray("listApp");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				if (getPackageName().equals(obj.getString("packageName"))) {
					APP_ID = obj.getString("id") + "";
					String display = obj.getString("displayName");
					String devloper = obj.getString("publisherName");
					MLog.i(display + ":" + APP_ID + "-" + devloper);
					refPosition = i;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void action04AppDetail() {
		try {
			MLog.v("action04AppDetail");
			String url = String.format(ACTION_APP_DETAIL, APP_ID, clientId,
					MD5Utils.string2MD5(deviceInfo.imei), refPosition, version,
					deviceInfo.verCode, sessionId);
			MLog.i("action04AppDetail:" + url);
			// Request
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Accept-Encoding", "gzip");

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			// Response
			HttpResponse response = client.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);

			String content = u
					.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("content:" + content);
			JSONObject object = new JSONObject(content);
			APP_DOWNLOAD_URL = object.getString("host");
			APP_VERSION_CODE = object.getJSONObject("app").getString(
					"versionCode");
			MLog.d("download apk host:" + APP_DOWNLOAD_URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void action05GetDownloadURL() {
		try {
			String signature = u.generateRandCharNum(11) + "+"
					+ u.generateRandCharNum(3) + "+" + u.generateRandCharNum(9)
					+ "/" + u.generateRandCharNum(1) + "=";
			MLog.v("action05GetDownloadURL");
			String url = String.format(ACTION_GET_DOWNLOAD_URL, APP_ID,
					clientId, MD5Utils.string2MD5(deviceInfo.imei),
					refPosition, version, deviceInfo.verCode, sessionId,
					URLEncoder.encode(signature, "UTF-8"));
			MLog.i("action05GetDownloadURL:" + url);

			// Request
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Accept-Encoding", "gzip");

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			// Response
			HttpResponse response = client.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);

			String content = u
					.readContentFromHttpResponse(response, HTTP.UTF_8);
			JSONObject object = new JSONObject(content);
			APP_DOWNLOAD_URL += object.getString("apk");
			MLog.d("download url:" + APP_DOWNLOAD_URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void action06DownloadAPK() {
		try {
			MLog.v("DownloadAPK");
			MLog.v("downloadURL:" + APP_DOWNLOAD_URL);
			// Request
			HttpGet httpGet = new HttpGet(APP_DOWNLOAD_URL);
			int range = ((int) (Math.random() * 300));
			MLog.i("range:" + range + "-" + (range + 2));
			httpGet.addHeader("Range", "bytes=" + range + "-" + (range + 2));

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			String downloadAgent = String
					.format("AndroidDownloadManager/%1$s (Linux; U; Android %1$s; %2$s Build/JRO03L)",
							deviceInfo.verName, deviceInfo.brand);
			HttpProtocolParams.setUserAgent(params, downloadAgent);
			client.setParams(params);

			// Response
			HttpResponse response = client.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);

			String content = u
					.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.v("content:" + content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean action07UpdateDataV2(Context context) {
		try {
			MLog.v("action07UpdateDataV2");
			HttpPost httpPost = new HttpPost(ACTION_UPDATE_INFOV2);

			httpPost.addHeader("Accept-Encoding", "gzip");
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");

			// Post data
			String postData = getUpdateInfo(context) + "&versionCode="
					+ APP_VERSION_CODE;
			MLog.d("postData:" + postData);
			httpPost.setEntity(new StringEntity(postData, HTTP.UTF_8));

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
			HttpResponse response = client.execute(httpPost);
			int statuscode = response.getStatusLine().getStatusCode();
			MLog.i("statusCode:" + statuscode);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("updateInfo2=>" + body);
			JSONObject object = new JSONObject(body);
			JSONArray array1 = object.getJSONArray("invalidPackage");
			JSONArray array2 = object.getJSONArray("listApp");
			if (array1 != null && array2 != null && array1.length() < 1
					&& array2.length() < 1) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean action08UpdateData(Context context) {
		try {
			MLog.v("action08UpdateData");
			HttpPost httpPost = new HttpPost(ACTION_UPDATE_INFO);

			httpPost.addHeader("Accept-Encoding", "gzip");
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");

			// Post data
			String postData = getUpdateInfo(context);
			MLog.d("postData:" + postData);
			httpPost.setEntity(new StringEntity(postData, HTTP.UTF_8));

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
			HttpResponse response = client.execute(httpPost);
			int statuscode = response.getStatusLine().getStatusCode();
			MLog.i("statusCode:" + statuscode);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("updateInfo=>" + body);
			JSONObject object = new JSONObject(body);
			if (statuscode == HttpStatus.SC_OK) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private String getPostData(Context context) {
		String postData = u.getInstance().getReqestContent(
				"xiaomi_connect.json");
		postData = String.format(postData, deviceInfo.brand,
				deviceInfo.screenSize, u.getAPN(context).toLowerCase(),
				marketVersionName, version, deviceInfo.imei);
		MLog.d("postData:" + postData);
		try {
			postData = Base64.encodeToString(postData.getBytes(HTTP.UTF_8),
					Base64.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return postData;
	}

	private String getUpdateInfo(Context context) {
		String postData = "clientId=%1$s&co=CN&imei=%2$s&la=zh&os=%3$s&packageName=%4$s&sdk=%5$s&session=%6$s";
		postData = String.format(postData, clientId,
				MD5Utils.string2MD5(deviceInfo.imei), version,
				getPackageName(), deviceInfo.verCode, sessionId);
		return postData;
	}

	public String getAPP_ID() {
		return APP_ID;
	}

	public void setAPP_ID(String aPP_ID) {
		APP_ID = aPP_ID;
	}

	public String getMarketVersionName() {
		return marketVersionName;
	}

	public void setMarketVersionName(String marketVersionName) {
		this.marketVersionName = marketVersionName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	protected void initAllParams() {
		try {
			setMarketVersionName(params.get("m_marketVersionName"));
			setVersion(params.get("m_version"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
