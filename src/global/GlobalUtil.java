package global;

import org.apache.log4j.PropertyConfigurator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: ASUS
 * Date: 13-11-20
 * Time: 下午2:22
 * To change this template use File | Settings | File Templates.
 */
public class GlobalUtil implements GlobalConstant {
    private GlobalUtil() {
    }
    public static String getSelectedItem(){
        System.out.print("\n请输入抓取板块序号:");
        for (int i =0;i<itemsName.length;i++){
            System.out.printf("  %d. %s",i,itemsName[i]);
        }
        int selected = SavitchIn.readInt();
        if (selected<0||selected>=itemsName.length){
            System.out.print("\n请输入正确的抓取板块序号:");
            return getSelectedItem();
        }
        return items[selected];
    }
    public static void initLog4j(String boardName){
        Properties prop = new Properties();
        prop.setProperty("log4j.rootLogger", "INFO, ServerDailyRollingFile, stdout");
        prop.setProperty("log4j.appender.ServerDailyRollingFile", "org.apache.log4j.DailyRollingFileAppender");
        prop.setProperty("log4j.appender.ServerDailyRollingFile.DatePattern", "'.'yyyy-MM-dd");
        prop.setProperty("log4j.appender.ServerDailyRollingFile.File", logFolderPatch+boardName+".log");
        prop.setProperty("log4j.appender.ServerDailyRollingFile.layout", "org.apache.log4j.PatternLayout");
        prop.setProperty("log4j.appender.ServerDailyRollingFile.layout.ConversionPattern", "%d [%c]-[%p] %m%n");
        prop.setProperty("log4j.appender.ServerDailyRollingFile.Append", "true");

        prop.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        prop.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        prop.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%d{yyyy-MM-dd HH:mm:ss} %p [%c] %m%n");

        PropertyConfigurator.configure(prop);
    }
    public static boolean fetching = true;
    public static LinkedBlockingQueue<String> urlQ = new LinkedBlockingQueue<String>();
    public static LinkedBlockingQueue<String> htmlQ = new LinkedBlockingQueue<String>();

    public static LinkedBlockingQueue<PostStruct> postQ = new LinkedBlockingQueue<PostStruct>();

    public static boolean initDataBase() {
        boolean result = false;
        Connection conn =null;
        Statement s = null;
        try {
            Class.forName(driver);
            String createDatebaseurl = preDatabaseUrl + endDatabaseUrl;
            conn = DriverManager.getConnection(createDatebaseurl, user, password);
            if(!conn.isClosed())
                System.out.println("Succeeded connecting to the Database");
            s = conn.createStatement();
            for (String item : items){
                String tableName = datebaseHeader+item;
                s.addBatch("CREATE DATABASE IF NOT EXISTS "+tableName+
                        " default character set utf8");
                s.addBatch("Create table IF NOT EXISTS " + tableName + "."+tableName_Topic+"(" +
                        columnName_Topic_id+" varchar(40) primary key," +
                        columnName_Topic_title+" varchar(80)," +
                        columnName_Topic_clickcount+" int," +
                        columnName_Topic_replycount+" int," +
                        columnName_Topic_state+" int" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
                s.addBatch("Create table IF NOT EXISTS "+tableName+"."+tableName_Reply+"(" +
                        columnName_Reply_id+" varchar(40)," +
                        columnName_Reply_floor+" int," +
                        columnName_Reply_replytime+" datetime," +
                        "primary key ("+columnName_Reply_id+","+columnName_Reply_floor+")" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
            }
            s.executeBatch();
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                if (s!=null){
                    s.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Connection getConnToDatabase(String itemName){
        Connection conn = null;
        try {
            Class.forName(driver);
            String url = preDatabaseUrl+datebaseHeader+itemName+endDatabaseUrl;
            conn = DriverManager.getConnection(url, user, password);
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
