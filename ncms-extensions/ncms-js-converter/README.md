Integration of Google Closure JS compiler and nCMS
==================================================

This module joins and minimizes scripts specified in httl templates 
and produces links to compiled scripts. Also it provides js syntax checking
in the nCMS code editor.
 
In HTTL templates
-----------------

The snippet:
```html
<html>
 <body>
    ...
    ${jsScript(['path/to/js1.js', 'path/to/js2/js'], 
               ['in':'es6', 'out':'es5', 'level': 'simple'])}
 </body>
</html>
```

will produce a markup like this:
```html
<html>
 <body> 
    ...
    <script type="application/javascript" src="/rs/x/js/script/be1914e01d9917362786ab1ee1996448.js"></script>
 </body>
</html>
```

The file `/rs/x/js/script/be1914e01d9917362786ab1ee1996448.js` is a closure compilation result 
of `path/to/js1.js` and `path/to/js2/js` with input language level of scripts: `es6` and a final script will 
has `es5` language level. 

* Compiled scripts are cached by this module.
* Any changes in sources scripts or compiler settings will cause scripts recompilation.   
                
Global settings in nCMS config file           
-----------------------------------
          
```xml
<configuration>
  <media>
    <js>
      <!-- Max number of days to wait before cleanup 
           of a compilation results what are no longer used.
           (30 days by default)             
      -->  
      <forgotten-scripts-max-life-days>30</forgotten-scripts-max-life-days>
    </js>
  </media>
</configuration>
```          
