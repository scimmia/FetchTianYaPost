package versiona;

import global.GlobalConstant;
import global.GlobalUtil;
import global.HttpClientUtil;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ASUS on 14-1-8.
 */
public class FetchReplyThread implements Runnable, GlobalConstant {
    Logger logger;
    String item;
    HttpClient httpClient;
    public FetchReplyThread(String item) {
        this.item = item;
        logger = Logger.getLogger("versiona.FetchReplyThread");
    }

    @Override
    public void run() {
        Connection connection = GlobalUtil.getConnToDatabase(item);
        httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        try {
            ResultSet rs = selectUnfetched(connection);

            String deleteSql = "delete from "+tableName_Topic+" where "+columnName_Topic_id+" = ?";
            PreparedStatement deleteWrongId =connection.prepareStatement(deleteSql);
            String updateSql = "update "+tableName_Topic+" set "
                    +columnName_Topic_clickcount+" = ? ,"
                    +columnName_Topic_replycount+" = ? ,"
                    +ColumnName_Topic_initiateTime+" = ? ,"
                    +columnName_Topic_state+" = ? " +
                    "where "+columnName_Topic_id+" = ?";
            PreparedStatement updateInitiateTime =connection.prepareStatement(updateSql);
            String insertSql = "insert INTO "+tableName_Reply+" ("
                    +columnName_Reply_floor+","
                    +columnName_Reply_replytime
                    +") VALUES(?,?)";
            PreparedStatement replaceReply=connection.prepareStatement(insertSql);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            while (GlobalUtil.fetching && rs.next()){
                String htmlID = rs.getString(columnName_Topic_idForReply);
                String id = rs.getString(columnName_Topic_id);
//                String id = (String.format(oldid,1)).replace("http://bbs.tianya.cn","");
                boolean deleteID = false;
                int pageCount = 1;
                //deal first page
                String firstHtml = HttpClientUtil.getHtmlByUrl(String.format(htmlID,1),3);
                if (firstHtml == null){
                    deleteID = true;
                }else {
                    Document doc = Jsoup.parse(firstHtml);
                    String htmlTitle = doc.title();
                    if (htmlTitle.isEmpty() || htmlTitle.equalsIgnoreCase("出错了_天涯社区")){
                        deleteID = true;
                    }else {
                        Element allPages = doc.select("div.atl-pages").first();
                        if (allPages != null){
                            Elements hrefs = allPages.select("a[href]");
                            if (hrefs!=null){
                                for (Element href : hrefs){
                                    String urlTemp = href.attr("href");
                                    int startTemp = urlTemp.lastIndexOf('-');
                                    int currentPage = Integer.parseInt(urlTemp.substring(startTemp+1,urlTemp.length()-6));
                                    if (currentPage > pageCount){
                                        pageCount = currentPage;
                                    }
                                }
                                System.out.println("pageCount:"+pageCount);
                            }
                        }
                        Element atlMenu = doc.select("div.atl-menu").first();
                        if (atlMenu != null){
                            int clickCount = Integer.parseInt(atlMenu.attr("js_clickcount"));
                            int replyCount = Integer.parseInt(atlMenu.attr("js_replycount"));
                            long posttime = Long.parseLong(atlMenu.attr("js_posttime"));
                            String initiateTime = formatter.format(new Date(posttime));
                            System.out.println("initiateTime:"+initiateTime);

//                            updateInitiateTime.setString(1, id);
                            updateInitiateTime.setInt(1, clickCount);
                            updateInitiateTime.setInt(2, replyCount);
                            updateInitiateTime.setString(3, initiateTime);
                            updateInitiateTime.setInt(4, 1);
                            updateInitiateTime.setString(5, id);
                        }
                    }
                }

                for (int i = 1;i<=pageCount;i++){
                    logger.info("访问【" + id + "】!");
                    String url = String.format(htmlID,i);

                    String html = HttpClientUtil.getHtmlByUrl(url);
                    if (html!=null){
                        Document doc = Jsoup.parse(html);
                        String htmlTitle = doc.title();
                        if (htmlTitle.isEmpty() || htmlTitle.equalsIgnoreCase("出错了_天涯社区")){
                            continue;
                        }else {
                            Elements replies = doc.select("div.atl-head-reply");
                            for (Element replyTemp : replies) {
                                Elements hrefs = replyTemp.select("a[href]"); //带有href属性的a元素
                                String replyTime = null;
                                try {
                                    replyTime = hrefs.first().attr("replytime");
                                    if (replyTime != null){
                                        replaceReply.setString(1,id);
                                        replaceReply.setString(2,replyTime);
                                        replaceReply.addBatch();
                                    }
                                }catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                if (deleteID){
                    replaceReply.clearBatch();
                    deleteWrongId.setString(1, id);
                    deleteWrongId.executeUpdate();
                }else {
                    //todo update
                    replaceReply.executeBatch();
                    updateInitiateTime.executeUpdate();
                }
                logger.info("save【"+id+"】!");
                connection.commit();
            }
            rs.close();
        } catch(SQLException e) {
            logger.error(e);
        } catch(Exception e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    public ResultSet selectUnfetched(Connection connection) throws SQLException {
        String querySql = "SELECT "+columnName_Topic_id+
                ","+columnName_Topic_idForReply+
                " FROM "+tableName_Topic+" WHERE "
                +columnName_Topic_state+" IS NULL ORDER BY " + columnName_Topic_replycount;
        PreparedStatement psUpdate=connection.prepareStatement(querySql);
        ResultSet rs = psUpdate.executeQuery();
        return rs;
    }


}