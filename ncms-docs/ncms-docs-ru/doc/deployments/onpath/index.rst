.. _onpath_deployment:

ηCMS в контексте другого сайта
==============================

В данной схеме развертывания предполагается работа ηCMS
в контексте другого сайта под некоторым префиксом.

.. figure:: img/ncms-onpath-deployment.png
    :align: center

    ηCMS в контексте основного сайта


* Входящий трафик проксируется веб сервером `nginx` и, в зависимости от контекста, перенаправляется
  либо на основной сайт, либо на ηCMS ресурсы, находящиеся под префиксом `/<ncms_prefix>`.
* Все ресурсы за путем `http://example.com/<ncms_prefix>/*` обрабатываются ηCMS.
* Все другие ресурсы обрабатываются основным сайтом.

.. warning::

    Подобная конфигурация развертывания ηCMS не рекомендуется для работы
    с MTT фильтрами трафика и A/B тестирования, поскольку при создании
    MTT правил необходимо всегда учитывать, в каком контексте находятся
    ресурсы ηCMS (`http://example.com/<ncms_prefix>/*`).

Конфигурация nginx
------------------

Ниже приведен пример `nginx` конфигурации для данного режима развертывания.
Здесь `rewrite` правило перенаправляет трафик за `/ncms_prefix/*`
на экземпляр ηCMS:

.. code-block:: nginx

     server {
        listen 80 default_server;
        server_name example.com;

        root /var/www/html;
        index index.html;

        location /ncms_prefix {
            rewrite             ^(/ncms_prefix)$ $1/ break;
            proxy_pass		    http://localhost:9191;
            proxy_set_header	Host	$host;
            proxy_set_header	X-Real-IP	$remote_addr;
        }
     }


Конфигурация ηCMS
-----------------

Основной файл конфигурации ηCMS должен
содержать директиву `app-prefix`, настроенную на корректный
путь, под которым работает ηCMS:

.. code-block:: xml

    <app-prefix>/ncms_prefix</app-prefix>
    <security>
        ...
        <shiro-config-locations>/WEB-INF/shiro.ini</shiro-config-locations>
        ...
    </security>

Также следует поменять конфигурацию `Apache Shiro`, ссылку на которую содержит
элемент `security/shiro-config-locations`.

Добавляем `ncms_prefix` в пути, перечисленные в конфигурации shiro:

.. code-block:: ini

    [main]

    authc.successUrl = /ncms_prefix/adm/

    [urls]

    /ncms_prefix/rs/media/**    = authcBasic[POST,PUT,DELETE]
    /ncms_prefix/rs/adm/**      = authcBasic
    /ncms_prefix/adm/**         = authcBasic


После перечисленных выше действий корень ηCMS будет доступен в контексте
сайта example.com по адресу `http://example.com/ncms_prefix/`.










