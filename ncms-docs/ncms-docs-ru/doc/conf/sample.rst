.. _conf_sample:

Пример файла конфигурации с комментариями
=========================================

.. code-block:: xml

    <?xml version="1.0" encoding="utf-8" ?>
    <configuration>
        <!-- Ссылка на лог конфигурацию проекта
             http://logback.qos.ch/manual/configuration.html -->
        <logging-ref>app-logging.xml</logging-ref>
        <!-- Название приложения -->
        <app-name>My app</app-name>
        <!--
            Режим работы приложения:
            - production    Приложение в нормальном режиме работы
            - dev           Приложение в режиме работы для разработчика
                                Включен debug режим работы.
            - test          Приложение в режиме выполнения тест кейсов
        -->
        <environment>dev</environment>
        <!-- DNS имя веб сервера или IP адрес для по умолчанию внешних клиентов -->
        <server-name>127.0.0.1</server-name>
        <!-- Порт сервера для внешних клиентов -->
        <server-port>${serverPort}</server-port>
        <site>
            <!-- Основной адрес веб сервера для для внешних клиентов -->
            <root>http://${server-name}:${server-port}</root>
            <!-- Если true, то адрес веб сервера для внешних
                 клиентов (например в email сообщениях и ссылках)
                 будет создаваться на основе данных
                 HTTP запросов клиентов, если это возможно.
                 В противном случае будет использоваться параметр
                 xml конфигурации: site/root
            -->
            <preferRequestUrl>true</preferRequestUrl>
        </site>

        <!-- Префикс приложения относительно корня веб контейнера.
             См. http://ncms.one/manual/doc/deployments/index.html
        -->
        <app-prefix>/</app-prefix>

        <!--
            Список реусрсов используемых для локализации
            сообщений сервера. Можно не включать этот
            этот блок в конфигурацию, в том случае если
            не используются расширения nCMS http://ncms.one/manual/doc/extending/index.html
        -->
        <messages>
            <bundle>com.softmotions.ncms.Messages</bundle>
            <bundle>myapp.Messages</bundle>
        </messages>

        <asm>
            <site-files-root resolveRelativePaths="true">/site</site-files-root>
            <resource-loaders>
                <loader>com.softmotions.ncms.asm.render.ldrs.AsmMediaServiceResourceLoader</loader>
            </resource-loaders>
        </asm>

        <pages>
            <default-page-language>en</default-page-language>
            <lru-cache-size>4096</lru-cache-size>
            <lru-aliases-cache-size>16384</lru-aliases-cache-size>
        </pages>

        <jar-web-resources>
            <resource>
                <path-prefix>/adm</path-prefix>
                <options>/${rootArtifactId}-qx/${rootArtifactId},X-App-Prefix=/,watch=true</options>
            </resource>
            <resource>
                <path-prefix>/manual</path-prefix>
                <options>/ncmsmanual_ru</options>
            </resource>
        </jar-web-resources>

        <cache-headers-groups>
            <cache-group>
                <patterns>*.css,*.js</patterns>
                <expiration>7200</expiration>
            </cache-group>
            <cache-group>
                <patterns>/rs/media/fileid/*,/images/*,/adm/resource/*</patterns>
                <expiration>7200</expiration>
            </cache-group>
            <cache-group>
                <nocache>true</nocache>
                <patterns>/adm/script/*</patterns>
            </cache-group>
        </cache-headers-groups>

        <liquibase>
            <changelog>com/softmotions/ncms/db/changelog/db-changelog-master.xml</changelog>
            <update/>
        </liquibase>

        <mybatis>
            <bindDatasource>true</bindDatasource>
            <config>com/softmotions/ncms/db/mybatis-config.xml</config>
            <propsFile>{home}/.${rootArtifactId}.ds</propsFile>
            <extra-properties>
                JDBC.driver=com.ibm.db2.jcc.DB2Driver
            </extra-properties>
            <extra-mappers>
                <mapper>
                    <!--<resource>extra_mybatis_mapper.xml</resource>-->
                </mapper>
            </extra-mappers>
        </mybatis>

        <media>
            <basedir>{home}/.${rootArtifactId}/media</basedir>
            <max-upload-size>31457280</max-upload-size>
            <max-upload-inmemory-size>1048576</max-upload-inmemory-size>
            <locks-lrucache-size>128</locks-lrucache-size>
            <meta-lrucache-size>1024</meta-lrucache-size>
            <thumbnails-width>250</thumbnails-width>
            <resize-default-format>jpeg</resize-default-format>
            <max-edit-text-size>524288</max-edit-text-size>
            <system-directories>
                <directory>/site</directory>
                <directory>/pages</directory>
            </system-directories>
            <import>
                <directory>{webapp}</directory>
                <target>site</target>
                <watch>true</watch>
                <overwrite>false</overwrite>
                <system>true</system>
                <includes>
                    <include>**/*</include>
                </includes>
                <excludes>
                    <exclude>META-INF/**</exclude>
                    <exclude>WEB-INF/**</exclude>
                    <exclude>scss/**</exclude>
                </excludes>
            </import>
        </media>

        <httl extensions="*,httl,html,httl.css">
            import.methods+=${package}.AppHttlMethods
            import.packages+=${package}
        </httl>

        <security>
            <xml-user-database placeTo="{home}/.${rootArtifactId}/${rootArtifactId}-users.xml">conf/${rootArtifactId}-users.xml</xml-user-database>
            <shiro-config-locations>/WEB-INF/shiro.ini</shiro-config-locations>
            <password-hash-algorithm>sha256</password-hash-algorithm>
            <dbJVMName>WSUserDatabase</dbJVMName>
            <acl-lru-cache-size>4096</acl-lru-cache-size>
            <!--<web-access-control-allow>*</web-access-control-allow>-->
        </security>

        <ui>
            <navigation-selectors>
                <widget qxClass="ncms.pgs.PagesNav" roles="user"/>
                <widget qxClass="ncms.news.NewsNav" roles="user"/>
                <widget qxClass="ncms.mmgr.MediaNav" roles="user"/>
                <widget qxClass="ncms.asm.AsmNav" roles="admin.asm"/>
                <widget qxClass="ncms.mtt.MttNav" roles="mtt" extra="true"/>
                <widget qxClass="ncms.mtt.tp.MttTpNav" roles="mtt" extra="true"/>
                <widget qxClass="ncms.usr.UsersNav" roles="admin.users" extra="true"/>
            </navigation-selectors>
        </ui>

        <mediawiki>
            <max-inline-image-width-px>900</max-inline-image-width-px>
            <tags>
                <tag name="note" class="com.softmotions.ncms.mediawiki.NoteTag"/>
                <tag name="gmap" class="com.softmotions.ncms.mediawiki.GMapTag"/>
                <tag name="youtube" class="com.softmotions.ncms.mediawiki.YoutubeTag"/>
                <tag name="tree" class="com.softmotions.ncms.mediawiki.TreeTag"/>
                <tag name="slideshare" class="com.softmotions.ncms.mediawiki.SlideSharePresentationTag"/>
                <tag name="extimg" class="com.softmotions.ncms.mediawiki.ExternalImageTag"/>
                <tag name="vimeo" class="com.softmotions.ncms.mediawiki.VimeoTag"/>
                <tag name="ind" class="com.softmotions.ncms.mediawiki.IndentTag"/>
            </tags>
            <interwiki-links>
                <!--<link key="page" value="/asm/${dollar}1"/>-->
            </interwiki-links>
        </mediawiki>

        <modules>
            <module>${package}.AppModule</module>
        </modules>
    </configuration>