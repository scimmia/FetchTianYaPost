package global;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: mac
 * Date: 13-9-4
 * Time: 下午4:18
 * To change this template use File | Settings | File Templates.
 */
public class HttpClientUtil implements GlobalConstant{
    static Logger logger = Logger.getLogger("http");

    /**
     * 根据URL获得所有的html信息
     * @param url
     * @return html
     */
    public static String getHtmlByUrl(String url){
        String html = null;
        //创建httpClient对象
        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        //以get方式请求该URL
        HttpGet httpget = new HttpGet(url);
        httpget.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        httpget.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, CONNECTION_TIMEOUT);
        try {
            //得到responce对象
            HttpResponse responce = httpClient.execute(httpget);
            //返回码
            int resStatu = responce.getStatusLine().getStatusCode();
            //200正常  其他就不对
            if (resStatu== HttpStatus.SC_OK) {
                //获得相应实体
                HttpEntity entity = responce.getEntity();
                if (entity!=null) {
                    //获得html源代码
                    html = EntityUtils.toString(entity);
                    EntityUtils.consume(entity);
                }
            }
        } catch (Exception e) {
            logger.error("访问【"+url+"】出现异常!");
            logger.error(e);
        } finally {
            httpget.abort();
            httpClient.getConnectionManager().shutdown();
        }
        return html;
    }

}