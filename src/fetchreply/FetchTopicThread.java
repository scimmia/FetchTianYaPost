package fetchreply;

import global.*;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: ASUS
 * Date: 13-11-25
 * Time: 上午11:27
 * To change this template use File | Settings | File Templates.
 */
public class FetchTopicThread implements Runnable, GlobalConstant {
    Logger logger;
    String item;
    String startURL;
    HashSet<TopicStruct> set;
    public FetchTopicThread(String item,String startURL) {
        this.item = item;
        this.startURL = startURL;
        set = new HashSet<TopicStruct>();
        logger = Logger.getLogger("FetchTopicThread");
    }

    @Override
    public void run() {
        String insertSql = "REPLACE INTO "+tableName_Topic+" ("
                +columnName_Topic_id+","
                +columnName_Topic_title+","
                +columnName_Topic_clickcount+","
                +columnName_Topic_replycount+","
                +columnName_Topic_idForReply
                +") VALUES(?,?,?,?,?)";
        PreparedStatement ps = null;
        Connection connection = GlobalUtil.getConnToDatabase(item);
        if (connection == null){
            return;
        }
        try {
            ps =connection.prepareStatement(insertSql);
            String nextURL =  this.startURL;
            while (GlobalUtil.fetching && nextURL!=null){
                String html = HttpClientUtil.getHtmlByUrl(nextURL,3);
                System.out.println(nextURL);
                if (html != null){
                    set.clear();
                    Document doc = Jsoup.parse(html);
                    fetchTopic(doc);
                    System.out.println("------------------------");
                    nextURL =  getNextURl(doc);

                    for (TopicStruct temp:set){
                        ps.setString(1,temp.getId());
                        ps.setString(2,temp.getTitle());
                        ps.setInt(3, temp.getClickCount());
                        ps.setInt(4, temp.getReplyCount());
                        ps.setString(5,temp.getIdForReply());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    connection.commit();
                }
            }
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
        logger.info(String.format("抓取 %s 结束",item));
    }

    private String getNextURl(Document doc) {
        String result = null;
        Element link = doc.select("div.links").first();
        Elements linksHref = link.select("a[href]");
        for (Element linkHref : linksHref){
            if (linkHref.text().equals("下一页")){
                result = boardHeader + linkHref.attr("href");
                break;
            }
        }
        return result;
    }

    public void fetchTopic(Document doc){
        Element element = doc.select("div.mt5").last();
        Elements trs = element.select("tr");
        for (Element tr : trs){
            Elements tds = tr.select("td");
            if (tds.size() == 5){
                int replyCount = Integer.parseInt(tds.get(3).text());
                if (replyCount < minReplyCount){
                    continue;
                }else{
                    int clickCount = Integer.parseInt(tds.get(2).text());
                    if (clickCount/replyCount < minReplyProportion){
                        continue;
                    }else {
                        Element href = tds.get(0).select("a[href]").first();
                        String title = href.text();
                        String url = href.attr("href");

                        String idForReply = "http://bbs.tianya.cn"+url.replace("-1.shtml","-%d.shtml");
                        set.add(new TopicStruct(url,title,replyCount,clickCount,idForReply));
//                        String before = "/post-free-3722617-1.shtml";
//                        int lastIndex = url.lastIndexOf("-1.shtml");
//                        String id = url.substring(7+this.item.length(),lastIndex);
//                        deal(url, title, replyCount, clickCount);
                    }
                }
            }
        }
    }

    private void deal(String id, String title, int replyCount, int clickCount) {
//        System.out.println(id+'\t'+title+'\t'+replyCount+'\t'+clickCount);
    }


    private void saveData() {

    }

}