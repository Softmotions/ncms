package com.softmotions.ncms.mhttl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.softmotions.commons.cont.CollectionUtils;

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
    protected Boolean buttonFacebook;
    protected Boolean buttonTwitter;
    protected Boolean buttonVkontakte;
    protected Boolean buttonOdnoklassniki;

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

    public Boolean getButtonFacebook() {
        return buttonFacebook;
    }

    public void setButtonFacebook(Boolean buttonFacebook) {
        this.buttonFacebook = buttonFacebook;
    }

    public Boolean getButtonTwitter() {
        return buttonTwitter;
    }

    public void setButtonTwitter(Boolean buttonTwitter) {
        this.buttonTwitter = buttonTwitter;
    }

    public Boolean getButtonVkontakte() {
        return buttonVkontakte;
    }

    public void setButtonVkontakte(Boolean buttonVkontakte) {
        this.buttonVkontakte = buttonVkontakte;
    }

    public Boolean getButtonOdnoklassniki() {
        return buttonOdnoklassniki;
    }

    public void setButtonOdnoklassniki(Boolean buttonOdnoklassniki) {
        this.buttonOdnoklassniki = buttonOdnoklassniki;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String toHtml() {
        final StringBuilder sb = new StringBuilder("<ul class=\"social-networks\">");
        if (facebook != null) {
            sb.append("<li><a class=\"facebook\" href=\"").append(PREFIX_FACEBOOK).append(facebook).append("\">facebook</a></li>");
        }
        if (twitter != null) {
            sb.append("<li><a class=\"twitter\" href=\"").append(PREFIX_TWITTER).append(twitter).append("\">twitter</a></li>");
        }
        if (vkontakte != null) {
            sb.append("<li><a class=\"vkontakte\" href=\"").append(PREFIX_VKONTAKTE).append(vkontakte).append("\">vkontakte</a></li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String shareButtons() {
        final StringBuilder sb = new StringBuilder("<div class=\"ya-share2\" data-services=\"");
        Set<String> buttons = new HashSet<>();
        if (buttonFacebook) {
            buttons.add("facebook");
        }
        if (buttonTwitter) {
            buttons.add("twitter");
        }
        if (buttonVkontakte) {
            buttons.add("vkontakte");
        }
        if (buttonOdnoklassniki) {
            buttons.add("odnoklassniki");
        }
        sb.append(CollectionUtils.join(",", buttons));
        sb.append("\" data-counter=\"\"></div>");
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
