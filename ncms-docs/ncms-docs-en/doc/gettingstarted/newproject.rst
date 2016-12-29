.. _newproject:

.. contents::

Creating a new project
======================

Prerequisites
-------------

* JDK `1.8`
* Maven 3.3.x

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

Let's consider a creation and further evolution of the ηCMS project
using the creation of an information site about parrots as a sample.
The site code is in the package `org.myparrots`.

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

As a result of `mvn archetype: generate` we got the project
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

* `qx` - administrative GUI interface of the site based on `javascript library qooxdoo <http://qooxdoo.org>`_
* `web` - business logics of sites on the server side
* `tomcat` - configuration files of the Apache Tomcat server to start the server in the test mode

Choosing and configuring the database connection
------------------------------------------------

Before running the system in the project configuration,
it is required to set parameters of the database connection.

 |ncmsversion| version supports the following databases:

* :ref:`db2`
* :ref:`postgresql`

Parameters used to connect the application to the database and some other application
parameters are defined by the :ref:`configuration files <conf>`.
In the current project it is the file: `conf/ncmsapp-dev-configuration.xml`.


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

Additional parameters of the connection to the database are to be set in the file `{home}/.ncmsapp.ds`,
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
