package com.softmotions.ncms.marketing

import com.google.inject.AbstractModule
import com.google.inject.Singleton
import com.softmotions.ncms.marketing.mtt.MttRulesRS

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
class MarketingModule : AbstractModule() {

    override fun configure() {
        bind(MttRulesRS::class.java).`in`(Singleton::class.java)
    }
}
