.. _postgresql:

PostgreSQL
==========

ηCMS supports PostgreSQL 9.6 or higher

Installing PostgreSQL on Ubuntu
-------------------------------

.. code-block:: sh

    sudo apt-get install postgresql

Creating a new database
-----------------------

Let us set the name of the new database as `ncms` and the database user name: `ncms`

#. Login as postgres owner: `sudo su - postgres`::

    psql

#. Create a new database

.. code-block:: sql

    CREATE DATABASE ncms WITH ENCODING 'UTF8' TEMPLATE=template0;
    CREATE USER ncms WITH PASSWORD '<User password>';
    GRANT ALL PRIVILEGES ON DATABASE ncms TO ncms;

ηCMS Configuration
------------------

It is necessary to setup a correct JDBC driver to work with the postgres database.
Configuration example:

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

    Make sure that the `mybatis/extra-properties` configuration item contains
    the JDBC driver for Postgres: `org.postgresql.Driver`

JDBC URL, user name and password are stored in property file, it is placed at `${HOME}/.ncmsapp.ds` in the example above::

    JDBC.url=jdbc:postgresql://127.0.0.1:5432/ncms
    JDBC.username=ncms
    JDBC.password=xxxxxx

