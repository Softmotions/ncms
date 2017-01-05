.. _postgresql:

PostgreSQL
==========

ηCMS supports PostgreSQL 9.5 or higher

Installing PostgreSQL on Ubuntu
-------------------------------

.. code-block:: sh

    sudo apt-get install postgresql

Creating a new database
-----------------------

Let us set the name of the new database as `ncms` and the database user name - `ncms`

#. Login as postgres owner: `sudo su - postgres`::

    psql

#. Create the new database

.. code-block:: sql

    CREATE DATABASE ncms WITH ENCODING 'UTF8' LC_COLLATE='ru_RU.UTF-8' LC_CTYPE='ru_RU.UTF-8' TEMPLATE=template0;
    CREATE USER ncms WITH PASSWORD '<User password>';
    GRANT ALL PRIVILEGES ON DATABASE ncms TO ncms;

ηCMS Configuration
------------------

It is necessary to setup a correct JDBC driver to work with database.
Example of a configuration:

.. code-block:: xml

    <mybatis>
        <bindDatasource>true</bindDatasource>
        <config>com/softmotions/ncms/db/mybatis-config.xml</config>
        <propsFile>{home}/.ncms.ds</propsFile>
        <extra-properties>
            JDBC.driver=org.postgresql.Driver
        </extra-properties>
        ...
    </mybatis>

.. warning::

    Make sure that the `mybatis/extra-properties` configuration item contains
    the JDBC driver for Postgres: `org.postgresql.Driver`

Where  JDBC URL, user name and password are stored in the `${HOME}/.ncms.ds` file::

    JDBC.url=jdbc:postgresql://127.0.0.1:5432/ncms
    JDBC.username=ncms
    JDBC.password=xxxxxx
