.. _newproject:

.. contents::

Создание нового проекта
=======================

Предварительные требования
--------------------------

* JDK `1.8`
* Maven версии `3.3.x`
* Python `2.7` необходим для сборки :ref:`административного интерфейса <ui>` ηCMS
* Linux или Windows

.. note::

    Вы можете использовать ηCMS под Windows
    однако тест кейсы |ncmsversion| предназначены
    для работы только на Unix-подобных платформах.
    |ncmsversion| в продакшен режиме рекомендуется использовать
    только на Linux


Использование Maven Archetype
-----------------------------

Лучший способ создать новый проект на базе ηCMS - это создать
его из maven архетипа (maven archetype).

.. code-block:: sh

    mvn org.apache.maven.plugins:maven-archetype-plugin:2.4:generate -U \
        -DarchetypeGroupId=softmotions \
        -DarchetypeArtifactId=ncms-site-archetype \
        -DarchetypeVersion=1.1-SNAPSHOT \
        -DarchetypeRepository=https://repo.softmotions.com/repository/softmotions-public


В качестве альтернативы можно выполнить более короткую команду:

.. code-block:: sh

    mvn org.apache.maven.plugins:maven-archetype-plugin:2.4:generate \
        -DarchetypeCatalog=https://softmotions.com/rs/media/public/1542/archetype-catalog.xml


Продемострируем создание и дальнейшее развитие ηCMS проекта на примере
создания информационного сайта о попугаях.
Код сайта будет в пакете `org.myparrots`.

.. code-block:: text

    $:   mvn org.apache.maven.plugins:maven-archetype-plugin:2.4:generate -U \
    >         -DarchetypeGroupId=softmotions \
    >         -DarchetypeArtifactId=ncms-site-archetype \
    >         -DarchetypeVersion=1.1-SNAPSHOT \
    >         -DarchetypeRepository=https://repo.softmotions.com/repository/softmotions-public
    [INFO] Scanning for projects...

    ....

    Define value for property 'groupId':  org.example: : org.myparrots
    Define value for property 'artifactId':  ncmsapp: : ncmsapp
    Define value for property 'version':  1.0-SNAPSHOT: :
    Define value for property 'package':  org.example: : org.myparrots
    Define value for property 'projectName':  My ηCMS Project: : All about my parrots
    Define value for property 'serverPort':  8080: : 9292
    Confirm properties configuration:
    groupId: org.myparrots
    artifactId: ncmsapp
    version: 1.0-SNAPSHOT
    package: org.myparrots
    projectName: All about my parrots
    serverPort: 9292

Структура проекта
-----------------

В результате выполнения `mvn archetype:generate` мы получили проект
со следующей структурой:

.. code-block:: text

    ncmsapp
    │
    ├── qx/
    │   ├── src/
    │   └── pom.xml
    ├── tomcat/
    │   ├── context.xml
    │   └── server.xml
    ├── web/
    │   ├── src/
    │   └── pom.xml
    ├── pom.xml
    └── README.md


Где:

* `qx` - административный GUI интерфейс сайта, построенный на основе `javascript библиотеки qooxdoo <http://qooxdoo.org>`_
* `web` - бизнес логика сайтов на стороне сервера
* `tomcat` - файлы конфигурации сервера Apache Tomcat для запуска сервера
           в режиме тестирования

Выбор и настройка соединения с БД
---------------------------------

Перед тем, как запустить систему в конфигурации проекта, необходимо настроить параметры
соединения с базой данных.

В версии |ncmsversion| поддерживаются следующие базы данных:

* :ref:`db2`
* :ref:`postgresql`

Параметры соединения приложения с базой данных, как и другие параметры приложения,
определяются в :ref:`файлах конфигурации <conf>`. В данном проекте
это файл: `conf/ncmsapp-dev-configuration.xml`.


.. code-block:: xml

     <mybatis>
        <bindDatasource>true</bindDatasource>
        <config>com/softmotions/ncms/db/mybatis-config.xml</config>
        <propsFile>{home}/.ncmsapp.ds</propsFile>
        <!-- DB2 -->
        <extra-properties>
            JDBC.driver=com.ibm.db2.jcc.DB2Driver
        </extra-properties>
        <!-- Postgresql -->
        <!--
        <extra-properties>
            JDBC.driver=org.postgresql.Driver
        </extra-properties>
        -->
        <extra-mappers>
            <mapper>
                <!--<resource>extra_mybatis_mapper.xml</resource>-->
            </mapper>
        </extra-mappers>
    </mybatis>

Дополнительные параметры соединения с БД определяются в файле `{home}/.ncmsapp.ds`,
где `{home}` - домашняя директория пользователя, из-под которого запущен сервер
ηCMS. Необходимо создать этот файл и заполнить его параметрами аналогично
примеру:

.. code-block:: sh

    cat ~/.ncmsapp.ds

Для DB2:

.. code-block:: sh

    JDBC.url=jdbc:db2://127.0.0.1:50000/NCMS
    JDBC.username=ncms
    JDBC.password=*******

Для PostgreSQL:

.. code-block:: sh

    JDBC.url=jdbc:postgresql://127.0.0.1:5432/ncms
    JDBC.username=ncms
    JDBC.password=*******

Сборка и запуск проекта
-----------------------

.. code-block:: sh

    mvn clean verify && mvn -Pcargo.run


Интерфейс администратора будет доступен по адресу: `http://localhost:<port>/adm/`
Имя пользователя `admin` а пароль находтся в ресурсе: `conf/ncmapp-users.xml`


Новый ηCMS проект на Youtube
----------------------------
..  youtube:: nPIFHWlNcC0
    :width: 100%
