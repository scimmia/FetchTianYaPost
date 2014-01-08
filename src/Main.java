import fetchreply.FetchReplyThread;
import fetchreply.FetchTopicThread;
import fetchtopic.FetchBoardHtmlThread;
import fetchtopic.ParseBoardHtmlThread;
import fetchtopic.SaveTopicThread;
import global.GlobalConstant;
import global.GlobalUtil;
import global.SavitchIn;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        runIt();
    }

    public static void runIt(){
        System.out.println("请输入抓取类型序号: " +
                "1 抓主贴 2 抓详情 3 初始化数据库 " +
                "4 抓搜索主贴 5 抓搜索回复" +
                "0 退出 \n抓取开始后输入1回车可结束");
        int selectIndex = SavitchIn.readInt();
        switch (selectIndex){
            case 1:
            {
                fetchTopic1125();
                break;
            }
            case 2:
            {
                fetchReply();
                break;
            }
            case 3:
            {
                if (GlobalUtil.initDataBase()){
                    System.out.println("初始化数据库成功，继续选择操作");
                }
                runIt();
                break;
            }
            case 4:
            {
                fetchSearchTop();
                break;
            }
            case 5:
            {
                fetchSearchReply();
                break;
            }
            case 0:
            {
                break;
            }
            default:
            {
                System.out.println("重选");
                runIt();
                break;
            }
        }
    }

    public static void fetchTopic(){
        String item = GlobalUtil.getSelectedItem();
        System.out.println("请输入上次运行的网址，直接回车则从30天前开始");
        String url = SavitchIn.readLine();
        if (url.isEmpty()){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE,-30);
            url = "list.jsp?item=free&p=1&vu=84924361963&n="+
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(calendar.getTime());
        }
        try {
            GlobalUtil.urlQ.put(url);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GlobalUtil.initLog4j("Fetching-"+item);
        new Thread(new FetchBoardHtmlThread(item)).start();
        new Thread(new ParseBoardHtmlThread(item)).start();
        new Thread(new SaveTopicThread(item)).start();
        waitForExit();
    }
    public static void fetchReply(){
        String item = GlobalUtil.getSelectedItem();
        GlobalUtil.initLog4j("FetchReply-"+item);
        new Thread(new FetchReplyThread(item)).start();
        waitForExit();
    }
    public static void waitForExit(){
        int m = SavitchIn.readInt();
        if (m==1){
            GlobalUtil.fetching = false;
        }
    }
    public static void fetchTopic1125(){
        String item = GlobalUtil.getSelectedItem();
        System.out.println("请输入开始id");
        String id = SavitchIn.readLine();

        GlobalUtil.initLog4j("Fetching-" + item);
        new Thread(new FetchTopicThread(item,id)).start();
        waitForExit();
    }

    public static void fetchSearchTop(){
        int choise = GlobalUtil.makeDecision(GlobalConstant.tags);
        String item = GlobalConstant.tags[choise];
        String[] tagOptions = GlobalUtil.getTagOptions(GlobalConstant.tags[choise]);
        int choiseB = GlobalUtil.makeDecision(tagOptions);
        String tag = tagOptions[choiseB];

//        System.out.println("请输入开始id,回车结束");
//        String tag = SavitchIn.readLineWord();
        System.out.println("请输入开始page,回车结束");
        int startPage = SavitchIn.readInt();
        GlobalUtil.initLog4j("Fetching-" +item);
        System.out.println(tag);
        new Thread(new versiona.FetchTopicThread(item,tag,startPage)).start();
        waitForExit();
    }

    private static void fetchSearchReply() {
        int choise = GlobalUtil.makeDecision(GlobalConstant.tags);
        String item = GlobalConstant.tags[choise];
        GlobalUtil.initLog4j("FetchingReply-" +item);
        new Thread(new versiona.FetchReplyThread(item)).start();
        waitForExit();
    }


}
