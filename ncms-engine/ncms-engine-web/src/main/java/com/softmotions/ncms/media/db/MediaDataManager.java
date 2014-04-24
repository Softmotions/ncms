package com.softmotions.ncms.media.db;

import com.avaje.ebean.EbeanServer;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.MediaFolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by shu on 4/23/2014.
 */
public class MediaDataManager {

	@Inject
	EbeanServer ebean;

	public List<MediaFolder> getSubFolders(MediaFolder parent) {
		Long id = parent == null ? null : parent.getId();
		List<MediaFolder> childs = ebean.find(MediaFolder.class).where().eq("parent_id", id).findList();
		return childs;
	}

	public List<MediaFile> getFiles(MediaFolder parent) {
		return ebean.find(MediaFile.class).where().eq("media_folder_id", parent.getId()).findList();
	}

	public List<MediaFolder> getRootFolders() {
		return getSubFolders(null);
	}

	public String ident(int lev) {
		String res = "";
		for (int i = 0; i < lev; i++) res += "  ";
		return res;
	}

	public void show(PrintWriter pw, MediaFolder folder, int lev) {
		List<MediaFolder> childs = getSubFolders(folder);
		List<MediaFile> files = folder == null? new ArrayList<MediaFile>() : getFiles(folder);
		if (childs.isEmpty() && files.isEmpty()) {
			if (folder != null) {
				pw.println(ident(lev) + "Folder: " + folder.getName() + "[" + folder.getId() + "] {}");
			}
		} else {
			int sublev = lev;
			if (folder != null) {
				pw.println(ident(lev) + "Folder: " + folder.getName() + "[" + folder.getId() + "] {");
				sublev = lev + 1;
			}
			for (MediaFolder f : childs) {
				show(pw, f, sublev);
			}
			for(MediaFile file: files) {
				pw.println(ident(lev+1) + "- " + file.getName() + "[" + file.getId() + "] {");
			}
			if (folder != null) pw.println(ident(lev) + "}");
		}
	}

	public void show(Writer w, MediaFolder folder, int lev) {
		PrintWriter pw = new PrintWriter(w);
		show(pw, folder, lev);
		pw.close();
	}

	public String dump(MediaFolder folder, int lev) {
		StringWriter sw = new StringWriter();
		show(sw, null, 0);
		return sw.toString();
	}

	public void delete(MediaFolder folder) {
		for(MediaFolder f: getSubFolders(folder)) {
			delete(f);
		}
		ebean.delete(folder);
	}

}
