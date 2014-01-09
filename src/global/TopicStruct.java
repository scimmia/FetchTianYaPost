package global;

/**
 * Created by ASUS on 14-1-7.
 */
public class TopicStruct {
    String id;
    String title;
    String topicType;
    int replyCount;
    int clickCount;
    String idForReply;
    public TopicStruct() {
    }

    public TopicStruct(String id, String title, String topicType, int replyCount,String idForReply) {
        this.id = id;
        this.title = title;
        this.topicType = topicType;
        this.replyCount = replyCount;
        this.idForReply = idForReply;
    }

    public String getIdForReply() {
        return idForReply;
    }

    public void setIdForReply(String idForReply) {
        this.idForReply = idForReply;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopicType() {
        return topicType;
    }

    public void setTopicType(String topicType) {
        this.topicType = topicType;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    @Override
    public boolean equals(Object st){
        TopicStruct temp = (TopicStruct) st;
        return id.equals(temp.getId());
    }
}
