package com.umeng.ad.app;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;

import com.umeng.ad.app.u.TimeExtra;

class MarketCoolSmart extends Market {

	private String APP_ID = "";
	private String ResourceVesionId = "";
	private DeviceInfo deviceInfo;

	public MarketCoolSmart() {
		// TODO Auto-generated constructor stub
	}

	public MarketCoolSmart(String packageName) {
		MARKET_NAME = MARKET_COOLSMART;
		setPackageName(packageName);
	}

	@Override
	protected boolean prepareToRank(Context context) throws Exception {
		deviceInfo = new DeviceInfo(context);
		return postAnalysis(context);
	}

	/**
	 * 
	 */
	private boolean postAnalysis(Context context) {
		try {
			String userAgent = "Apache-HttpClient/UNAVAILABLE (java 1.4)";
//			String url = "http://www.coolmart.net.cn/developer/coolmart/resdownload.action";
			String url ="http://www.coolmart.net.cn/developer/coolmart/resdownloadTempB.action";
			MLog.v("CooSmart:" + url);
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");
			// String content = u.getInstance().getReqestContent(
			// "coolmart_post.json");
			String content = "resId=%1$s&resourceVersionId=%2$s";
			// content = String.format(content, u.generateImei(deviceInfo.imei),
			// deviceInfo.verCode,
			// deviceInfo.getScreenSize().replace("X", "*"),
			// deviceInfo.brand, u.getAPN(context).toUpperCase(),
			// deviceInfo.imei, APP_ID);

			content = String.format(content, APP_ID, ResourceVesionId);
			MLog.d("coolmart content:" + content);
			httpPost.setEntity(new StringEntity(content, HTTP.UTF_8));

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
			HttpResponse response = client.execute(httpPost);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("coolmart postAnalysis=>status_code:" + code);
			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.d("coolmart body:" + body);
			if (body.indexOf("<freedown>") > 0) {
				String downloadUrl = body.substring(
						body.indexOf("<freedown>") + 10,
						body.indexOf("</freedown>"));
				MLog.d("downloadurl:" + downloadUrl);
				if (downloadUrl != null && downloadUrl.trim().length() > 1) {
					return true;
				}
			}
		} catch (Exception e) {
			MLog.e("exception:" + e.getMessage());
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

	public void setResourceVesionId(String resourceVesionId) {
		ResourceVesionId = resourceVesionId;
	}

	@Override
	protected void initAllParams() {
		try {
			setAPP_ID(params.get("a_appId"));
			setResourceVesionId(params.get("a_resourceVersionId"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
