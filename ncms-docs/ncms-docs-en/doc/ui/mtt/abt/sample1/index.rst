.. _abt_sample1:

Example of A/B testing
======================

This example demonstrates the creation
of a simple website and the use of `A/B` testing
to site users.

With a probability of 50% to 50% the website shows users two pictures: `shark` and `dolphins`.
Selecting the display mode for user takes place at the first access to the site
and stores for 1 day. After that, the selection of display mode for the user is repeated.
The website is in a virtual domain: `foo.example.com`.

Sample pages of the site in the mode of `A`:

.. figure:: img/screen10.png
    :align: center

    Web site page mode `A`



Thus, let's create a file `abtest_core.htt` of a home page of the website
in the interface of media-repository:

.. code-block:: html

    <html>
    <body>
      <h1>${'title'.asm}</h1>

      #if(abt('A'))
      <img src="$!{((Image) asm('imageA')).link}"></img>
      #end

      #if(abt('B'))
      <img src="$!{((Image) asm('imageB')).link}"></img>
      #end
    </body>
    </html>


.. figure:: img/screen4.png
    :align: center

     File `abtest_core.htt` in the interface of media-repository

The following three :term:`attributes <attribute>` of assembly: `title`,` imageA`, `imageB` are
included to the `html` text of markup.
`abt` function is used to determine the current mode of the page display.
In this simple example, in a mode`A` where(``abt('A') == true``) it displays a picture `imageA`,
and, accordingly, `imageB` for` B` mode.


Then create a page template (assembly) for this markup called `absimple`
and define attributes of the assembly as shown in the screenshot below:


.. figure:: img/screen1.png
    :align: center

    Create page template

Create a page based on the template and load
the corresponding images to the page repository:

.. figure:: img/screen3.png
    :align: center

    Creating an instance of the page


Select the page created in the assembly management interface
and add the `mainpage` attribute, says ηCMS that
this page is the main for domain `foo.example.com`:


.. figure:: img/screen12.png
:align: center

   Designation of the main page for `foo.example.com` domain

At this stage, the creation of the site is completed. While opening the website via the preview
in admin zone, we see dolphins and shark simultaneously, because
in the administrator preview mode all `A/B` options are activated.
After publishing of the site, the pictures will not be displayed, because
there are no rules for choosing `A/B` options for public users.
This is what we are going to do on the following steps of a sample.


.. figure:: img/screen5.png
    :align: center

    Preview of the site through the admin zone, all the rules enabled


Further, using `MTT` console we define traffic-rule to include `A/B` modes
for the main page `foo.example.com`. We call the rule `docsing_abt1`
and indicate that the rule action is applied only for the domain name `foo.example.com`,
as shown in the screenshot below:

.. figure:: img/screen6.png
    :align: center

    Activation of the rule only for `foo.example.com`


It remains to determine parameters of enabling `A/B` modes.
To do this, add to the rule a `probability group`
where with a probability of 50/50 the different `A/B` modes will be activated:


.. figure:: img/screen8.png
    :align: center

    Configuration of the probabilities of `A/B` modes enabling


Here is the screen of editing `A/B`, mode `A`:


.. figure:: img/screen7.png
    :align: center

    Screen of editing `A/B` mode

It is worth noting that in the `A/B label` field arbitrary regimes which are to be enabled,
can be listed separated by commas, for example, `A, B, C, ...` or `Mode1, Mode2, ...`.
ηCMS imposes no restrictions for the number of options for split testing and names of these options.
Of course, they must be agreed with httl web site template code.


As a result, we have a website with a simple `A/B` testing at the level of the content :


.. figure:: img/screen11.png
    :align: center

    Website in `B` mode for external user












