.. _extending:

Расширение функционала ηCMS
===========================

ηCMS инсталлируется в форме :ref:`создания проекта <newproject>`
веб приложения на java платформе. Тем самым, пользователь ηCMS
с самого начала работы с этой системой имеет самые широкие
возможности расширения функционала веб сайтов, построенных на этой системе.

Структура нового ηCMS проекта в случае, если имя проекта `myncms`::

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


После создания нового проекта создается `Guice <https://github.com/google/guice>`_
модуль, в контексте которого можно регистрировать кастомные модули, специфичные для вашего
проекта:

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

При генерации нового проекта создается заготовка кастомного :ref:`HTTL <httl>` модуля, статические методы которого
доступны в шаблонах ваших веб сайтов:


.. code-block:: java

    package com.myncms;

    public class AppHttlMethods {

        private AppHttlMethods() {
        }

        public static String helloFromHttl(String name) {
            return "Hello " + name;
        }
    }

Использование `helloFromHttl` в HTTL шаблонах:

.. code-block:: html

    <!-- Вывод строки: Hello Andy -->
    ${helloFromHttl('Andy')}


Клиентская часть административной зоны ηCMS реализована
на базе javascript фреймворка Qooxdoo http://qooxdoo.org.

Стартовый класс qooxdoo приложения административной зоны ηCMS
определен в подпроекте `qx` в файле `src/main/qooxdoo/classes/myncms/Application.js`.
В случае, если ваш проект назван `myncms`:

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

В данной стартовой точке административного GUI вашего
приложения (`myncms.Application`) вы можете создавать
и регистрировать кастомные qooxdoo элементы, расширяя и настраивая
функционал административной зоны ηCMS.
