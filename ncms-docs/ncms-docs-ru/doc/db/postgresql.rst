.. _postgresql:

PostgreSQL
==========

ηCMS поддерживает PostgreSQL версии не ниже `9.6`.

Установка PostgreSQL на Ubuntu
------------------------------

.. code-block:: sh

    sudo apt-get install postgresql

Создание новой базы данных
--------------------------

Пусть имя новой базы данных будет `ncms`,  имя пользователя базы `ncms`

#. Становимся пользователем postgres: `sudo su - postgres`::

    psql

#. Создаем БД

.. code-block:: sql

    CREATE DATABASE ncms WITH ENCODING 'UTF8' LC_COLLATE='ru_RU.UTF-8' LC_CTYPE='ru_RU.UTF-8' TEMPLATE=template0;
    CREATE USER ncms WITH PASSWORD '<Пароль пользователя>';

    GRANT ALL PRIVILEGES ON DATABASE ncms TO ncms;

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
            JDBC.driver=org.postgresql.Driver
        </extra-properties>
        ...
    </mybatis>

.. warning::

    Убедитесь в том, что в элементе конфигурации `mybatis/extra-properties`
    присутствует JDBC драйвер для Postgres: `org.postgresql.Driver`

Где в файле `${HOME}/.ncmsapp.ds` хранятся JDBC URL, имя пользователя и пароль к БД::

    JDBC.url=jdbc:postgresql://127.0.0.1:5432/ncms
    JDBC.username=ncms
    JDBC.password=xxxxxx


