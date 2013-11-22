package fetchtopic;

import global.GlobalConstant;
import global.GlobalUtil;
import global.HttpClientUtil;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: ASUS
 * Date: 13-11-20
 * Time: 下午2:07
 * To change this template use File | Settings | File Templates.
 */
public class FetchBoardHtmlThread implements Runnable, GlobalConstant {
    Logger logger;
    String item;

    public FetchBoardHtmlThread(String item) {
        this.item = item;
    }
    @Override
    public void run() {
        logger = Logger.getLogger("FetchBoardHtmlThread");
        while (GlobalUtil.fetching){
            String url = GlobalUtil.urlQ.poll();
            if (url== null){
                try {
                    Thread.sleep(sleep_short);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                logger.info(url);
                String html = HttpClientUtil.getHtmlByUrl(url);
                if (html!=null) {
                    try {
                        GlobalUtil.htmlQ.put(html);
                    } catch (InterruptedException e) {
                        logger.error(e);
                    }
                }
            }

        }
    }
}
