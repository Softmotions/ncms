package com.softmotions.ncms.media.model;

/**
 * Created by shu on 4/17/2014.
 */
import java.util.List;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.ibatis.session.SqlSession;

@Singleton
public class MediaFileDao {

  private SqlSession session;

  @Inject
  public MediaFileDao(SqlSession session){
    this.session = session;
  }

  public List<MediaFile> selectAll(){
    return session.selectList("com.softmotions.ncms.MediaFile.getAll");
  }

  public MediaFile getById(long id){
    return session.selectOne("com.softmotions.ncms.MediaFile.getById", id);
  }

  public void deleteById(long id){
    session.delete("com.softmotions.ncms.MediaFile.deleteById", id);
  }

  public void deleteAll(){
    session.delete("com.softmotions.ncms.MediaFile.deleteAll");
  }

  public void insert(MediaFile mediaFile){
    session.insert("com.softmotions.ncms.MediaFile.insert", mediaFile);
  }

  public void update(MediaFile mediaFile){
    session.update("com.softmotions.ncms.MediaFile.update", mediaFile);
    //session.commit();
  }
}