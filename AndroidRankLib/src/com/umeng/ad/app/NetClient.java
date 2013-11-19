/**
 * 
 */
package com.umeng.ad.app;

import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * @author Stream
 * 
 */
class BaseHttpClient extends DefaultHttpClient {

	private String userAgent = "";

	BaseHttpClient(String userAgent) {
		this.userAgent = userAgent;
	}

	BaseHttpClient() {
		HttpParams params = new BasicHttpParams();

		HttpConnectionParams.setConnectionTimeout(params, 60 * 1000);
		HttpConnectionParams.setSoTimeout(params, 60 * 1000);
		HttpConnectionParams.setSocketBufferSize(params, 8192);

		HttpClientParams.setRedirecting(params, true);

		HttpProtocolParams.setUserAgent(params, userAgent);
		setParams(params);
	}
}
