package com.softmotions.ncms

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
interface DatabaseTestRunner {

    fun setupDb(props: Map<String, Any> = emptyMap<String, Any>())

    fun shutdownDb()
}