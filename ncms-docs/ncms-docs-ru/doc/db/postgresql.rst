.. _postgresql:

PostgreSQL
==========

nCMS поддерживает PostgreSQL версии не ниже `9.5`

Установка PostgreSQL на Ubuntu
------------------------------

#. Становимся рутом `sudo su -`::

     apt-get install postgresql
#. Добавьте себя в группу: `postgres` - это необходимо для проведения тестов БД

Создание новой базы данных
--------------------------

Пусть имя новой базы данных будет `ncms`  имя пользователя базы `ncms`

#. Становимся пользователем postgres: `sudo su - postgres`::

    psql

#. Создаем БД

.. code-block:: sql

    CREATE DATABASE ncms WITH ENCODING 'UTF8' LC_COLLATE='ru_RU.UTF-8' LC_CTYPE='ru_RU.UTF-8' TEMPLATE=template0;
    CREATE USER ncms WITH PASSWORD '<Пароль пользователя>';

    GRANT ALL PRIVILEGES ON DATABASE ncms TO ncms;

