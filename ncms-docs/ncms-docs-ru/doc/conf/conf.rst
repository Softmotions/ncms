.. _conf:

.. contents::

Конфигурация сервера
====================

Конфигурация сервера ηCMS определяется в XML файле.
Сервер при старте читает переменную ОС или системное JVM свойство
с именем `WEBOOT_CFG_LOCATION`, которое указывает на файл конфигурации.
Файл может быть ресурсом в файловой системе или ресурсом в classpath JVM сервера ηCMS.
Система не запустится, если `WEBOOT_CFG_LOCATION` не был задан одним из указанных выше способов.

Для работы с конфигурацией сервера используется
`Apache Сommons Сonfiguration <https://commons.apache.org/proper/commons-configuration/>`_
и, как следствие, в файле поддерживаются ссылки на значения других
элементов, например::

    <root>http://${server-name}:${server-port}</root>

где `server-name` и `server-port` являются XML элементами конфигурации.

Дополнительно в файле конфигурации можно использовать заменители (placeholders)
в виде `{name}`, где {name} может принимать следующие значения:

* {cwd}    --  текущая рабочая директория
* {home}   --  домашняя директория пользователя, из-под которого запущен сервер ηCMS
* {tmp}    --  временная папка
* {newtmp} --  уникальная временная директория для запущенного приложения
* {webapp} -- путь до корня развернутого веб приложения
* {env:NAME} -- Где *NAME* - имя переменной окружения
* {sys:NAME} -- Где *NAME* - имя системного свойства JVM

.. _conf_sample:

Пример файла конфигурации ηCMS с комментариями
----------------------------------------------

.. code-block:: xml

    <?xml version="1.0" encoding="utf-8" ?>
    <configuration>
        <!-- Ссылка на лог конфигурацию проекта
             http://logback.qos.ch/manual/configuration.html
             Относительно этого файла -->
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
        <server-port>8080</server-port>
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
             См. http://ncms.one/manual/ru/doc/deployments/index.html
        -->
        <app-prefix>/</app-prefix>

        <!--
            Список дополнительных ресурсов, используемых для локализации
            сообщений сервера. Можно опустить этот блок в конфигурации, в том случае если вы
            не используете дополнительные локализованные ресурсы.
            См. http://ncms.one/manual/ru/doc/extending/index.html
        -->
        <messages>
            <bundle>com.softmotions.ncms.Messages</bundle>
            <bundle>myapp.Messages</bundle>
        </messages>

        <asm>
            <!-- Директория в медиа-репозитории ηCMS,
                 являющаяся корневой для файлов/ресурсов
                 доступных как веб ресурсы по HTTP протоколу.
                 По умолчанию /site -->
            <site-files-root>/site</site-files-root>
        </asm>

        <!-- Конфигурация статических веб ресурсов,
             хранящихся в .jar файлах приложения -->
        <jar-web-resources>
            <!-- Административная зона сайта,
                 доступная по адресу: /adm -->
            <resource>
                <path-prefix>/adm</path-prefix>
                <options>/myapp-qx/myapp,watch=true</options>
            </resource>
        </jar-web-resources>

        <!-- Управление кешированием ресурсов ηCMS в на стороне клиента  -->
        <cache-headers-groups>
            <cache-group>
                <!-- Все css и js файлы кешируются на 2 часа-->
                <patterns>*.css,*.js</patterns>
                <expiration>7200</expiration>
            </cache-group>
            <cache-group>
                <!-- Статические медиа ресурсы сайтов кешируются на 2 часа -->
                <patterns>/rs/media/fileid/*,/images/*,/adm/resource/*</patterns>
                <expiration>7200</expiration>
            </cache-group>
            <cache-group>
                <!-- Отмена кеширования для всех ресурсов под /adm/script/* -->
                <nocache>true</nocache>
                <patterns>/adm/script/*</patterns>
            </cache-group>
        </cache-headers-groups>

        <!-- Файл создания схемы базы данных http://www.liquibase.org/ -->
        <liquibase>
            <changelog>com/softmotions/ncms/db/changelog/db-changelog-master.xml</changelog>
            <!-- Liquibase обновит структуру базы данных при старте -->
            <update/>
        </liquibase>

        <!-- Параметры связи приложения с базой данных -->
        <mybatis>
            <bindDatasource>true</bindDatasource>
            <!-- Mybatis конфигурация приложения -->
            <config>com/softmotions/ncms/db/mybatis-config.xml</config>
            <!-- Файл с логином и паролем для соединения с базой данных -->
            <propsFile>{home}/.myapp.ds</propsFile>
            <extra-properties>
                JDBC.driver=com.ibm.db2.jcc.DB2Driver
            </extra-properties>
            <extra-mappers>
                <mapper>
                    <!-- Дополнительно подключаемая конфигурация Mybatis
                         в случае если вы расширяете функционал ηCMS
                         См. http://ncms.one/manual/doc/extending/index.html -->
                    <!--<resource>extra_mybatis_mapper.xml</resource>-->
                </mapper>
            </extra-mappers>
        </mybatis>

        <media>
            <!-- Директория, где хранятся медиа файлы сайтов ηCMS -->
            <basedir>{home}/.myapp/media</basedir>
            <!-- Максимальный размер файла в байтах загружаемого в ηCMS.
                 По умолчанию 30Мб -->
            <max-upload-size>31457280</max-upload-size>
            <!-- Максимальный размер данных, сохраняемых в оперативную
                 память сервера ηCMS при загрузке файла.
                 По умолчанию 10Мб-->
            <max-upload-inmemory-size>1048576</max-upload-inmemory-size>
            <!-- Размер иконок предпросмотра для изображений в репозитории ηCMS -->
            <thumbnails-width>250</thumbnails-width>
            <!-- Максимальный размер текстового файла редактируемого в редакторе -->
            <max-edit-text-size>524288</max-edit-text-size>
            <!-- Множество элементов <import> автоматического импорта
                 медиа файлов из файловой системы (где работает сервер ηCMS)
            <import>
                <!-- Исходная директория для импорта. -->
                <directory>{webapp}</directory>
                <!-- Целевая директория для импорта -->
                <target>site</target>
                <!-- Следить за изменениями исходных файлов,
                     для того, чтобы синхронно изменять их в
                     медиа репозитории ηCMS -->
                <watch>true</watch>
                <!-- В случае, если время изменения файла в целевой директории,
                     больше, чем время изменения исходного файла, то он не будет переписан.
                     По умолчанию: false
                     -->
                <overwrite>false</overwrite>
                <!-- Шаблоны включаемых файлов в исходной директории
                     аналогично https://ant.apache.org/manual/dirtasks.html
                -->
                <includes>
                    <include>**/*</include>
                </includes>
                <!-- Шаблоны исключаемых файлов в исходной директории -->
                <excludes>
                    <exclude>META-INF/**</exclude>
                    <exclude>WEB-INF/**</exclude>
                    <exclude>scss/**</exclude>
                </excludes>
            </import>
        </media>

        <!-- Конфигурация парсера HTTL разметки.
             extensions: Возможные расширения httl файлов.
                         По умолчанию: *,httl,html,httl.css -->
        <httl extensions="*,httl,html,httl.css">
            <!-- Свойства конфигурации HTTL
                 см. http://httl.github.io/en/config.html -->
            import.methods+=myapp.AppHttlMethods
            import.packages+=myapp
        </httl>

        <!-- Настройки системы аутентификации и авторизации ηCMS -->
        <security>
            <!-- Расположение базы данных пользователей ηCMS в XML файле.
                 placeTo: Опционально. Расположение, в которое будет скопирована база данных
                          пользователей, для последующего редактирования через интерфейс
                          управления пользователями ηCMS -->
            <xml-user-database placeTo="{home}/.myapp/mayapp-users.xml">
                <!-- Начальный путь в classpath для read-only базы данных пользователей ηCMS.
                     Если указан атрибут placeTo, то база данных будет скопирована
                     в место, указанное placeTo в том случае, если файл отсутствовал -->
                conf/mayapp-users.xml
            </xml-user-database>
            <!-- Алгоритм для генерации хешей для паролей в XML базе данных пользователей ηCMS.
                 Возможные значения:
                    - sha256
                    - bcrypt
                    - пустая строка или отсутствие элемента: пароли не шифруются
             -->
            <password-hash-algorithm>sha256</password-hash-algorithm>
            <!-- Путь до конфигурации https://shiro.apache.org/
                 который используется в ηCMS -->
            <shiro-config-locations>/WEB-INF/shiro.ini</shiro-config-locations>
            <!-- Имя базы данных пользователей в ηCMS -->
            <dbJVMName>WSUserDatabase</dbJVMName>
        </security>

        <!-- Компоненты UI администратора -->
        <ui>
            <navigation-selectors>
                <!-- Компонент управления страницами ηCMS
                     roles: роли пользователей,
                            имеющих доступ к данному компоненту -->
                <widget qxClass="ncms.pgs.PagesNav" roles="user"/>
                <!-- Ленты новостей и событий ηCMS -->
                <widget qxClass="ncms.news.NewsNav" roles="user"/>
                <!-- Интерфейс управление медиафайлами -->
                <widget qxClass="ncms.mmgr.MediaNav" roles="user"/>
                <!-- Интерфейс управления сборкими -->
                <widget qxClass="ncms.asm.AsmNav" roles="admin.asm"/>
                <!-- Интерфейс Marketing transfer tools (MTT) -->
                <widget qxClass="ncms.mtt.MttNav" roles="mtt" extra="true"/>
                <!-- Интерфейс управления tracking pixels в MTT
                <widget qxClass="ncms.mtt.tp.MttTpNav" roles="mtt" extra="true"/>
                <!-- Интерфейс управления пользователями и правами доступа -->
                <widget qxClass="ncms.usr.UsersNav" roles="admin.users" extra="true"/>
            </navigation-selectors>
        </ui>

        <mediawiki>
            <!-- Максимальная ширина изображения при отображении mediawiki блока
                 в контексте страницы -->
            <max-inline-image-width-px>900</max-inline-image-width-px>
            <!-- Регистрация дополнительных тегов для mediawiki парсера -->
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
        </mediawiki>

        <!-- Дополнительные (опциональные) Guice модули
             регистрируемые при старте ηCMS.
             См. http://ncms.one/manual/doc/extending/index.html -->
        <modules>
            <module>myapp.AppModule</module>
        </modules>
    </configuration>


.. _conf_extended:

Дополнительные (Advanced) элементы конфигурации
-----------------------------------------------

.. code-block:: xml

    <?xml version="1.0" encoding="utf-8" ?>
    <configuration>
        ...
        <asm>
            ...
            <!-- Загрузчики HTTL шаблонов ηCMS.
                 По умолчанию используется загрузчик файлов медиа репозитория ηCMS.
            -->
            <resource-loaders>
                <loader>com.softmotions.ncms.asm.render.ldrs.AsmMediaServiceResourceLoader</loader>
            </resource-loaders>
        </asm>

        <pages>
            <!-- Максимальный размер LRU кеша
                 для хранения мета информации (com.softmotions.ncms.asm.CachedPage)
                 о страницах сайта,
                 к которым недавно осуществлялся доступ (resently used).
                 По умолчанию: 1024 -->
            <lru-cache-size>1024</lru-cache-size>

            <!-- Максимальный размер LRU кеша псевдонимов страниц (page alias).
                 Для быстрого нахождения страницы по псевдониму.
                 По умолчанию: 8192
            -->
            <lru-aliases-cache-size>8192</lru-aliases-cache-size>
        </pages>

        <media>
            ...
            <!-- Список директорий в медиа репозитории ηCMS,
                 которые будут помечены, как системные -->
            <system-directories>
                <directory>/site</directory>
                <directory>/pages</directory>
            </system-directories>
        </media>

        <security>
            ...
            <!-- Максимальный размер LRU кеша
                 для хранения прав доступа (ACL) пользователей
                 к страницам сайта.
                 По умолчанию: 1024
            -->
            <acl-lru-cache-size>1024</acl-lru-cache-size>
        </security>

        <mediawiki>
            ...
            <!-- Регистрация дополнительных
                 interwiki ссылок в mediawiki модуль ηCMS.
                 API проекта Bliki https://bitbucket.org/axelclk/info.bliki.wiki/overview
            -->
            <interwiki-links>
                <link key="page" value="/asm/$1"/>
            </interwiki-links>

            <mediawiki.image-base-url>/rs/mw/res/${image}</mediawiki.image-base-url>
            <mediawiki.link-base-url>/rs/mw/link/${title}</mediawiki.link-base-url>
        </mediawiki>


        <!-- Репозиторий справочных страниц, которые
             используются в различных элементах UI ηCMS.
             В формате: ключ => ссылка на страницу справки
             В текущей версии используются следующие ключи: wiki.gmap, wiki -->
        <help>
            <topics>
                <!-- Справка по вставки фрейма местоположения в Google maps -->
                <topic key="wiki.gmap">https://support.google.com/maps/answer/3544418</topic>
                <!-- Ссылка на документацию по wiki разметке -->
                <topic key="wiki">...</topic>
            </topics>
        </help>

        <events>
            <!-- Количество потоков, которые используются
                 для обработки асинхронных событий в ηCMS
                 По умолчанию: 1 поток.
                 Не меняйте данную настройку,
                 если в точности не знаете, что делаете
            -->
            <num-workers>1</num-workers>
        </events>

        <!-- Конфигурация task executor-ов для различных
             асинхронных задач. -->
        <executor> <!-- множество элементов -->
            <!-- Имя экзекютора -->
            <name>default</name>
            <!-- Количество потоков -->
            <threads-num>allcores</threads-num>
            <!-- Максимальный размер очереди задач
                 java.util.concurrent.LinkedBlockingQueue
            -->
            <queue-size>1000</queue-size>
        </executor>

        <browser-filter/>
        <solr/>
        <scheduler/>

    </configuration>

