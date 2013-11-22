package fetchtopic;

import global.GlobalConstant;
import global.PostStruct;
import global.GlobalUtil;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 * User: ASUS
 * Date: 13-11-20
 * Time: 下午4:58
 * To change this template use File | Settings | File Templates.
 */
public class SaveTopicThread implements Runnable, GlobalConstant {
    Logger logger;
    String item;
    public SaveTopicThread(String item) {
        this.item = item;
    }
    @Override
    public void run() {
        logger = Logger.getLogger("SaveTopicThread");
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
            ps =connection.prepareStatement(insertSql);
            int i = 0;
            PostStruct postStruct;
            while (!GlobalUtil.postQ.isEmpty() || GlobalUtil.fetching){
                postStruct = GlobalUtil.postQ.poll();
                if (postStruct == null){
                    try {
                        Thread.sleep(sleep_long);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {
                    if (connection!=null && ps!=null){
                        ps.setString(1,postStruct.getId());
                        ps.setString(2,postStruct.getTitle());
                        ps.setInt(3,postStruct.getClickCount());
                        ps.setInt(4,postStruct.getReplyCount());
                        ps.execute();
                        i++;
                        if (i==saveEveryCount){
                            connection.commit();
                            i = 0;
                        }
                    }
                }
            }
            if (i>0){
                connection.commit();
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
}
