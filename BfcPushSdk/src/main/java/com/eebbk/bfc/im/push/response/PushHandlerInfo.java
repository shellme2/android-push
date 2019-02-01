package com.eebbk.bfc.im.push.response;

public class PushHandlerInfo {

    private String alias;

    private String tags;

    private String pkgName;

    private int order;

    private int pageSize;

    private long syncKey;

    public PushHandlerInfo() {
    }

    public PushHandlerInfo(String alias, String pkgName, int order, int pageSize, long syncKey) {
        this.alias = alias;
        this.pkgName = pkgName;
        this.order = order;
        this.pageSize = pageSize;
        this.syncKey = syncKey;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getSyncKey() {
        return syncKey;
    }

    public void setSyncKey(long syncKey) {
        this.syncKey = syncKey;
    }


    @Override
    public String toString() {
        return "PushHandlerInfo{" +
                "alias='" + alias + '\'' +
                ", tags='" + tags + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", order=" + order +
                ", pageSize=" + pageSize +
                ", syncKey=" + syncKey +
                '}';
    }
}
