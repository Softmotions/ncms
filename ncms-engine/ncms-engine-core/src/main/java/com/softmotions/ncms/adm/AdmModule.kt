package com.softmotions.ncms.adm

import com.google.inject.AbstractModule
import com.google.inject.Singleton

/**
 * Admin-zone GUI RESTFull modules.
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AdmModule : AbstractModule() {

    override fun configure() {
        bind(WorkspaceRS::class.java).`in`(Singleton::class.java)
        bind(AdmUIResourcesRS::class.java).`in`(Singleton::class.java)
    }
}
