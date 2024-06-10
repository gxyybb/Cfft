package com.example.cfft;

public class UrlConstants {
    public static final String BASE_URL = "http://101.200.79.152:8080";

    // 用户相关接口
    public static final String USER_FIRST_URL = BASE_URL + "/user/first";

    // 轮播图接口
    public static final String CAROUSEL_ALL_URL = BASE_URL + "/carousel/all";

    // 帖子搜索接口
    public static final String POST_SEARCH_URL = BASE_URL + "/post/search";

    // 帖子列表接口
    public static final String POST_LIST_URL = BASE_URL + "/post/list";
    // 获取帖子详情接口
    public static final String POST_DETAIL_URL = BASE_URL + "/post?postId=";

    // 获取评论列表接口
    public static final String COMMENT_LIST_URL = BASE_URL + "/comment/comment/";
    public static final String VIDEO_COMMENT_LIST_URL = BASE_URL + "/comment/comment/";

    // 发送评论接口
    public static final String SEND_COMMENT_URL = BASE_URL + "/comment";

    public static final String SEND_VIDEO_COMMENT_URL = BASE_URL + "/video/comment";
}

