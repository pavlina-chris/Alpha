.PHONY: all clean alcodoc
PYTHON ?= /usr/bin/python

all: jars/jcommander.jar
	${PYTHON} ./build.py	

jars/jcommander.jar:
	make -C jcommander
	mv jcommander/jcommander.jar jars/

check:
	${PYTHON} ./test.py

doc:
	cd docs; \
	pdflatex alpha.tex; \
	pdflatex alpha.tex

alcodoc:
	doxygen

clean:
	find src/java -name '*.class' -delete
	rm -f test/*.class test/*.o
	rm -f src/c/alco
	find jars -name '*.d' | xargs rm -rf
	rm -rf build alcodoc
	rm -rf alco.jar alco
	make -C jcommander clean
