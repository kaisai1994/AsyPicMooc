package asy.kagura.com;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @version $Rev$
 * @auther kagura
 * @time 2016/7/22.22:00
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateData $Author$
 * @updatedes ${TODO}
 */
public class ImageLoader {
    private LruCache<String,Bitmap> mLruCache;
    private String murl;
    private ImageView mImageView;
    private ListView mListView;
    private Set<NewsAsyncTask> mTask;
    public ImageLoader(ListView listview)
    {
        mListView = listview;
        mTask = new HashSet<NewsAsyncTask>();
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 4;
        mLruCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用
                return value.getByteCount();
            }
        };
    }
    //从缓存中获取数据
    public Bitmap getBitmapFromCache(String url)
    {
        return mLruCache.get(url);

    }
    //添加缓存
    public void addBitmapToCache(String url, Bitmap bitmap)
    {
        if(getBitmapFromCache(url) == null)
        {
            mLruCache.put(url,bitmap);
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mImageView.getTag().equals(murl));
            mImageView.setImageBitmap((Bitmap) msg.obj);
        }
    };
    public void showImageByThread(ImageView imageView, final String url)
    {
        murl = url;
        mImageView = imageView;
        new Thread()
        {
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFromURL(url);
                Message message = Message.obtain();
                message.obj = bitmap;
                mHandler.sendMessage(message);

            }
        }.start();
    }
    public void loadImages(int start,int end)
    {
        System.out.println("loadImages"+start+"|"+end);
        for (int i = start; i < end; i++) {
            String url = NewsAdapter.URLS[i];
            Bitmap bitmap = getBitmapFromCache(url);
            if(bitmap == null)
            {
                NewsAsyncTask task = new NewsAsyncTask(url);
                task.execute(url);
                mTask.add(task);
            }else {
                ImageView imageview = (ImageView) mListView.findViewWithTag(url);
                imageview.setImageBitmap(bitmap);
            }
        }
    }
    public Bitmap getBitmapFromURL(String urlString)
    {
        InputStream is = null;
        Bitmap bitmap;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            is = new BufferedInputStream(conn.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);
            conn.disconnect();
            return bitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public void showImageByAsyncTask(ImageView imageView,String url)
    {
        Bitmap bitmap = getBitmapFromCache(url);
        if(bitmap == null)
        {
            new NewsAsyncTask(url).execute(url);
        }else {
            imageView.setImageBitmap(bitmap);
        }

    }
    public void cancelAllTasks()
    {
        if (mTask != null)
        {
            for (NewsAsyncTask task: mTask) {
                task.cancel(false);
            }
        }
    }
    private class NewsAsyncTask extends AsyncTask<String,Void,Bitmap>
    {
        private String murl;
        //private ImageView mImageView;
        public NewsAsyncTask(String url)
        {

            murl = url;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Bitmap bitmap = getBitmapFromURL(url);
            if (bitmap != null)
            {
                addBitmapToCache(murl,bitmap);
            }
            return bitmap;

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageview = (ImageView) mListView.findViewWithTag(murl);
            if(imageview != null && bitmap == null)
            {
                imageview.setImageBitmap(bitmap);
            }
            mTask.remove(this);
        }
    }
}
