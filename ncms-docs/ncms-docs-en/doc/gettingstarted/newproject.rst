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
    cases are designed to run only on linux platforms.
    We highly recommend to run |ncmsversion| on Linux in production.

Using Maven Archetype
---------------------

The best way to create a new ηCMS project is to create
it from the maven archetype.

.. code-block:: sh

    mvn archetype:generate \
        -DarchetypeGroupId=softmotions \
        -DarchetypeArtifactId=ncms-site-archetype \
        -DarchetypeVersion=1.0.1 \
        -DarchetypeRepository=https://repo.softmotions.com/repository/softmotions-public

Let's consider a creation and developing of the ηCMS project
using the website about parrots as a sample. The java code of this project resides
in package `org.myparrots`.

.. code-block:: text

    $:   mvn archetype:generate \
    >         -DarchetypeGroupId=softmotions \
    >         -DarchetypeArtifactId=ncms-site-archetype \
    >         -DarchetypeVersion=1.0.1 \
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

As a result of `mvn archetype:generate` we got the project
with the following structure:

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

* `qx` - The source code of the administrative GUI interface, it is based on `javascript library qooxdoo <http://qooxdoo.org>`_
* `web` - The server side web resources and project's java code
* `tomcat` - Configuration files of the Apache Tomcat server to start the server
  in the development mode using maven cargo plugin

Choosing and configuring the database connection
------------------------------------------------

Before running the application we need to setup configuration of the database connection.

|ncmsversion| version supports the following database systems:

* :ref:`db2`
* :ref:`postgresql`


All application configuration parameters alongside with database connection
parameters are reside in the :ref:`configuration files <conf>`.
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

Parameters of the connection to the database are defined in the file `{home}/.ncmsapp.ds`,
where `{home}` is a home directory of user who runs the ηCMS server. It is required to create the file
and fill it with the parameters like in the sample below:


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
