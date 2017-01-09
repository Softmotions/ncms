.. _db2:

IBM DB2
=======

ηCMS supports IBM DB2 9.7 or higher

DB2 Installation
----------------
IBM DB2 Express-C is a free powerful DBMS. It is enough to run a single copy of ηCMS
for dozens of complex sites. To install and configure DB2 refer to the `IBM documentation <http://www.ibm.com/support/knowledgecenter/SSEPGG_11.1.0/com.ibm.db2.luw.welcome.doc/doc/welcome.html>`_.

Below are steps needed to install DB2 Express-C `v11.1` on Ubuntu Linux

Installing DB2 on Ubuntu
************************

#. It is assumed that the Ubuntu (x64) system has the 16.x version
#. We use the `v11.1_linuxx64_expc.tar.gz` distribution kit
#. Login as root `sudo su -`::

     apt-get install libstdc++6:i386 libpam0g:i386 \
             libstdc++6 lib32stdc++6 \
             libaio1 gcc ksh numactl

#. Install an instance of DB2::

    cd ./expc
    ./db2_install
    useradd -m db2inst1 -s /bin/bash
    useradd -m db2fenc1
    /opt/ibm/db2/V11.1/instance/db2icrt -u db2fenc1 db2inst1

#. Add yourself to the linux user group: `db2inst1`
#. Set the database instance operation flags required to work with ηCMS::

    sudo su - db2inst1
    db2set DB2_COMPATIBILITY_VECTOR=4000
    db2stop
    db2start



Creating database
*****************

#. On this step, we need to setup the database user.
   Let it be `ncms`::

    useradd -m ncms
    passwd ncms
    # Password of the ncms user to access the database

Creating a new database
-----------------------

Let us set the name of the new database as `NCMS` and the database user name - `ncms`

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


ηCMS configuration
------------------

It is necessary to setup a correct JDBC driver to work with database.
Configuration example:

.. code-block:: xml

    <mybatis>
        <bindDatasource>true</bindDatasource>
        <config>com/softmotions/ncms/db/mybatis-config.xml</config>
        <propsFile>{home}/.ncms.ds</propsFile>
        <extra-properties>
            JDBC.driver=com.ibm.db2.jcc.DB2Driver
        </extra-properties>
        ...
    </mybatis>

.. warning::

    Make sure that the `mybatis/extra-properties` configuration item contains
    the JDBC driver for DB2: `com.ibm.db2.jcc.DB2Driver`

Where JDBC URL, user name and password are stored in the `${HOME}/.ncms.ds` file::

    JDBC.url=jdbc:db2://127.0.0.1:50000/NCMS
    JDBC.username=ncms
    JDBC.password=xxxxxx

