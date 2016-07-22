package asy.kagura.com;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private static String URL = "http://www.imooc.com/api/teacher?type=4&num=30";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.lv_main);
        new NewsAsynTask().execute(URL);
    }
    class NewsAsynTask extends AsyncTask<String,Void,List<NewsBean>>{
        @Override
        protected List<NewsBean> doInBackground(String... params) {
            return getJsonData(params[0]);
        }
    }

    private List<NewsBean> getJsonData(String param) {
        List<NewsBean> newsBeanList = new ArrayList<>();
        try {
            String jsonString = readStream(new URL(URL).openStream());
            //Log.d("MainActivity",jsonString);
            JSONObject jsonObject = null;
            NewsBean newsBean = null;
            jsonObject = new JSONObject(jsonString);
            JSONArray jsonarray = jsonObject.getJSONArray("data");
            for(int i = 0;i <jsonarray.length();i++)
            {
                jsonObject = jsonarray.getJSONObject(i);
                newsBean.setNewsIconUrl(jsonObject.getString("picSmall"));
                newsBean.setNewsTitle(jsonObject.getString("name"));
                newsBean.setNewsContent(jsonObject.getString("description"));
                newsBeanList.add(newsBean);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newsBeanList;
    }
    private String readStream(InputStream is)
    {
        InputStreamReader isr;
        String result = "";
        try {
            String line = "";
            isr = new InputStreamReader(is,"utf-8");
            BufferedReader br = new BufferedReader(isr);
            while((line = br.readLine())!= null)
            {
                result += line;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
