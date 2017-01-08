.. _onpath_deployment:

ηCMS deployment in the context of another site
==============================================

In this deployment scheme the ηCMS system is supposed to be
in the context of other website under some URL suffix.

.. figure:: img/ncms-onpath-deployment.png
    :align: center

    ηCMS in the context of the main site

* Incoming traffic will be proxied by `nginx` web server and redirected either
  to the main web site or to ηCMS under the prefix `/<ncms_prefix>` depending on the context.
* All resources for `http://example.com/<ncms_prefix>/*` are processed by ηCMS.
* All other resources are handled by the main site.

.. warning::

    This configuration of ηCMS deployment is not recommended for a work
    with MTT filters or A/B testing, because it is required
    to take account of the context where the ηCMS resource
    (`http://example.com/<ncms_prefix>/*`) is used.

Nginx configuration
-------------------

Below is a sample of a `nginx` configuration for this deployment mode.
Here the `rewrite` rule redirects the traffic to the ηCMS instance  passing the `/ ncms_prefix /*`

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


ηCMS configuration
------------------

The main file of the ηCMS configuration should contain
a directive `app-prefix` which should be set to a correct path where ηCMS works:

.. code-block:: xml

    <app-prefix>/ncms_prefix</app-prefix>
    <security>
        ...
        <shiro-config-locations>/WEB-INF/shiro.ini</shiro-config-locations>
        ...
    </security>

Also it is necessary to change the `Apache Shiro` configuration, the link passing to it contains the
`security/shiro-config-locations` item.

Add `ncms_prefix` to the paths specified in the configuration shiro:

.. code-block:: ini

    [main]

    authc.successUrl = /ncms_prefix/adm/

    [urls]

    /ncms_prefix/rs/media/**    = authcBasic[POST,PUT,DELETE]
    /ncms_prefix/rs/adm/**      = authcBasic
    /ncms_prefix/adm/**         = authcBasic


Following the steps above, the ηCMS root is available in the context of
example.com site at `http://example.com/ncms_prefix/`.






