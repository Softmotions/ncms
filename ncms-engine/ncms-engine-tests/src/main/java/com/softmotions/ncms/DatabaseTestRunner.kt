package com.softmotions.ncms

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
interface DatabaseTestRunner {

    fun setupDb(props: Map<String, Any> = emptyMap<String, Any>())

    fun shutdownDb()
}