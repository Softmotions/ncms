package com.softmotions.ncms.media.db;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.config.GlobalProperties;

public class CheckDataSource {

    public static void main(String[] args) {

        String sql = "select count(*) as count from dual";
        SqlRow row =
            Ebean.createSqlQuery(sql)
            .findUnique();

        Integer i = row.getInteger("count");

        System.out.println("Got "+i+"  - DataSource good.");
    }
}  