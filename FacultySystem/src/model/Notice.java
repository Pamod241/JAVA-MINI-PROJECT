package model;

import java.time.LocalDateTime;

public class Notice {
    private String        noticeId;
    private String        title;
    private String        content;
    private String        createdBy; // Admin_id
    private LocalDateTime createdAt;

    public Notice(String noticeId, String title, String content,
                  String createdBy, LocalDateTime createdAt) {
        this.noticeId  = noticeId;
        this.title     = title;
        this.content   = content;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String        getNoticeId()  { return noticeId; }
    public String        getTitle()     { return title; }
    public String        getContent()   { return content; }
    public String        getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setTitle(String t)      { title = t; }
    public void setContent(String c)    { content = c; }
}
