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
            String updateSql = "update "+tableName_Topic+" set "+columnName_Topic_state+" = ? " +
                    "where "+columnName_Topic_id+" = ?";
            PreparedStatement updateInitiateTime =connection.prepareStatement(updateSql);
            String insertSql = "replace INTO "+tableName_Reply+" ("
                    +columnName_Reply_id+","
                    +columnName_Reply_floor+","
                    +columnName_Reply_replytime
                    +") VALUES(?,?,?)";
            PreparedStatement replaceReply=connection.prepareStatement(insertSql);
            String basicUrl = "http://bbs.tianya.cn/post-%s-%s-%d.shtml";
            while (GlobalUtil.fetching && rs.next()){
                String id = rs.getString(columnName_Topic_id);
                int viewCount = rs.getInt(columnName_Topic_replycount);
                int pageCount = viewCount/100 + 1;
                boolean deleteID = false;
                for (int i = 1;i<=pageCount;i++){
                    logger.info("访问【" + id + "】!");
                    String url = String.format(basicUrl,item,id,i);

                    String html = HttpClientUtil.getHtmlByUrl(url);
                    if (html==null){
                        deleteID = true;
                        break;
                    }else {
                        Document doc = Jsoup.parse(html);
                        String htmlTitle = doc.title();
                        if (htmlTitle.isEmpty() || htmlTitle.equalsIgnoreCase("出错了_天涯社区")){
                            deleteID = true;
                            break;
                        }else {
                            Elements replies = doc.select("div.atl-head-reply");
                            for (Element replyTemp : replies) {
                                Elements hrefs = replyTemp.select("a[href]"); //带有href属性的a元素
                                String replyTime = null;
                                String floor = null;
                                try {
                                    replyTime = hrefs.first().attr("replytime");
                                    floor = hrefs.last().attr("floor");
                                    if (!(replyTime == null || floor == null || replyTime.isEmpty() || floor.isEmpty())){
//                                    if (replyTime!=null && floor !=null ){
                                        //todo
                                        replaceReply.setString(1,id);
                                        replaceReply.setString(2,floor);
                                        replaceReply.setString(3,replyTime);
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
                    updateInitiateTime.setInt(1, 1);
                    updateInitiateTime.setString(2, id);
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
        String querySql = "SELECT "+columnName_Topic_id+","
                +columnName_Topic_replycount+
                " FROM "+tableName_Topic+" WHERE "
                +columnName_Topic_state+" IS NULL ORDER BY " + columnName_Topic_replycount;
        PreparedStatement psUpdate=connection.prepareStatement(querySql);
        ResultSet rs = psUpdate.executeQuery();
        return rs;
    }
}