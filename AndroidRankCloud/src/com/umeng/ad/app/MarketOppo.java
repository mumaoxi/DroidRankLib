package com.umeng.ad.app;

import java.net.URLEncoder;

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

import android.content.Context;

import com.umeng.ad.app.u.TimeExtra;

class MarketOppo extends Market {

	private String APP_ID = "";
	private String DOWNLOAD_FROM = "";
	private String DOWNLOAD_URL = "";
	private String APP_KEYWORD = "";
	private String detailParam;
	private DeviceInfo deviceInfo;

	public MarketOppo() {
	}

	public MarketOppo(String packageName) {
		MARKET_NAME = MARKET_OPPO;
		setPackageName(packageName);
	}

	@Override
	protected boolean prepareToRank(Context context) throws Exception {
		deviceInfo = new DeviceInfo(context);
		String userAgent = u.getInstance().getAgent(
				(int) (System.currentTimeMillis() % 14));
		deviceInfo.setUserAgent(userAgent);
		int random = (System.currentTimeMillis() % 2) == 0 ? 1 : ((int) (Math
				.random() * 5));
		String[] from1 = { "1152_" + random };
		// Download from
		DOWNLOAD_FROM = from1[(int) (System.currentTimeMillis() % from1.length)];
		// Download URL
		DOWNLOAD_URL = "http://store.nearme.com.cn/product/download.html?id="
				+ APP_ID + "&" + detailParam.split("\\?")[1];
		// keyword
		APP_KEYWORD = getAppKeyword();

		/**
		 * Step1.DownloadAPK
		 */
		this.downloadAPK();

		/**
		 * Step2.Analysis
		 */
		return getAnalysis();
	}

	/**
	 * Download android APK file
	 */
	private void downloadAPK() {
		try {
			HttpGet httpGet = new HttpGet(DOWNLOAD_URL);
			int range = ((int) (Math.random() * 300));
			MLog.i("range:" + range + "-" + (range + 2));
			httpGet.addHeader("Range", "bytes=" + range + "-" + (range + 2));
			MLog.i("URL:" + DOWNLOAD_URL);

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
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);
			int code = response.getStatusLine().getStatusCode();
			MLog.i("downloadWebPre=>status_code:" + code);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.v("download content:" + body);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get Analysis
	 */
	private boolean getAnalysis() {
		try {
			StringBuffer stringBuffer = new StringBuffer("SYSTEM_ID="
					+ (System.currentTimeMillis() % 2 == 0 ? 1 : 2) + "&");
			stringBuffer.append("CHANNEL_ID=2&");
			stringBuffer.append("SOURCE_CODE=" + DOWNLOAD_FROM + "&");
			stringBuffer.append("PRODUCT_ID=" + APP_ID + "&");
			stringBuffer.append("WUID=WEB" + u.generateRandCharNum(12) + "&");
			stringBuffer.append("CLICK_INDEX=" + (DOWNLOAD_FROM.split("_")[1])
					+ "&");

			stringBuffer.append("REFER=");
			StringBuffer refer = new StringBuffer(
					"http://store.nearme.com.cn/search/do.html?");
			String keyword = APP_KEYWORD;
			keyword = URLEncoder.encode(keyword, "UTF-8");
			refer.append("keyword=" + keyword + "&");
			refer.append("nav=index&");
			refer.append("BROWSER=" + deviceInfo.getUserAgent());
			stringBuffer.append(URLEncoder.encode(refer.toString(), "UTF-8"));
			String url = "http://i.stat.nearme.com.cn/statistics/ClientDownloadByUrl";
			HttpGet httpGet = new HttpGet(url + "?" + stringBuffer.toString());

			// Headers
			String downloadReferer = String
					.format("http://store.nearme.com.cn/product/" + detailParam);
			httpGet.addHeader("Referer", downloadReferer);
			MLog.i("downloadReferer:" + downloadReferer);

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
			HttpResponse response = client.execute(httpGet);
			int code = response.getStatusLine().getStatusCode();
			MLog.i("getAnalysis=>status_code:" + code);

			if (code == HttpStatus.SC_OK) {
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

	public String getDetailParam() {
		return detailParam;
	}

	public void setDetailParam(String detailParam) {
		this.detailParam = detailParam;
	}

	@Override
	protected void initAllParams() {
		try {
			setAPP_ID(params.get("a_appId"));
			setDetailParam(params.get("a_detailParam"));
		} catch (Exception e) {
		}
	}

}
