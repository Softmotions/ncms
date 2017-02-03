.. _abt_sample2:

Example of A/B testing through pages inheritance
================================================

There is a significant drawback :ref:`in the previous A/B testing example <abt_sample1>`:
if the number of `A/B` modes is more than two, we need to defined attributes
specific for every `A/B` mode, it leads to a big set of similar attributes
and can be very heavy and awkward.

More convenient way is to implement `A/B` testing through page inheritance.
Create a base page for particular `A/B` mode and other `A/B` modes can be implemented
within pages which are inherited from a base page.

.. figure:: img/screen1.png

    Pages inheritance for `A/B/C` testing

Let's demonstrate this example in more detail.

Creating a base page
-------------------------

Base page markup:

.. code-block:: html

    <html>
    <body>

      <h2>${asm('title')}</h2>

      <img src="$!{((Image) asm('image1')).link}"></img>
      <br><br>
      <img src="$!{((Image) asm('image2')).link}"></img>

    </body>
    </html>

.. figure:: img/screen2.png

    Base page in the edit mode

Creating child pages
--------------------

Create two pages for modes `\B` and `\C` and specify the base page for the mode `\A`
as the parent page for them.

* Right-click `Create`:

.. image:: img/screen3.png


* Open the dialog to select a page template:

.. image:: img/screen4.png


* Select the tab `Structure` and the parent page `(A) foo.example.com`:

.. image:: img/screen5.png

* Change the page content in the child page in accordance with an mode `\B`. The page `title` and two images.

Similarly we proceed with the mode `\C`.

This approach to create different variants of pages for `A/B` modes,
is significantly more flexible then the use of different attributes for each option and
it is very simple to be implemented for relatively complex changes in every variant.

Setting MTT rules for pages
---------------------------

Let that the probabilities of A,B,C mode selection are equal.
Then routing rules can be configured as follows:

.. figure:: img/screen8.png

    Equal probabilities for A, B, C modes
