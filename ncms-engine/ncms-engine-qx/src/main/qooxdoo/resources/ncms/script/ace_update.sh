#!/usr/bin/env bash

set -x
set -e

ACE_BASE="https://raw.githubusercontent.com/ajaxorg/ace-builds/2268d21c5893320330c9daf10b70b1973ca45eba/src-min"

curl ${ACE_BASE}/ace.js > ./ace_all.js

echo -e "\n\n//mode css \n" >> ./ace_all.js
curl ${ACE_BASE}/mode-css.js >> ./ace_all.js

echo -e "\n\n//mode html \n" >> ./ace_all.js
curl ${ACE_BASE}/mode-html.js >> ./ace_all.js

echo -e "\n\n//mode javascript \n" >> ./ace_all.js
curl ${ACE_BASE}/mode-javascript.js >> ./ace_all.js

echo -e "\n\n//mode json \n" >> ./ace_all.js
curl ${ACE_BASE}/mode-json.js >> ./ace_all.js

echo -e "\n\n//mode jsp \n" >> ./ace_all.js
curl ${ACE_BASE}/mode-jsp.js >> ./ace_all.js

echo -e "\n\n//mode markdown \n" >> ./ace_all.js
curl ${ACE_BASE}/mode-markdown.js >> ./ace_all.js

echo -e "\n\n//mode xml \n" >> ./ace_all.js
curl ${ACE_BASE}/mode-xml.js >> ./ace_all.js

echo -e "\n\n//mode sass \n" >> ./ace_all.js
curl ${ACE_BASE}/mode-sass.js >> ./ace_all.js

echo -e "\n\n//mode scss \n" >> ./ace_all.js
curl ${ACE_BASE}/mode-scss.js >> ./ace_all.js

echo -e "\n\n//mode python \n" >> ./ace_all.js
curl ${ACE_BASE}/mode-python.js >> ./ace_all.js

echo -e "\n\n//theme eclipse \n" >> ./ace_all.js
curl ${ACE_BASE}/theme-eclipse.js >> ./ace_all.js

echo -e "\n\n//ext-language_tools \n" >> ./ace_all.js
curl ${ACE_BASE}/ext-language_tools.js >> ./ace_all.js

echo -e "\n\n//ext-searchbox \n" >> ./ace_all.js
curl ${ACE_BASE}/ext-searchbox.js >> ./ace_all.js

curl ${ACE_BASE}/worker-css.js > ./worker-css.js
curl ${ACE_BASE}/worker-html.js > ./worker-html.js
curl ${ACE_BASE}/worker-javascript.js > ./worker-javascript.js
curl ${ACE_BASE}/worker-json.js > ./worker-json.js
curl ${ACE_BASE}/worker-xml.js > ./worker-xml.js







