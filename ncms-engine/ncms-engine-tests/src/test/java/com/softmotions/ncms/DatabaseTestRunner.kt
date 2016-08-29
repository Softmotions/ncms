package com.softmotions.ncms

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
interface DatabaseTestRunner {

    fun setupDB(props: Map<String, Any> = emptyMap<String, Any>())

    fun shutdownDB()
}