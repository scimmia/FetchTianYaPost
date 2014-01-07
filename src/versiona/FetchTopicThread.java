package versiona;

import global.GlobalConstant;
import global.GlobalUtil;
import global.HttpClientUtil;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by ASUS on 14-1-7.
 */
public class FetchTopicThread implements Runnable, GlobalConstant {
    Logger logger;
    String item;
    String startURL;

    String baseURL = "http://search.tianya.cn/bbs?q=%s&pn=%d&f=3";
    int startPage;
    String tag;
    public FetchTopicThread(String tag, int startPage) {
        try {
            this.tag = URLEncoder.encode(tag, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.startPage = startPage;
        logger = Logger.getLogger("versiona.FetchTopicThread");
    }
    String getUrl(){
        return String.format(baseURL, tag, startPage);
    }
    @Override
    public void run() {
        String url;
        while (GlobalUtil.fetching && startPage <76){
            url = getUrl();
            logger.info("fetch---"+url);
            String response = HttpClientUtil.getHtmlByUrl(url);
//            logger.info(response);
            if (response!=null){
                Document doc = Jsoup.parse(response);
                fetchTopic(doc);
            }




            startPage++;
        }
    }

    public void fetchTopic(Document doc){
        Element element = doc.select("div.searchListOne").last();
        Elements trs = element.select("h3");
        for (Element tr : trs){
//            Element tds = tr.select("h3").first();
            Element href = tr.select("a[href]").first();
            if (href!=null){
            String title = href.text();
            String url = href.attr("href");
//                        String before = "/post-free-3722617-1.shtml";
//            int lastIndex = url.lastIndexOf("-1.shtml");
//            String id = url.substring(7+this.item.length(),lastIndex);
//            deal(id, title, replyCount, clickCount);
//                logger.info(url.replace("-1.shtml","-%d.shtml"));
                logger.info(url+'\t'+title);

            }
        }
    }

    private void deal(String id, String title, int replyCount, int clickCount) {
//        System.out.println(id+'\t'+title+'\t'+replyCount+'\t'+clickCount);
    }
}
