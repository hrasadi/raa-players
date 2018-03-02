package media.raa.raa_android_player.model.entities;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

/**
 * The ProgramInfo container
 * Created by hamid on 3/1/18.
 */

@SuppressWarnings("unused")
public  class ProgramInfo {
    private String title;
    private String about;
    private String thumbnail;
    private String banner;
    private String feed;

    private Bitmap bannerBitmap;
    private Bitmap thumbnailBitmap;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public Bitmap getBannerBitmap() {
        return bannerBitmap;
    }

    public void setBannerBitmap(Bitmap bannerBitmap) {
        this.bannerBitmap = bannerBitmap;
    }

    public Bitmap getThumbnailBitmap() {
        return thumbnailBitmap;
    }

    public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
        this.thumbnailBitmap = thumbnailBitmap;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static class ProgramInfos {
        private Map<String, ProgramInfo> programInfoMap = new HashMap<>();

        public Map<String, ProgramInfo> getProgramInfoMap() {
            return programInfoMap;
        }

        public void setProgramInfoMap(Map<String, ProgramInfo> programInfoMap) {
            this.programInfoMap = programInfoMap;
        }
    }
}
