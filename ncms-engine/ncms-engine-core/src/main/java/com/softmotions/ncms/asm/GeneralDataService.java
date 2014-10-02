package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.Pair;

import java.io.InputStream;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public interface GeneralDataService {

    Pair<String, InputStream> getAdditionalData(String ref) throws Exception;

    boolean isAdditionalDataExists(String ref);

    void saveAdditionalData(String ref, byte[] data, String type);

    void removeAdditionalData(String ref);

}