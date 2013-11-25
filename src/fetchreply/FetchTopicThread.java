package fetchreply;

import global.GlobalConstant;
import global.GlobalUtil;
import global.HttpClientUtil;
import global.PostStruct;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
    public FetchTopicThread(String item,String startURL) {
        this.item = item;
        this.startURL = startURL;
        logger = Logger.getLogger("FetchTopicThread");
    }

    @Override
    public void run() {
        String insertSql = "REPLACE INTO "+tableName_Topic+" ("
                +columnName_Topic_id+","
                +columnName_Topic_title+","
                +columnName_Topic_clickcount+","
                +columnName_Topic_replycount
                +") VALUES(?,?,?,?)";
        PreparedStatement ps = null;
        Connection connection = GlobalUtil.getConnToDatabase(item);
        if (connection == null){
            return;
        }
        try {
            String nextURL =  this.startURL;
            ps =connection.prepareStatement(insertSql);
            while (GlobalUtil.fetching){
                String html = HttpClientUtil.getHtmlByUrl(nextURL);
                System.out.println(nextURL);
                if (html != null){
                    Document doc = Jsoup.parse(html);
                    fetchTopic(doc);
                    System.out.println("------------------------");
                    nextURL =  getNextURl(doc);
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
//                        String before = "/post-free-3722617-1.shtml";
                        int lastIndex = url.lastIndexOf("-1.shtml");
                        String id = url.substring(7+this.item.length(),lastIndex);
                        deal(id, title, replyCount, clickCount);
                    }
                }
            }
        }
    }

    private void deal(String id, String title, int replyCount, int clickCount) {
//        System.out.println(id+'\t'+title+'\t'+replyCount+'\t'+clickCount);
    }
}