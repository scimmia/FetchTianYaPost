package global;

/**
 * Created with IntelliJ IDEA.
 * User: ASUS
 * Date: 13-11-22
 * Time: 下午1:35
 * To change this template use File | Settings | File Templates.
 */
public interface GlobalConstant {
    //日志
    String logFolderPatch = "e://logs/";
    //数据库
    String driver = "com.mysql.jdbc.Driver";
    String user = "root";
    String password = "1234";

    String preDatabaseUrl = "jdbc:mysql://127.0.0.1:3306/";
    String endDatabaseUrl = "?useUnicode=true&characterEncoding=UTF-8";
    String datebaseHeader = "tianya_";

    String tableName_Topic = "topic";
    String columnName_Topic_id = "id";
    String columnName_Topic_title = "title";
    String columnName_Topic_clickcount = "clickcount";
    String columnName_Topic_replycount = "replycount";
    String columnName_Topic_state = "state";

    String tableName_Reply = "reply";
    String columnName_Reply_id = "id";
    String columnName_Reply_floor = "floor";
    String columnName_Reply_replytime = "replytime";

    //
    int CONNECTION_TIMEOUT = 15000;
    int sleep_short = 300;
    int sleep_long = 1000;
    //版块
    String[] items = {"funinfo","free","no11","worldlook","feeling"};
    String[] itemsName = {"娱乐八卦","天涯杂谈","时尚资讯","国际观察","情感天地"};

    //主题
    int minReplyCount = 500;
    int minReplyProportion = 8;
    int saveEveryCount = 20;

    //回复
    int saveReplyEveryCount = 2000;
    String vu = "84853088886";
}
