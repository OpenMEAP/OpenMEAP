#!/bin/bash 

JS_LOC=../../../../clients/shared/javascript
versions="1a 2a 3a"

function clean() {
for ver in $versions; do
	if [ -e version-0.0.$ver.zip ]; then
		rm -f version-0.0.$ver.zip
		rm -f version-0.0.$ver/js/openmeap.js
	fi;
done;
}

function make() {
for ver in $versions; do
	cd version-0.0.$ver && \
		cp $JS_LOC/openmeap.js js
		zip -qr ../tmp.zip * && \
		cd .. && \
		mv tmp.zip version-0.0.$ver.zip
done;
}

case "$1" in
clean)
clean;
;;
*)
clean; make;
;;
esac
