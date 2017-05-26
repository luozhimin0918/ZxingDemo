package com.kxt.zxingdemo;

import android.app.Application;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class BaseApplication extends Application {
@Override
public void onCreate() {
	// TODO Auto-generated method stub
	super.onCreate();
	ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())  
    .threadPriority(Thread.NORM_PRIORITY - 2)  
    .denyCacheImageMultipleSizesInMemory()  
    .discCacheFileNameGenerator(new Md5FileNameGenerator())  
    .tasksProcessingOrder(QueueProcessingType.LIFO)  
    .build();  
    ImageLoader.getInstance().init(config);  
}
}
