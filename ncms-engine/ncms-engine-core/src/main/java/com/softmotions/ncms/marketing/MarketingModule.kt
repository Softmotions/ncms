package com.softmotions.ncms.marketing

import com.google.inject.AbstractModule
import com.google.inject.Singleton
import com.softmotions.ncms.marketing.rules.RulesRS

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
class MarketingModule : AbstractModule() {

    override fun configure() {
        bind(RulesRS::class.java).`in`(Singleton::class.java)
    }
}
