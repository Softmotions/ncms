.. _newproject:

.. contents::

Creating a new project
======================

Prerequisites
-------------

* JDK `1.8`
* Maven `3.3.x`
* Python `2.7` installed (Needed to build the :ref:`admin GUI <ui>`)
* Linux or Windows

.. note::

    You can run ηCMS on Windows, but the |ncmsversion| test
    cases are designed to run only on Linux platforms.
    We highly recommend to run |ncmsversion| on Linux in production.

Using Maven Archetype
---------------------

The best way to create a new ηCMS project is to create
it from the maven archetype.

.. code-block:: sh

    mvn org.apache.maven.plugins:maven-archetype-plugin:2.4:generate -U \
        -DarchetypeGroupId=softmotions \
        -DarchetypeArtifactId=ncms-site-archetype \
        -DarchetypeVersion=1.1-SNAPSHOT \
        -DarchetypeRepository=https://repo.softmotions.com/repository/softmotions-public


Alternatively you can run:

.. code-block:: sh

    mvn org.apache.maven.plugins:maven-archetype-plugin:2.4:generate \
        -DarchetypeCatalog=https://softmotions.com/rs/media/public/1542/archetype-catalog.xml


Let's consider the creation and development of the ηCMS project
using the website about parrots as an example. The Java code of this project resides
in package `org.myparrots`.

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

Project structure
-----------------

As a result of `mvn archetype:generate` we got the following structure:

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


Where:

* `qx` - The source code of the administrative GUI interface, is based on `javascript library qooxdoo <http://qooxdoo.org>`_
* `web` - The server side web resources and project's Java code
* `tomcat` - Apache Tomcat configuration files start the server
  in the development mode using maven cargo plugin

Configuring the database connection
------------------------------------------------

Before running the application we have to setup configuration of the database connection.

|ncmsversion| version supports the following database systems:

* :ref:`db2`
* :ref:`postgresql`


All application configuration parameters as well as database connection
parameters are located in the :ref:`configuration files <conf>`.
In our project it will be: `conf/ncmsapp-dev-configuration.xml`.


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

Database connection parameters are defined in the `{home}/.ncmsapp.ds` file ,
where `{home}` is a home directory of users who run the ηCMS server. You must create the file
and specify the parameters within it:


.. code-block:: sh

    cat ~/.ncmsapp.ds

For DB2:

.. code-block:: sh

    JDBC.url=jdbc:db2://127.0.0.1:50000/NCMS
    JDBC.username=ncms
    JDBC.password=*******

For PostgreSQL:

.. code-block:: sh

    JDBC.url=jdbc:postgresql://127.0.0.1:5432/ncms
    JDBC.username=ncms
    JDBC.password=*******

Building and running the project
--------------------------------

.. code-block:: sh

    mvn clean verify && mvn -Pcargo.run

The admin zone will be available at: `http://localhost:<port>/adm/`
Username: `admin`  and password is stored in `conf/ncmapp-users.xml`:


New ηCMS project on Youtube
---------------------------
..  youtube:: nPIFHWlNcC0
    :width: 100%
