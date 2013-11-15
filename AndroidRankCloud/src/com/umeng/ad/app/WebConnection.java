package com.umeng.ad.app;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;

import com.umeng.ad.app.u.CompressType;

class WebConnection {
	private String url;
	private Map<String, String> headers = new HashMap<String, String>();
	private Map<String, Object> params = new HashMap<String, Object>();
	private String userAgent = "";
	private String charset = "utf-8";
	private String jsonString;
	private String httpMethod = "get";
	private CompressType compressType;
	private int tag;

	WebConnection(Context context) {
		userAgent = "umeng_rank_config_"+u.getAPN(context)+"_#"+u.getIpAddress(context)+"#_"
				+u.getWifiMac(context)+"_("+u.getCountry()+"_"+u.getLanguage()+")_"
				+u.getManufacturer(context)+":"+u.getModel(context)+"=>"+u.getIMEI(context);
	}

	WebConnection(String url, Map<String, String> headers,
			Map<String, Object> params, String userAgent, String charset,
			String httpMethod) {
		this.url = url;
		this.headers = headers;
		this.params = params;
		this.userAgent = userAgent;
		this.charset = charset;
		this.httpMethod = httpMethod;
	}

	public HashMap<String, String> doGet() throws IOException, Exception {
		url += genrateUrlparams(params);
		MLog.d("doGet,Url:" + url + "\nheader:" + headers);
		HttpGet httpGet = new HttpGet(url);
		Set<String> headerKeys = headers.keySet();
		for (String key : headerKeys) {
			httpGet.setHeader(key, headers.get(key));
		}

		DefaultHttpClient client = new DefaultHttpClient();
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setUserAgent(params, userAgent);
		client.setParams(params);
		HttpResponse response = client.execute(httpGet);

		int status_code = response.getStatusLine().getStatusCode();
		String body = u.readContentFromHttpResponse(response, charset);

		HashMap<String, String> resHashMap = new HashMap<String, String>();
		resHashMap.put("body", body);
		resHashMap.put("status_code", String.valueOf(status_code));
		resHashMap.put("tag", tag + "");
		MLog.d("doGet, before exit, resHashMap == " + resHashMap);
		return resHashMap;
	}

	/**
	 * 
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public HashMap<String, String> doPost() throws IOException {
		MLog.d("doPost,Url:" + url + "\nheader:" + headers + "\nparams:"
				+ params);

		/**
		 * Post
		 */
		HttpPost httpPost = new HttpPost(url);
		Set<String> headerKeys = headers.keySet();
		for (String key : headerKeys) {
			httpPost.setHeader(key, headers.get(key));
		}

		/**
		 * Params
		 */

		// new Nam

		List<NameValuePair> httpParams = new ArrayList<NameValuePair>();
		Set<String> paramsKeys = params.keySet();
		for (String key : paramsKeys) {
			httpParams.add(new BasicNameValuePair(key, String.valueOf(params
					.get(key))));
		}
		httpPost.setEntity(new UrlEncodedFormEntity(httpParams, HTTP.UTF_8));

		DefaultHttpClient client = new DefaultHttpClient();
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setUserAgent(params, userAgent);
		client.setParams(params);
		HttpResponse response = client.execute(httpPost);

		int status_code = response.getStatusLine().getStatusCode();
		String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);

		HashMap<String, String> resHashMap = new HashMap<String, String>();
		resHashMap.put("body", body);
		resHashMap.put("status_code", String.valueOf(status_code));
		resHashMap.put("tag", tag + "");
		return resHashMap;
	}

	public HashMap<String, String> doPostJson() throws Exception {
		MLog.d("doPost,Url:" + url + "\nheader:" + headers);

		/**
		 * Post
		 */
		HttpPost httpPost = new HttpPost(url);
		Set<String> headerKeys = headers.keySet();
		for (String key : headerKeys) {
			httpPost.setHeader(key, headers.get(key));
		}

		/**
		 * Params
		 */
		if (compressType == CompressType.GZIP) {
			httpPost.addHeader("Content-Encoding", "gzip");
			httpPost.setEntity(u.compressPostBodyWithGzip(jsonString));
		} else if (compressType == CompressType.DEFLATE) {
			httpPost.addHeader("Content-Encoding", "deflate");
			httpPost.setEntity(u.compressPostBodyWithDeflate(jsonString));
		} else
			httpPost.setEntity(new StringEntity(jsonString, HTTP.UTF_8));
		MLog.i("postJson:" + jsonString);

		DefaultHttpClient client = new DefaultHttpClient();
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setUserAgent(params, userAgent);
		client.setParams(params);
		HttpResponse response = client.execute(httpPost);

		int status_code = response.getStatusLine().getStatusCode();
		String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);

		HashMap<String, String> resHashMap = new HashMap<String, String>();
		resHashMap.put("body", body);
		resHashMap.put("status_code", String.valueOf(status_code));
		resHashMap.put("tag", tag + "");
		return resHashMap;
	}

	public HashMap<String, String> doPut(String url,
			Map<String, String> headers, String jsonString) throws Exception {
		MLog.d("doPut,Url:" + url + "\nheader:" + headers);

		/**
		 * Put
		 */
		HttpPut httpPut = new HttpPut(url);
		Set<String> headerKeys = headers.keySet();
		for (String key : headerKeys) {
			httpPut.setHeader(key, headers.get(key));
		}

		/**
		 * Params
		 */
		httpPut.setEntity(new StringEntity(jsonString, HTTP.UTF_8));
		MLog.i("putJson:" + jsonString);

		DefaultHttpClient client = new DefaultHttpClient();
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setUserAgent(params, userAgent);
		client.setParams(params);
		HttpResponse response = client.execute(httpPut);

		int status_code = response.getStatusLine().getStatusCode();
		String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);

		HashMap<String, String> resHashMap = new HashMap<String, String>();
		resHashMap.put("body", body);
		resHashMap.put("status_code", String.valueOf(status_code));
		resHashMap.put("tag", tag + "");
		return resHashMap;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public CompressType getCompressType() {
		return compressType;
	}

	public void setCompressType(CompressType compressType) {
		this.compressType = compressType;
	}

	/**
	 * 
	 * @param params
	 * @return
	 */
	private static String genrateUrlparams(Map<String, Object> params) {
		if (params == null || params.size() < 1) {
			return "";
		}
		StringBuffer urlString = new StringBuffer("?");
		Set<String> keySet = params.keySet();
		for (String key : keySet) {
			try {
				urlString.append(key
						+ "="
						+ URLEncoder.encode(String.valueOf(params.get(key)),
								HTTP.UTF_8) + "&");
			} catch (Exception e) {
			}
		}
		urlString = new StringBuffer(urlString.reverse().toString()
				.replaceFirst("&", ""));
		return urlString.reverse().toString();
	}

}
