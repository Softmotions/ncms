package com.softmotions.ncms.mhttl;

import java.io.Serializable;

/**
 * Social links block object
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 * @version $Id$
 */

public class SocialLinks implements Serializable {

    public static final String PREFIX_FACEBOOK = "https://www.facebook.com/";
    public static final String PREFIX_TWITTER = "https://www.twitter.com/";
    public static final String PREFIX_VKONTAKTE = "https://www.vk.com/";
    protected String facebook;
    protected String twitter;
    protected String vkontakte;

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getVkontakte() {
        return vkontakte;
    }

    public void setVkontakte(String vkontakte) {
        this.vkontakte = vkontakte;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String toHtml() {
        final StringBuilder sb = new StringBuilder("");
        if (facebook != null) {
            sb.append("<div><a href=\"").append(PREFIX_FACEBOOK).append(facebook).append("\">facebook</a></div>");
        }
        if (twitter != null) {
            sb.append("<div><a href=\"").append(PREFIX_TWITTER).append(twitter).append("\">twitter</a></div>");
        }
        if (vkontakte != null) {
           sb.append("<div><a href=\"").append(PREFIX_VKONTAKTE).append(vkontakte).append("\">vkontakte</a></div>");
        }
        return sb.toString();
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        final StringBuilder sb = new StringBuilder("SocialLinks{");
        sb.append("facebook=").append(facebook);
        sb.append(", twitter=").append(twitter);
        sb.append(", vkontakte=").append(vkontakte);
        sb.append('}');
        return sb.toString();
    }
}
