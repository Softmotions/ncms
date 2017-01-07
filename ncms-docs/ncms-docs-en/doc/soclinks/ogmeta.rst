.. _ogmeta:

Page integration with social media services
===========================================

Meta-information added to a page in conform to `open graph <http://ogp.me>`_ protocol
allows you to integrate a content of a page to the social graph. In other words,
by clicking a "Share" button for the page it will be correctly displayed for social services
like Facebook.

To generate a social meta-information for a page add a method call `ogmeta` inside the `head` tag:

.. code-block:: html

    <head>
      $!{ogmeta()}
      ...
    </head>

Or with with more specific parameters:

.. code-block:: html

    <head>
      $!{ogmeta(["title":"Test page", "image":"some_image_ref", ...])}
      ...
    </head>

As a result, the page will contain the html markup like the this:

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

The fist of fields generated automatically:

* `og:url` - Page URL. Cannot be overridden.
* `og:site_name` - A site's virtual host name. Cannot be overridden.
* `og:locale` - Locale of http request. Cannot be overridden.
* `og:title` - Page title, the default is `page.hname`. It may be overridden by `title` argument passed to `ogmeta`.
* `og:type` - Page type, the default is `article`. It may be overridden by `type` argument passed to `ogmeta`.
* `og:image` - Page image banner. it will be used if the `image` argument is specified. The argument's value
   may be a name of `image <am_image>` assembly attribute or path to image file in media repository.

Any other named parameters passed in the call of `ogmeta`, will be added "as is",
with the prefix `og:`.
