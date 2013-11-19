package com.umeng.ad.app;

import android.util.Log;

 class MLog {
	protected static boolean logEnable = true;

	protected static void i(Object object) {
		if (MLog.logEnable) {
			Log.i("MobclickAgent", object != null ? object.toString() : "null");
		}
	}
	
	protected static void d(Object object) {
		if (MLog.logEnable) {
			Log.d("MobclickAgent", object != null ? object.toString() : "null");
		}
	}
	
	protected static void e(Object object) {
		if (MLog.logEnable) {
			Log.e("MobclickAgent", object != null ? object.toString() : "null");
		}
	}
	
	protected static void v(Object object) {
		if (MLog.logEnable) {
			Log.v("MobclickAgent", object != null ? object.toString() : "null");
		}
	}
	
	protected static void w(Object object) {
		if (MLog.logEnable) {
			Log.w("MobclickAgent", object != null ? object.toString() : "null");
		}
	}
}
