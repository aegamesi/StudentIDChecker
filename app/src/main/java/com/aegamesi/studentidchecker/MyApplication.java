package com.aegamesi.studentidchecker;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;

public class MyApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		Realm.init(this);

		if (BuildConfig.DEBUG) {
			// initialize Stetho
			Stetho.initialize(
					Stetho.newInitializerBuilder(this)
							.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
							.enableWebKitInspector(RealmInspectorModulesProvider.builder(this)
									.build())
							.build());
		}
	}
}
