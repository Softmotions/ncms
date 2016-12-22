package com.softmotions.ncms.asm.am;

import java.util.Map;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.media.MediaResource;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public abstract class AsmFileAttributeManagerSupport extends AsmAttributeManagerSupport {

    @Nullable
    public static String getFileDescription(@Nullable String location) {
        if (location == null) {
            return null;
        }
        int ind = location.indexOf('|');
        if (ind == -1 || ind == location.length() - 1) {
            return null;
        }
        return location.substring(ind + 1).trim();
    }

    @Nullable
    public static String getRawLocation(@Nullable String location) {
        if (location == null) {
            return null;
        }
        int ind = location.indexOf('|');
        if (ind != -1) {
            return location.substring(0, ind).trim();
        } else {
            return location.trim();
        }
    }

    /**
     * Returns location of new cloned file or `null`.
     */
    @Nullable
    public static String translateClonedFile(MediaReader reader,
                                             @Nullable String location,
                                             Map<Long, Long> fmap) {
        if (StringUtils.isBlank(location)) {
            return null;
        }
        location = getRawLocation(location);
        //noinspection ConstantConditions
        MediaResource res = reader.findMediaResource(location, null);
        if (res == null) {
            return null;
        }
        Long tid = fmap.get(res.getId());
        if (tid == null) {
            return null;
        }
        MediaResource nres = reader.findMediaResource(tid, null);
        if (nres == null) {
            return null;
        }
        String nlocation = nres.getName();
        String fdesk = getFileDescription(location);
        if (fdesk != null) {
            nlocation += ('|' + fdesk);
        }
        return nlocation;
    }

    /**
     * Returns id of a new cloned file of `null`.
     */
    @Nullable
    public static Long translateClonedFile(Long fid,
                                           Map<Long, Long> fmap) {
        if (fid == null) {
            return null;
        }
        Long tid = fmap.get(fid);
        if (tid == null) {
            return null;
        }
        return tid;
    }
}
