.. _extending:

Extending ηCMS
==============

ηCMS system is created as a :ref:`new a web project <newproject>` based on Java platform.
Thereby, an ηCMS user has a wide range of possibilities for farther project development
and can extend every part of the platform.

The name of our project will be `myncms`,
so the project structure will be like this::

    myncms
    │
    ├── qx/
    │   ├── src/...
    |   |      main/qooxdoo/classes/myncms/Application.js
    │   └── pom.xml
    ├── tomcat/
    │   ├── context.xml
    │   └── server.xml
    ├── web/
    │   ├── src/...
    |   |      main/java/com/myncms/AppModule.java
    |   |      main/java/com/myncms/AppHttlMethods.java
    |   |      main/java/com/myncms/AppBoot.java
    │   └── pom.xml
    ├── pom.xml
    └── README.md



After creating a new project, the new `Guice <https://github.com/google/guice>`_
module is created. It's possible to register custom project specific modules in its context:

.. code-block:: java

    package com.myncms;

    import com.google.inject.AbstractModule;
    import com.google.inject.Singleton;
    import com.softmotions.weboot.WBConfiguration;

    /**
     * Custom application Guice module
     */
    public class AppModule extends AbstractModule {

        private final WBConfiguration cfg;

        public AppModule(WBConfiguration cfg) {
            this.cfg = cfg;
        }

        @Override
        protected void configure() {
            // Register your modules here
        }
    }

While generating a new project, a blank custom :ref:`HTTL <httl>` module is created.
Its static methods are available in all httl-templates:

.. code-block:: java

    package com.myncms;

    public class AppHttlMethods {

        private AppHttlMethods() {
        }

        public static String helloFromHttl(String name) {
            return "Hello " + name;
        }
    }

Using of `helloFromHttl` in HTTL temlplates:

.. code-block:: html

    <!-- The output of the string: Hello Andy -->
    ${helloFromHttl('Andy')}

A client part of the ηCMS GUI is implemented
with Qooxdoo framework http://qooxdoo.org

The main class of ηCMS GUI Qooxdoo application is defined by a
file `src/main/qooxdoo/classes/myncms/Application.js` in the `qx` subproject.

.. code-block:: js

    /**
     * App site application.
     */
    qx.Class.define("myncms.Application", {
        extend: ncms.Application,

        members: {

            main: function () {
                this.base(arguments);
            },

            createActions: function () {
                return new myncms.Actions();
            }
        }
    });

In this starting point of the ηCMS GUI application (`myncms.Application`)
you can create and register custom Qooxdoo elements,
extending and customizing the functionality of the GUI.

