.. _onvhost_deployment:

Deployment as an independent service
====================================

:ref:`A new ηCMS web application <newproject>` will start
at the root `\/` of Apache Tomcat server.

In production mode, this service can be proxied under `Apache` or `Nginx` web server.
Note that ηCMS supports the virtual hosts, therefore,
the system can handle requests for multiple sites using either no external proxy or a simple traffic proxy.


