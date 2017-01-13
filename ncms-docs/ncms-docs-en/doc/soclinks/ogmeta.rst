.. _ogmeta:

Support of Open Graph protocol
==============================

Meta-information added to a page in conform to `open graph <http://ogp.me>`_ protocol
allows you to integrate a content of a page to a social graph. In other words,
by clicking a "Share" button for the page it will be correctly displayed for social networks
like Facebook.

To generate a social meta-information for a page add a method call `ogmeta` inside a `head` html tag:

.. code-block:: html

    <head>
      $!{ogmeta()}
      ...
    </head>

Calling `ogmeta` with the more specific parameters:

.. code-block:: html

    <head>
      $!{ogmeta(["title":"Test page", "image":"some_image_ref", ...])}
      ...
    </head>

As a result, the page will contain the html markup like this:

.. code-block:: html

    <head>
      <meta property="og:site_name" content="site.com"/>
      <meta property="og:image" content="http://site.com/rs/media/fileid/323"/>
      <meta property="og:title" content="Test page"/>
      <meta property="og:locale" content="en"/>
      <meta property="og:type" content="article"/>
      <meta property="og:url" content="http://site.com/news/123"/>
      ...
    </head>

The default ogmeta fields:

* `og:url` - A page URL. Cannot be overridden.
* `og:site_name` - A site virtual host name. Cannot be overridden.
* `og:locale` - A locale of http request. Cannot be overridden.
* `og:title` - A page title, the default is `page.hname`. It may be overridden by `title` argument passed to `ogmeta` method.
* `og:type` - Page type, the default is `article`. It may be overridden by `type` argument passed to `ogmeta` method.
* `og:image` - An image banner associated with page. It will be used if the `image` argument is specified.
  The value of an argument may be a name of `image <am_image>` assembly attribute
  or path to an image file in media repository.

Any other named parameters passed in the call of `ogmeta`, will be added "as is",
with the prefix `og:`.
