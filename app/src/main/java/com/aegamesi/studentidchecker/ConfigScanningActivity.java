package com.aegamesi.studentidchecker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.aegamesi.studentidchecker.util.AndroidUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.function.Consumer;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ConfigScanningActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {
	private static final int PERMISSION_REQUEST_CAMERA = 4;

	private ZBarScannerView scannerView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config_scanning);

		if (!requireCameraPermission()) {
			finish();
			return;
		}

		scannerView = new ZBarScannerView(this);
		scannerView.setFormats(Collections.singletonList(BarcodeFormat.QRCODE));
		FrameLayout cameraFrame = (FrameLayout) findViewById(R.id.camera_frame);
		cameraFrame.addView(scannerView);
		scannerView.setResultHandler(this);
		scannerView.startCamera();
	}

	@SuppressLint("NewApi")
	private boolean requireCameraPermission() {
		if (!AndroidUtil.haveCameraPermission(this)) {
			requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
			// Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	@Override
	public void handleResult(Result result) {
		String url = result.getContents();

		DownloadFileTask task = new DownloadFileTask(this, (file) -> {
			if (file == null) {
				setResult(RESULT_CANCELED, new Intent());
			} else {
				Intent data = new Intent();
				data.setData(Uri.fromFile(file));
				setResult(RESULT_OK, data);
			}
			finish();
		});
		task.execute(url);
	}

	private interface FileReceiver {
		void receive(File file);
	}

	private static class DownloadFileTask extends AsyncTask<String, Integer, File> {
		private ProgressDialog dialog;
		private WeakReference<Context> contextReference;
		private File filesDir;
		private FileReceiver callback;

		DownloadFileTask(Context context, FileReceiver callback) {
			this.callback = callback;
			dialog = new ProgressDialog(context);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setMessage(context.getString(R.string.downloading));
			dialog.setMax(100);

			contextReference = new WeakReference<Context>(context);
			filesDir = context.getFilesDir();
		}

		@Override
		protected void onPreExecute() {
			dialog.show();
		}

		@Override
		protected void onPostExecute(File file) {
			dialog.dismiss();
			callback.receive(file);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			int p = values[0];
			if (p < 0) {
				dialog.setIndeterminate(true);
			} else {
				dialog.setProgress(p);
			}
		}

		@Override
		protected File doInBackground(String... urls) {
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection conn = null;
			try {
				URL url = new URL(urls[0]);
				Log.i("DownloadTask", "Downloading " + urls[0]);
				conn = (HttpURLConnection) url.openConnection();
				String authorization = url.getUserInfo();
				if (authorization != null && !authorization.isEmpty()) {
					final String basicAuth = "Basic " + Base64.encodeToString(authorization.getBytes(), Base64.NO_WRAP);
					conn.setRequestProperty("Authorization", basicAuth);
				}
				conn.connect();

				// check status code
				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					Context c = contextReference.get();
					if (c != null) {
						final String msg = "HTTP " + conn.getResponseCode() + " " + conn.getResponseMessage();
						new Handler(c.getMainLooper()).post(() -> {
							Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
						});
					}
					return null;
				}

				// get length
				int length = conn.getContentLength();
				if (length == -1) {
					publishProgress(-1);
				}

				// download the file
				String filename = url.getFile().substring(url.getFile().lastIndexOf('/'));
				if (filename.length() == 0) {
					filename = "download";
				}
				File file = new File(filesDir, filename);
				input = conn.getInputStream();
				output = new FileOutputStream(file);

				byte data[] = new byte[4096];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					// allow canceling with back button
					if (isCancelled()) {
						input.close();
						return null;
					}
					total += count;
					// publishing the progress....
					if (length > 0) {
						publishProgress((int) (total * 100 / length));
					}
					output.write(data, 0, count);
				}

				return file;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (IOException ignored) {
				}

				if (conn != null)
					conn.disconnect();
			}
		}
	}
}
