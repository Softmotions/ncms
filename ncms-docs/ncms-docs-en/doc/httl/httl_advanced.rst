.. _httl_advanced:

Advanced HTTL
=============

Using HTTL in CSS
-----------------

Sometimes it's required to include a request dependent conditional logic into CSS.
This can be done as shown below:

HTTL template:

.. code-block:: html

    <html>
    <head>
        ...
        <link href="css/site.httl.css?style=black" rel="stylesheet" type="text/css"/>
    </head>
    ...
    </html>

There css resource is called with a query parameter `?style = black`, which is processed while
generating the file `site.httl.css` as a template.


`css/site.httl.css` file:

.. code-block:: css

    .main {
        #if(ifRequestParameter('style', 'black'))
          color: black;
        #else
          color: blue;
        #end
    }

Here is a conditional HTTL directive `#if` and ηCMS method to check the value
of the request parameter: `ifRequestParameter`. To provide the `css` file
to be processed as a template, specify its extension as `.httl.css`.


.. warning::

     Usage dynamic logic inside `css` is not a recommended way
     of conditional stylization of pages. Firstly, it is impossible
     to use compressed css files, and secondly, css markup
     becomes mixed with HTTL and therefore it will be more complex.
     A simpler and perhaps more appropriate method would be the usage
     of different `css` files for different page styles
     and their conditional inclusion to the pages.

.. _httl_inheritance:

Inheritance of HTTL patterns
----------------------------

.. todo::

    TODO