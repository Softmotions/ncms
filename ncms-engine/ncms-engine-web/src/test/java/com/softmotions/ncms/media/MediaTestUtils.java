package com.softmotions.ncms.media;

import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.MediaFolder;

/**
 * Created by shu on 4/22/2014.
 */
public class MediaTestUtils {


	public static MediaFile createMediaFile(int no) {
		MediaFile mf = new MediaFile();
		mf.setName("test-" + no);
		mf.setDescription("something-"+no);
		mf.setFilePath("path-"+no);
		return mf;
	}

	public static MediaFolder createMediaFolder(int no) {
		MediaFolder mf = new MediaFolder();
		mf.setName("test-" + no);
		mf.setDescription("something-" + no);
		return mf;
	}

}
