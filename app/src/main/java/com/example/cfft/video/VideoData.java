package com.example.cfft.video;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;

import org.json.JSONArray;

import java.util.Date;

public class VideoData implements Parcelable {
    private Integer videoid;

    /**
     *
     */
    private String title;

    /**
     *
     */
    private String description;
    private Date duration;
    private String coverimage;



    /**
     *
     */
    private Integer views;

    /**
     *
     */
    private Integer likes;

    public JSONArray getComment() {
        return comment;
    }

    public void setComment(JSONArray comment) {
        this.comment = comment;
    }

    /**
     *
     */
    private Integer comments;
    private JSONArray comment;

    public VideoData(Integer id, String title, String description, String imageView) {
        this.videoid = id;
        this.title = title;
        this.description = description;
        this.coverimage = imageView;

    }

    public Integer getVideoid() {
        return videoid;
    }

    public void setVideoid(Integer videoid) {
        this.videoid = videoid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDuration() {
        return duration;
    }

    public void setDuration(Date duration) {
        this.duration = duration;
    }

    public String getCoverimage() {
        return coverimage;
    }

    public void setCoverimage(String coverimage) {
        this.coverimage = coverimage;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(videoid);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeLong(duration != null ? duration.getTime() : -1);
        dest.writeString(coverimage);
        dest.writeInt(views != null ? views : 0);
        dest.writeInt(likes != null ? likes : 0);
        dest.writeInt(comments != null ? comments : 0);

    }

    // Parcelable.Creator 实例用于反序列化对象
    public static final Creator<VideoData> CREATOR = new Creator<VideoData>() {
        @Override
        public VideoData createFromParcel(Parcel in) {
            return new VideoData(in);
        }

        @Override
        public VideoData[] newArray(int size) {
            return new VideoData[size];
        }
    };

    // 从 Parcel 中构造 VideoData 对象
    private VideoData(Parcel in) {
        videoid = in.readInt();
        title = in.readString();
        description = in.readString();
        long durationMillis = in.readLong();
        duration = durationMillis != -1 ? new Date(durationMillis) : null;
        coverimage = in.readString();
        views = in.readInt();
        likes = in.readInt();
        comments = in.readInt();
    }

}
