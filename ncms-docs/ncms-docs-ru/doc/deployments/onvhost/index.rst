.. _onvhost_deployment:

ηCMS как самостоятельный сервис
===============================


ηCMS является веб приложением и при :ref:`создании нового проекта <newproject>`
запускается в корне `/` сервера Apache Tomcat.

В продакшн режиме этот сервис может быть запроксирован с помошью `Apache HTTP` веб сервера
или сервера `Nginx`. Заметим, что в ηCMS включена поддержка виртуальных хостов, соответственно,
данная система может обслуживать запросы для множества сайтов как без внешнего проксирования,
так и с простым проксированием трафика.



