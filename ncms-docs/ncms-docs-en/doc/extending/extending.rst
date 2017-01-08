.. _extending:

Extending ηCMS functionality
============================

ηCMS system is created as a :ref:`new a web project <newproject>` based on Java platform.
Thereby, an ηCMS user has the wide possibilities for farther project development
and can extend every part of the platform.

Let the name of our project will be `myncms`,
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



After the creation a new project, the new `Guice <https://github.com/google/guice>`_
module is created. In its context it's possible to register custom modules, specific to the
project:

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

When generating a new project, a blank custom :ref:`HTTL <httl>` module is created.
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

The client part of the ηCMS GUI is implemented
using the Qooxdoo framework http://qooxdoo.org

The main class of the ηCMS GUI qooxdoo application is defined by the
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
you can create and register custom qooxdoo elements,
extending and customizing the functionality of the GUI.

