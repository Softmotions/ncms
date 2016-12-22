package com.softmotions.ncms

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class DB2TestRunner : DatabaseTestRunner {

    override fun setupDb(props: Map<String, Any>) {
        val home = System.getProperty("user.home")
        System.setProperty("JDBC.env", "development")
        System.setProperty("JDBC.url", "jdbc:db2://127.0.0.1:50000/NCMSTEST:currentSchema=NCMSTEST;")
        System.setProperty("JDBC.driver", "com.ibm.db2.jcc.DB2Driver")
        System.setProperty("JDBC.propsFile", "<propsFile>${home}/.ncms-test.ds</propsFile>")
    }

    override fun shutdownDb() {
    }
}