package com.softmotions.ncms.mhttl;

import java.io.Serializable;

/**
 * @author Motyrev Pavel (legioner.r@gmail.com)
 * @version $Id$
 */

public class SocialLinks implements Serializable {

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

    public String toString() {
        final StringBuilder sb = new StringBuilder("SocialLinks{");
        sb.append("facebook=").append(facebook);
        sb.append(", twitter=").append(twitter);
        sb.append(", vkontakte=").append(vkontakte);
        sb.append('}');
        return sb.toString();
    }
}
