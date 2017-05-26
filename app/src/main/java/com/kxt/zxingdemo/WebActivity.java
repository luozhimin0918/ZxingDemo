package com.kxt.zxingdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.Result;
import com.kxt.zxingdemo.util.ScanningImageTools;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

/**
 * 有问题请关注微信公众号  aikaifa
 * QQ交流群 154950206
 */
public class WebActivity extends Activity {
	protected ProgressWebView mWebView;


	private WebSettings mWebSettings;
	private String imageUrl = "";
	private LinearLayout top;
	private TextView EQCodeView,SaveBimap;
	private Bitmap bitmaps = null;
	private String EQResult = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		top = (LinearLayout) this.findViewById(R.id.top_layout);
		mWebView = (ProgressWebView) findViewById(R.id.baseweb_webview);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
				requestAlertWindowPermission();
			}
		}

		mWebView.getSettings().setJavaScriptEnabled(true);

		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}
		});


		mWebSettings = mWebView.getSettings();
		mWebSettings.setJavaScriptEnabled(true);
		mWebSettings.setSupportZoom(true);
		mWebSettings.setBuiltInZoomControls(true);
		mWebSettings.setUseWideViewPort(true);
		mWebSettings.setLoadWithOverviewMode(true);
		mWebView.setWebViewClient(new WebViewClient());
		mWebView.setOnLongClickListener(new View.OnLongClickListener() {

			public boolean onLongClick(View v) {
				WebView.HitTestResult result = ((WebView) v).getHitTestResult();
				if (null != result) {
					int type = result.getType();
					if (type == WebView.HitTestResult.IMAGE_TYPE
							|| type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
						imageUrl = result.getExtra();
						Log.e("TAG", "image -- " + imageUrl);
						showPopupWindow(top, imageUrl);
					}
				}
				return false;
			}
		});
		mWebView.loadUrl("http://www.dyhjw.com/");
	}
	private static final int REQUEST_CODE = 1;

	private void requestAlertWindowPermission() {
		ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);
	}

	private void showPopupWindow(View view, String url) {
		View contentView = View.inflate(this, R.layout.popuwoindow_item, null);
		final PopupWindow popupWindow = new PopupWindow(contentView,LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		EQCodeView = (TextView) contentView.findViewById(R.id.popuwoindow_eqCode);
		SaveBimap=(TextView) contentView.findViewById(R.id.saveBimap);
		EQCodeView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (EQResult == null || "".equals(EQResult))
					return;
				Log.e("TAG", "二维码的地址 -- " + EQResult);
				if(EQResult.startsWith("http://weixin.qq.com/r")){
					Intent intent = null;
					try {
						intent = Intent.parseUri("weixin://", Intent.URI_INTENT_SCHEME);
						startActivity(intent);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}catch (Exception e){
						e.printStackTrace();
						Toast.makeText(getApplicationContext(),"未安装此应用",Toast.LENGTH_SHORT).show();
					}
				}else{
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(EQResult));
					startActivity(intent);
				}
				popupWindow.dismiss();
				Toast.makeText(WebActivity.this, EQResult, Toast.LENGTH_LONG).show();
				//EQResult = "";
				//popupWindow.dismiss();
			}
		});
		SaveBimap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (null != imageUrl) {
					popupWindow.dismiss();
					new SaveImage().execute(); // Android 4.0以后要使用线程来访问网络
				}
			}
		});
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		//popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.selectmenu_bg_downward));
		popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
		ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {
			public void onLoadingStarted(String imageUri, View view) {
			}
			public void onLoadingFailed(String imageUri, View view,
										FailReason failReason) {
			}
			public void onLoadingComplete(String imageUri, View view,
										  Bitmap loadedImage) {
				// TODO Auto-generated method stub
				if (loadedImage == null)
					return;
				bitmaps = loadedImage;
				ScanningImageTools.scanningImage(loadedImage,
						new ScanningImageTools.IZCodeCallBack() {
							public void ZCodeCallBackUi(Result result) {
								if (result == null) {
									handler.sendEmptyMessage(0);
								} else {
									handler.sendEmptyMessage(1);
									EQResult = ScanningImageTools.recode(result.toString());
								}
							}
						});
			}
			public void onLoadingCancelled(String imageUri, View view) {
			}
		});
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (EQCodeView == null)
				return;
			switch (msg.what) {
				case 0:
					setVISIBLE(EQCodeView, false);
					break;
				case 1:
					setVISIBLE(EQCodeView, true);
					break;
				case 2:
					break;
				default:
					break;
			}
		}
	};
	public interface onGetBitMapListener {
		void getBitMap(Bitmap bit);
	}
	public void setVISIBLE(View v, boolean falg) {
		if (falg) {
			if (View.GONE == v.getVisibility()) {
				v.setVisibility(View.VISIBLE);
			}
		} else {
			if (View.VISIBLE == v.getVisibility()) {
				v.setVisibility(View.GONE);
			}
		}
	}
	/***
	 * 功能：用线程保存图片
	 *
	 * @author wangyp
	 *
	 */
	private class SaveImage extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String result = "";
			try {
				String sdcard = Environment.getExternalStorageDirectory()
						.toString();
				File file = new File(sdcard + "/Download");
				if (!file.exists()) {
					file.mkdirs();
				}
				int idx = imageUrl.lastIndexOf(".");
				String ext = imageUrl.substring(idx);
				file = new File(sdcard + "/Download/" + new Date().getTime()
						+ ext);
				InputStream inputStream = null;
				URL url = new URL(imageUrl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(20000);
				if (conn.getResponseCode() == 200) {
					inputStream = conn.getInputStream();
				}
				byte[] buffer = new byte[4096];
				int len = 0;
				FileOutputStream outStream = new FileOutputStream(file);
				while ((len = inputStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, len);
				}
				outStream.close();
				result = "图片已保存至：" + file.getAbsolutePath();
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
						Uri.fromFile(file)));
			} catch (Exception e) {
				result = "保存失败！" + e.getLocalizedMessage();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
		}
	}
}
