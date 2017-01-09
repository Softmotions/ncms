.. _am_webref:

Web resource (webref)
=====================

This attribute allows to include a content of remote
resource to a page using `http`,`https`, `ftp` protocols.

Attribute options
-----------------

.. figure:: img/webref_img1.png

    Attribute options

================================== =============
Option                             Description
================================== =============
**Location**                       Default remote resource URL.
**Display as location**            While the switch is turned on, the attribute value
                                   is the address (URL) of the resource.
                                   Otherwise, the attribute value is the text content of the resource.
================================== =============

Edit mode
---------

.. figure:: img/webref_img2.png

    Web resource URL on the :ref:`page management UI <pmgr>`

In this example, a page address
http://lib.ru/AKONANDOJL/doil1_5.txt was specified as a resource

Using in the markup
-------------------

**Type of attribute value:** `java.lang.String`.

Including a resource text to the html page::

    Webref:

    $!{asm('webref')}

Using the mentioned address of a resource, as a result we get:

.. image:: img/webref_img3.png


Limitations
-----------

* In the |ncmsversion| the maximum upload file size is limited to `1Mb`.
* For security reasons, a loading of local files and the classpath elements is disabled.
* When loading http resources the caching is in use. The maximum size of documents
  to be stored in the cache: `128Kb`. Maximum number of cached documents: `512`.
* Timeout for connection to the remote resource: `10 sec`.



