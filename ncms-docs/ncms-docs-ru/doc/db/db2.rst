.. _db2:

IBM DB2
=======

ηCMS поддерживает IBM DB2 версии не ниже `9.7`


Установка DB2
-------------
Версия IBM DB2 Express-C является бесплатной и, в тоже время,
мощной СУБД, достаточной для работы десятков высоконагруженных
сайтов в одном экземпляре ηCMS. Для установки и настройки DB2
рекомендуем обратиться к документации IBM.

Ниже пример шагов для быстрой установки db2 express-c `v11.1` на ОС Ubuntu
для разработчика сайтов на ηCMS.

Установка DB2 на Ubuntu
***********************

#. Предполагается, что система Ubuntu (x64) Версии 16.x
#. Получаем дистрибутив `v11.1_linuxx64_expc.tar.gz`
#. Становимся рутом `sudo su -`::

     apt-get install libstdc++6:i386 libpam0g:i386 \
             libstdc++6 lib32stdc++6 \
             libaio1 gcc ksh numactl
#. Устанавливаем экземпляр СУБД::

    cd ./expc
    ./db2_install
    useradd -m db2inst1 -s /bin/bash
    useradd -m db2fenc1
    /opt/ibm/db2/V11.1/instance/db2icrt -u db2fenc1 db2inst1

#. Добавляем себя в группу: `db2inst1`
#. Базовая настройка режима работы экземпляра::

    sudo su - db2inst1
    db2set DB2_COMPATIBILITY_VECTOR=4000
    db2stop
    db2start



Создание базы данных
********************

#. На данном шаге необходимо выбрать пользователя базы данных.
   Пусть это будет `ncms`::

    useradd -m ncms
    passwd ncms
    # Пароль пользователя ncms будет использоваться для доступа к базе


Создание новой базы данных
--------------------------

Пусть имя новой базы данных будет `NCMS`,  имя пользователя базы `ncms`

.. code-block:: sql

    CREATE DATABASE NCMS AUTOMATIC STORAGE YES
                         USING CODESET UTF-8
                         TERRITORY RU
                         COLLATE USING SYSTEM
                         PAGESIZE 16384;
    CONNECT TO NCMS;

    CREATE BUFFERPOOL BIGPAGEBP IMMEDIATE
           SIZE 4096 AUTOMATIC
           PAGESIZE 32 K;

    CREATE SYSTEM TEMPORARY TABLESPACE TEMPSPACE32
           PAGESIZE 32 K
           MANAGED BY AUTOMATIC STORAGE
           BUFFERPOOL BIGPAGEBP;

    CREATE SCHEMA NCMS AUTHORIZATION ncms;

    GRANT DBADM ON DATABASE TO USER ncms;


Конфигурация  ηCMS
------------------

Для работы с новой базой необходимо настроить использование
правильного JDBC драйвера. Пример конфигурации:

.. code-block:: xml

    <mybatis>
        <bindDatasource>true</bindDatasource>
        <config>com/softmotions/ncms/db/mybatis-config.xml</config>
        <propsFile>{home}/.ncmsapp.ds</propsFile>
        <extra-properties>
            JDBC.driver=com.ibm.db2.jcc.DB2Driver
        </extra-properties>
        ...
    </mybatis>

.. warning::

    Убедитесь в том, что в элементе конфигурации `mybatis/extra-properties`
    присутствует JDBC драйвер для DB2: `com.ibm.db2.jcc.DB2Driver`

Где в файле `${HOME}/.ncmsapp.ds` хранятся JDBC URL, имя пользователя и пароль к БД::

    JDBC.url=jdbc:db2://127.0.0.1:50000/NCMS
    JDBC.username=ncms
    JDBC.password=xxxxxx



