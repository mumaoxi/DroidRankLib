/**
 * 
 */
package com.umeng.ad.app;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.content.Context;
import android.os.AsyncTask;

/**
 * @author Stream
 * 
 */
class NetCenter {

	private static NetCenter playNetworkCenter;

	private NetCenter() {

	}

	static NetCenter getInstance() {
		if (playNetworkCenter == null) {
			playNetworkCenter = new NetCenter();
		}
		return playNetworkCenter;
	}

	/**
	 * 
	 * @param request
	 * @param listener
	 * @param context
	 * @return A 'NetworkTask' will be returned, and you can control the task as
	 *         you like.For example, <code>network.cancel(true);</code><br>
	 *         This can cancel the current request,and of course, the result
	 *         will not be called back to the main thread.
	 */
	NetworkTask startRequest(WebConnection connection, RequestListener listener,Context context) {

		MLog.i("startRequest.listener==>" + listener);
		NetworkTask task = new NetworkTask(listener, context);
		try {
			task.execute(connection);
		} catch (Exception e) {
			MLog.e("" + e.getMessage());
		}
		return task;
	}

	class NetworkTask extends
			AsyncTask<WebConnection, Void, HashMap<String, String>> {
		private final WeakReference<RequestListener> listenerReference;
		private final WeakReference<Context> contextReference;

		public NetworkTask(RequestListener listener, Context context) {
			this.listenerReference = new WeakReference<RequestListener>(
					listener);
			this.contextReference = new WeakReference<Context>(context);
		}

		@Override
		protected HashMap<String, String> doInBackground(
				WebConnection... params) {
			MLog.d("DoInBackground..." + params[0]);
			try {
				if (params[0].getHttpMethod() != null
						&& params[0].getHttpMethod().toLowerCase().equals("get")) {
					return params[0].doGet();
				}
				if (params[0].getHttpMethod() != null
						&& params[0].getHttpMethod().toLowerCase().equals("post_json")) {
					return params[0].doPostJson();
				}
			} catch (Exception e) {
				MLog.e("e" + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			MLog.w("oncalled");
			super.onCancelled();
		}

		/**
		 * If current task has not been canceled and the WeakReference of
		 * <code>{@link #listenerReference}</code> is not null,or the
		 * WeakReference of <code>{@link #handlerReference}</code>,the result
		 * will be returned to {@link #listenerReference} or
		 * {@link #handlerReference}
		 */
		@Override
		protected void onPostExecute(HashMap<String, String> response) {

//			MLog.d("onPostExecute response == " + response);

			/**
			 * Method 1.
			 */
			if (!isCancelled() && this.listenerReference != null) {
				RequestListener listener = listenerReference.get();

				MLog.d("onPostExecute  listenerReference == "
						+ listenerReference);
				MLog.d("onPostExecute  listener == " + listener);

				if (listener != null) {
					try {
						listener.onRequestFinished(contextReference.get(),
								response);
					} catch (Exception e) {
						MLog.e("e" + e.getMessage());
					}
				}
				MLog.d("onPostExecute exit");
			}

		}
	}

	interface RequestListener {

		void onRequestFinished(Context context,
				HashMap<String, String> requestFinish);

	}

}
