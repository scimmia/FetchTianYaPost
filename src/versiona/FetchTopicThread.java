package versiona;

import global.GlobalConstant;
import global.GlobalUtil;
import global.HttpClientUtil;
import global.TopicStruct;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by ASUS on 14-1-7.
 */
public class FetchTopicThread implements Runnable, GlobalConstant {
    Logger logger;
    String item;
    int maxPage = 76;

    String baseURL = "http://search.tianya.cn/bbs?q=%s&pn=%d&f=3&s=10";
    int startPage;
    String tag;
    HashSet<TopicStruct> set;
//    HashMap<String,String> map;
    public FetchTopicThread(String item, String tag,int startPage) {
        this.item = item;
        this.tag = tag;
//        this.tag = getTag(item);
//        try {
//            this.tag = URLEncoder.encode(getTag(item), "ISO-8859-1");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        this.startPage = startPage;
        set = new HashSet<TopicStruct>();
//        map = new HashMap<String, String>();
        logger = Logger.getLogger("versiona.FetchTopicThread"+tag);
    }
    String getTag(String str){
        if (str.equals("jiuye")){
            return "就业";
        }
        return "";
    }
    String getUrl(){
        return String.format(baseURL, tag, startPage);
    }
    @Override
    public void run() {
        String url;
        while (GlobalUtil.fetching && startPage <maxPage){
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
        saveData();
        logger.info(String.format("抓取 %s-%s 结束",item,tag));
    }

    private void saveData() {
        String insertSql = "REPLACE INTO "+tableName_Topic+" ("
                +columnName_Topic_id+","
                +columnName_Topic_title+","
                +columnName_Topic_replycount+","
                +columnName_Topic_type
                +") VALUES(?,?,?,?)";
        PreparedStatement ps = null;
        Connection connection = GlobalUtil.getConnToDatabase(item);
        if (connection == null){
            return;
        }
        try {
            ps =connection.prepareStatement(insertSql);
//            for (String key:map.keySet()){
//                ps.setString(1,key);
//                ps.setString(2,map.get(key));
//                ps.addBatch();
//            }
            for (TopicStruct temp:set){
                ps.setString(1,temp.getId());
                ps.setString(2,temp.getTitle());
                ps.setInt(3,temp.getReplyCount());
                ps.setString(4,temp.getTopicType());
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if (ps!=null){
                    ps.close();
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void fetchTopic(Document doc){
        Element element = doc.select("div.searchListOne").last();
        Elements trs = element.select("li");
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
                Element pSource = tr.select("p.source").first();
                String topicType = pSource.select("a[href]").first().text();
                int replyCount = Integer.parseInt(tr.select("span").last().text());
                logger.info(url+'\t'+title);
//                map.put(url.replace("http://bbs.tianya.cn",""),title);
                set.add(new TopicStruct(url.replace("-1.shtml","-%d.shtml"),title,topicType,replyCount));
            }
        }
    }

    private void deal(String id, String title, int replyCount, int clickCount) {
//        System.out.println(id+'\t'+title+'\t'+replyCount+'\t'+clickCount);
    }
}
