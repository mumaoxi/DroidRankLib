package com.example.androidrank;

import android.app.Application;
import android.content.Context;

public class QiaomingApplication extends Application {
	public static Context context;
	@Override
	public void onCreate() {
		context = this.getApplicationContext();
		super.onCreate();
	}
}
