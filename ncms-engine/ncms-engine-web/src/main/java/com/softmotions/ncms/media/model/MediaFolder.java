package com.softmotions.ncms.media.model;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: shu
 * Date: 4/12/14
 * Time: 5:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class MediaFolder {

  Long id;

  String name;
  String description;
  List<String> tags;

  List<MediaFolder> folders;
  List<MediaFile> files;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public List<MediaFolder> getFolders() {
    return folders;
  }

  public void setFolders(List<MediaFolder> folders) {
    this.folders = folders;
  }

  public List<MediaFile> getFiles() {
    return files;
  }

  public void setFiles(List<MediaFile> files) {
    this.files = files;
  }

}
