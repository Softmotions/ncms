.. _onpath_deployment:

Deployment within a context of another website
==============================================

In this deployment scheme the ηCMS system is supposed to be
in the context of another website under some URL suffix.

.. figure:: img/ncms-onpath-deployment.png
    :align: center

    ηCMS in the context of the main site

* Incoming traffic will be proxied by `nginx` web server and redirected either
  to the main web site or to ηCMS under the prefix `/<ncms_prefix>` depending on the context.
* All resources for `http://example.com/<ncms_prefix>/*` are processed by ηCMS.
* All other resources are handled by the main site.

.. warning::

    It is not recommended to use this deployment scheme in conjunction with
    MTT filters or A/B testing, because it is required
    to take into account the context where the ηCMS resource
    (`http://example.com/<ncms_prefix>/*`) is used.

Nginx configuration
-------------------

Below is an example of an `nginx` configuration for this deployment mode.
the `rewrite` rule redirects traffic to the ηCMS instance passing the `/ ncms_prefix /*`

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
a directive `app-prefix` which should be set to a correct path where the ηCMS root is located:

.. code-block:: xml

    <app-prefix>/ncms_prefix</app-prefix>
    <security>
        ...
        <shiro-config-locations>/WEB-INF/shiro.ini</shiro-config-locations>
        ...
    </security>

Also it is necessary to change the `Apache Shiro` configuration, specified in
`security/shiro-config-locations` configuration item.

Add `ncms_prefix` to the paths specified in the shiro configuration:

.. code-block:: ini

    [main]

    authc.successUrl = /ncms_prefix/adm/

    [urls]

    /ncms_prefix/rs/media/**    = authcBasic[POST,PUT,DELETE]
    /ncms_prefix/rs/adm/**      = authcBasic
    /ncms_prefix/adm/**         = authcBasic


After all the steps above are done, the ηCMS root is available in the context of
example.com site at `http://example.com/ncms_prefix/`.






