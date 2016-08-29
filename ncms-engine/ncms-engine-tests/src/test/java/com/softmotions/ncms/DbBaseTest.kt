package com.softmotions.ncms

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
open class DbBaseTest(db: String) : GuiceBaseTest() {

    val dbTestRunner: DatabaseTestRunner

    init {

        when (db) {
            "db2" -> {
                dbTestRunner = Db2TestRunner()
            }
            "postgresql" -> {
                dbTestRunner = PostgresTestRunner()
            }
            else -> throw RuntimeException("Invalid database id: ${db}")
        }
    }

    fun setupDb(props: Map<String, Any> = emptyMap<String, Any>()) {
        val sysprops = System.getProperties()
        val pnames = sysprops.propertyNames()
        while (pnames.hasMoreElements()) {
            val pname = pnames.nextElement() as String
            if (pname.startsWith("JDBC.")) {
                sysprops.remove(pname)
            }
        }
        // todo review
        System.setProperty("liquibase.dropAll", "true");
        dbTestRunner.setupDB(props)
    }

    fun shutdownDB() {
        dbTestRunner.shutdownDB()
    }
}