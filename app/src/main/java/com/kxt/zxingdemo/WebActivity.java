package com.kxt.zxingdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

/**
 * 有问题请关注微信公众号  aikaifa
 * QQ交流群 154950206
 */
public class WebActivity extends Activity {
	protected ProgressWebView mWebView;


	private WebSettings mWebSettings;
	private String imageUrl = "";
	private LinearLayout top;
	private TextView EQCodeView;
	private Bitmap bitmaps = null;
	private String EQResult = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		top = (LinearLayout) this.findViewById(R.id.top_layout);
		mWebView = (ProgressWebView) findViewById(R.id.baseweb_webview);
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
		mWebView.loadUrl("http://template.yingtongjinfu.com/zgc/src/index.html");
	}

	private void showPopupWindow(View view, String url) {
		View contentView = View.inflate(this, R.layout.popuwoindow_item, null);
		final PopupWindow popupWindow = new PopupWindow(contentView,LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		EQCodeView = (TextView) contentView.findViewById(R.id.popuwoindow_eqCode);
		EQCodeView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (EQResult == null || "".equals(EQResult))
					return;
				Toast.makeText(WebActivity.this, EQResult, Toast.LENGTH_LONG).show();
				//EQResult = "";
				//popupWindow.dismiss();
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
}
