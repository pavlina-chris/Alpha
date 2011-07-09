.PHONY: all clean alcodoc
PYTHON ?= /usr/bin/python

all: jars/jcommander.jar
	${PYTHON} ./build.py	

jars/jcommander.jar:
	make -C jcommander
	mv jcommander/jcommander.jar jars/

alcodoc:
	doxygen

clean:
	find src/java -name '*.class' -delete
	rm -f src/c/alco
	find jars -name '*.d' | xargs rm -rf
	rm -rf build alcodoc
	rm -rf alco.jar alco
	make -C jcommander clean
