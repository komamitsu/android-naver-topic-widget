package com.komamitsu.android.naver.topic;

import android.graphics.Bitmap;

public class Topic {
  private int rank;
  private String title = "";
  private String detail = "";
  private String time = "";
  private String urlOfImage = "";
  private String urlOfLink = "";
  private Bitmap image;

  /**
   * @param rank
   *          the rank to set
   */
  public void setRank(int rank) {
    this.rank = rank;
  }

  /**
   * @param title
   *          the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @param detail
   *          the detail to set
   */
  public void setDetail(String detail) {
    this.detail = detail;
  }

  /**
   * @param time
   *          the time to set
   */
  public void setTime(String time) {
    this.time = time;
  }

  /**
   * @param urlOfImage
   *          the urlOfImage to set
   */
  public void setUrlOfImage(String urlOfImage) {
    this.urlOfImage = urlOfImage;
  }

  /**
   * @param urlOfLink
   *          the urlOfLink to set
   */
  public void setUrlOfLink(String urlOfLink) {
    this.urlOfLink = urlOfLink;
  }

  /**
   * @return the rank
   */
  public int getRank() {
    return rank;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @return the detail
   */
  public String getDetail() {
    return detail;
  }

  /**
   * @return the time
   */
  public String getTime() {
    return time;
  }

  /**
   * @return the urlOfImage
   */
  public String getUrlOfImage() {
    return urlOfImage;
  }

  /**
   * @return the urlOfLink
   */
  public String getUrlOfLink() {
    return urlOfLink;
  }

  /**
   * @return the image
   */
  public Bitmap getImage() {
    return image;
  }

  /**
   * @param image
   *          the image to set
   */
  public void setImage(Bitmap image) {
    this.image = image;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "rank=" + rank + ", title=" + title + ", detail=" + detail + ", time=" + time + ", urlOfImage=" + urlOfImage
        + ", urlOfLink=" + urlOfLink;
  }
}
