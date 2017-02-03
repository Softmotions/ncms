.. _conf:

.. contents::

Server configuration
====================

The ηCMS server configuration is defined in the XML file. At the start the server reads
the OS environment or system JVM property: `WEBOOT_CFG_LOCATION`,
specifying the configuration file. The file can be a file system
resource or a resource in server classpath on JVM.
The system won't be launched if `WEBOOT_CFG_LOCATION`
isn't set by one of the methods stated above.

To work with the server configuration we use
`Apache Сommons Сonfiguration <https://commons.apache.org/proper/commons-configuration/>`_
and, as a consequence, a reference to values of other items are supported in the file, e.g.::

    <root>http://${server-name}:${server-port}</root>

where `server-name` and `server-port` are XML items of the configuration.

Additionally, in the configuration file you can use substitutes (placeholders)
as `{name}`, where `{name}` can take the following values:

* {cwd}    --  current working directory
* {home}   --  home directory of the user who runs the ηCMS server
* {tmp}    --  temporary folder
* {newtmp} --  unique temporary directory for the running application
* {webapp} --  path to the root of the deployed web application
* {env:NAME} -- where *NAME* - the name of the environment variable
* {sys:NAME} -- where *NAME* - the name of the JVM system property


.. _conf_sample:

Example of ηСМС configuration file
----------------------------------

.. code-block:: xml


    <?xml version="1.0" encoding="utf-8" ?>
    <configuration>
        <!-- Reference to the logging configuration relative
             to this config file
             http://logback.qos.ch/manual/configuration.html
        -->
        <logging-ref>app-logging.xml</logging-ref>

        <!-- Application name -->
        <app-name>My app</app-name>

        <!--
            Application working mode:
            - production    Working in production
            - dev           Developer mode, debugging enabled.
            - test          Testing
        -->
        <environment>dev</environment>
        <!-- Domain name of web-server or IP-address for external clients by default -->
        <server-name>127.0.0.1</server-name>
        <!-- Server port -->
        <server-port>8080</server-port>
        <site>
            <!-- Main web-server HTTP url -->
            <root>http://${server-name}:${server-port}</root>
            <!-- If true, then the webserver address for external
                 clients (for example for email messages and links)
                 is created based on the data of client's HTTP requests
                 if it is possible. Otherwise configuration
                 parameter `site/root` is used.
            -->
            <preferRequestUrl>true</preferRequestUrl>
        </site>

        <!-- Application prefix with respect to the web container root.
             See http://ncms.one/manual/en/doc/deployments/index.html
        -->
        <app-prefix>/</app-prefix>

        <!--
            A list of additional resources used for localization
            of server messages.  If you do not use additional localized resources,
            this configuration section can be omitted.
            Refer to http://ncms.one/manual/en/doc/extending/index.html
        -->
        <messages>
            <bundle>com.softmotions.ncms.Messages</bundle>
            <bundle>myapp.Messages</bundle>
        </messages>

        <asm>
            <!-- The directory in the ηCMS media repository,
                     which is the root for files/resources
                     available as a web resources via HTTP-protocol.
                 Default value: /site -->
            <site-files-root>/site</site-files-root>
        </asm>

        <!-- Configuration of static web-resources,
             stored in .jar files of the application -->
        <jar-web-resources>
            <!-- Administrative site area,
                 available by the address: /adm -->
            <resource>
                <path-prefix>/adm</path-prefix>
                <options>/myapp-qx/myapp,watch=true</options>
            </resource>
        </jar-web-resources>

        <!-- ηCMS resource caching on the client side  -->
        <cache-headers-groups>
            <cache-group>
                <!-- All css and js files are cached for 2 hours-->
                <patterns>*.css,*.js</patterns>
                <expiration>7200</expiration>
            </cache-group>
            <cache-group>
                <!-- Static media resources of sites are cached for 2 hours -->
                <patterns>/rs/media/fileid/*,/images/*,/adm/resource/*</patterns>
                <expiration>7200</expiration>
            </cache-group>
            <cache-group>
                <!-- Cancel caching for all resources in /adm/script/* -->
                <nocache>true</nocache>
                <patterns>/adm/script/*</patterns>
            </cache-group>
        </cache-headers-groups>

        <!-- File of a database schema creation http://www.liquibase.org/ -->
        <liquibase>
            <changelog>com/softmotions/ncms/db/changelog/db-changelog-master.xml</changelog>
            <!-- Liquibase updates the database structure at the server start -->
            <update/>
        </liquibase>

        <!-- Parameters of communication between the application and the database -->
        <mybatis>
            <bindDatasource>true</bindDatasource>
            <!-- Mybatis application configuration -->
            <config>com/softmotions/ncms/db/mybatis-config.xml</config>
            <!-- File containing the login and password for the connection to the database -->
            <propsFile>{home}/.myapp.ds</propsFile>
            <extra-properties>
                JDBC.driver=com.ibm.db2.jcc.DB2Driver
            </extra-properties>
            <extra-mappers>
                <mapper>
                    <!-- Additionally, plug in the Mybatis configuration
                         if you are to extend the ηCMS functionality
                         Refer to http://ncms.one/manual/doc/extending/index.html -->
                    <!--<resource>extra_mybatis_mapper.xml</resource>-->
                </mapper>
            </extra-mappers>
        </mybatis>

        <media>
            <!-- Directory where ηCMS media files are stored -->
            <basedir>{home}/.myapp/media</basedir>
            <!-- The maximum size of the file loaded to ηCMS (bytes).
                 By default 30MB -->
            <max-upload-size>31457280</max-upload-size>
            <!-- The maximum size of the data stored in the RAM cache
                     of the ηCMS server while uploading a file.
                 By default 10MB-->
            <max-upload-inmemory-size>1048576</max-upload-inmemory-size>
            <!-- The size of preview icons for the images in the ηCMS media repository -->
            <thumbnails-width>250</thumbnails-width>
            <!-- The maximum size of the text file being edited in a editor -->
            <max-edit-text-size>524288</max-edit-text-size>
            <!-- Set of automatic import rules <import>
                 of media-files from the file storage (where the ηCMS server works)
            <import>
                <!-- Source directory for import -->
                <directory>{webapp}</directory>
                <!-- Target directory for import -->
                <target>site</target>
                <!-- Watch for changes in the source files -->
                <watch>true</watch>
                <!-- If the file in the target directory was modified later than the source file,
                     it will not be overwritten. By default: false -->
                <overwrite>false</overwrite>
                <!-- Templates of files included to the source directory
                     same as Ant include patterns https://ant.apache.org/manual/dirtasks.html
                -->
                <includes>
                    <include>**/*</include>
                </includes>
                <!-- Templates of files excluded from the source directory -->
                <excludes>
                    <exclude>META-INF/**</exclude>
                    <exclude>WEB-INF/**</exclude>
                    <exclude>scss/**</exclude>
                </excludes>
            </import>
        </media>

        <!-- Configuration of the HTTL markup parser.
             extensions: Possible extensions of httl files.
                         By default: *,httl,html,httl.css -->
        <httl extensions="*,httl,html,httl.css">
            <!-- HTTL configuration properties
                 Refer to http://httl.github.io/en/config.html -->
            import.methods+=myapp.AppHttlMethods
            import.packages+=myapp
        </httl>

        <!-- ηCMS authentication and authorization setting -->
        <security>
            <!-- Location of the user ηCMS Database in an XML file.
                 placeTo: Optional. Location to copy the user database
                          for the future editing via the ηCMS user management interface	 -->
            <xml-user-database placeTo="{home}/.myapp/mayapp-users.xml">
                <!-- Initial path to the classpath for the read-only ηCMS users database.
                         If placeTo attribute is set and the file is missing, the database will be copied
                     to the location determined via placeTo -->
                conf/mayapp-users.xml
            </xml-user-database>
            <!-- Hash  generation algorithm for passwords in an XML database ηCMS users.
                     Possible values:
                    - sha256
                    - bcrypt
                    - empty string or absent element: passwords are not encrypted
             -->
            <password-hash-algorithm>sha256</password-hash-algorithm>
            <!-- Path to the Shiro(https://shiro.apache.org/) configuration
                 used in ηCMS -->
            <shiro-config-locations>/WEB-INF/shiro.ini</shiro-config-locations>
            <!-- ηCMS user database name -->
            <dbJVMName>WSUserDatabase</dbJVMName>
        </security>

        <!-- UI administrator components -->
        <ui>
            <navigation-selectors>
                <!-- ηCMS pages management components
                     roles: roles of users
                            having access to the component -->
                <widget qxClass="ncms.pgs.PagesNav" roles="user"/>
                <!-- ηCMS news and events feed -->
                <widget qxClass="ncms.news.NewsNav" roles="user"/>
                <!-- Interface of media files management -->
                <widget qxClass="ncms.mmgr.MediaNav" roles="user"/>
                <!-- Interface of assembly management -->
                <widget qxClass="ncms.asm.AsmNav" roles="admin.asm"/>
                <!-- Interface of  Marketing transfer tools (MTT) -->
                <widget qxClass="ncms.mtt.MttNav" roles="mtt" extra="true"/>
                <!-- Interface of MTT tracking pixels management
                <widget qxClass="ncms.mtt.tp.MttTpNav" roles="mtt" extra="true"/>
                <!-- Interface of user and user access management -->
                <widget qxClass="ncms.usr.UsersNav" roles="admin.users" extra="true"/>
            </navigation-selectors>
        </ui>

        <mediawiki>
            <!-- The maximum width of the image when displaying within mediawiki page -->
            <max-inline-image-width-px>900</max-inline-image-width-px>
            <!-- Additional mediawiki tags -->
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

        <!-- Additional Guice modules
             registered when ηCMS starts.
             Refer to http://ncms.one/manual/doc/extending/index.html -->
        <modules>
            <module>myapp.AppModule</module>
        </modules>
    </configuration>

.. _conf_extended:

Additional (Advanced) configuration items
-----------------------------------------

.. code-block:: xml

    <?xml version="1.0" encoding="utf-8" ?>
    <configuration>
        ...
        <asm>
            ...
            <!-- Loaders of HTTL templates.
                 By default a loader of media files of the ηCMS repository is used.
            -->
            <resource-loaders>
                <loader>com.softmotions.ncms.asm.render.ldrs.AsmMediaServiceResourceLoader</loader>
            </resource-loaders>
        </asm>

        <pages>
            <!-- The maximum size of the LRU cache
                     for the storing of the meta information (com.softmotions.ncms.asm.CachedPage)
                     about recently used pages of the site.
                 By default: 1024 -->
            <lru-cache-size>1024</lru-cache-size>

            <!-- Maximum LRU size of a cache for page aliases.
                 For the fast search of a page using its alias.
                 By default: 8192
            -->
            <lru-aliases-cache-size>8192</lru-aliases-cache-size>
        </pages>

        <media>
            ...
            <!-- List of directories in the ηCMS media repository
                 which are to be marked as 'system' -->
            <system-directories>
                <directory>/site</directory>
                <directory>/pages</directory>
            </system-directories>
        </media>

        <security>
            ...
            <!-- Maximum LRU cache size
                 for storage of user access control list (ACL)
                 to the website pages.
                 By default: 1024
            -->
            <acl-lru-cache-size>1024</acl-lru-cache-size>
        </security>

        <mediawiki>
            ...
            <!-- Registration of additional
                 interwiki links to ηCMS mediawiki unit (module).
                 API project Bliki https://bitbucket.org/axelclk/info.bliki.wiki/overview
            -->
            <interwiki-links>
                <link key="page" value="/asm/$1"/>
            </interwiki-links>

            <mediawiki.image-base-url>/rs/mw/res/${image}</mediawiki.image-base-url>
            <mediawiki.link-base-url>/rs/mw/link/${title}</mediawiki.link-base-url>
        </mediawiki>

        <!-- Help page references for ηCMS UI elements.
             The format: key => link to the reference page
             There are the following keys in the current version: wiki.gmap, wiki -->
        <help>
            <topics>
                <!-- Information on how to enter the frame of the Google Maps location -->
                <topic key="wiki.gmap">https://support.google.com/maps/answer/3544418</topic>
                <!-- Information on wiki markup -->
                <topic key="wiki">...</topic>
            </topics>
        </help>

        <events>
            <!-- The number of threads that are used
                      to handle asynchronous events in ηCMS
                      Default: 1 thread.
                      Do not change this setting,
                      if you do not exactly know what you are doing
            -->
            <num-workers>1</num-workers>
        </events>

        <!-- Task executor configuration for different
             asynchronous tasks. -->
        <executor> <!-- set of elements -->
            <!-- executor name -->
            <name>default</name>
            <!-- Number of threads -->
            <threads-num>allcores</threads-num>
            <!-- Maximum size of the task queue
                 java.util.concurrent.LinkedBlockingQueue
            -->
            <queue-size>1000</queue-size>
        </executor>

        <browser-filter/>
        <solr/>
        <scheduler/>

    </configuration>