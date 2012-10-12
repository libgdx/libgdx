NAME=pax-britannica
APPNAME=PaxBritannica

PLATFORMS=linux macosx mingw

help:
	@echo No platform given, printing help :D
	@echo
	@cat compiling.txt

$(PLATFORMS):
	make -C dokidoki-support $@ \
		NAME="../$(NAME)" \
		EXTRA_CFLAGS="-DEXTRA_LOADERS=\"../extra_loaders.h\" $(EXTRA_CFLAGS)" \
		EXTRA_OBJECTS="../particles.o"

clean:
	rm -f particles.o
	make -C dokidoki-support clean NAME="../$(NAME)"

$(APPNAME).app: macosx Info.plist
	rm -rf $@
	mkdir -p $@
	mkdir -p $@/Contents
	mkdir -p $@/Contents/MacOS
	mkdir -p $@/Contents/Resources
	mkdir -p $@/Contents/Frameworks
	cp Info.plist $@/Contents/
	cp $(NAME) $@/Contents/MacOS/$(APPNAME)
	# dokidoki
	cp -r dokidoki $@/Contents/Resources/dokidoki
	# lua files
	cp *.lua $@/Contents/Resources
	mkdir -p $@/Contents/Resources/components
	cp components/*.lua $@/Contents/Resources/components
	mkdir -p $@/Contents/Resources/scripts
	cp scripts/*.lua $@/Contents/Resources/scripts
	# resources
	mkdir -p $@/Contents/Resources/sprites
	cp sprites/*.png $@/Contents/Resources/sprites
	mkdir -p $@/Contents/Resources/audio
	cp audio/music.ogg $@/Contents/Resources/audio

$(APPNAME).dmg: $(APPNAME).app
	rm -rf $@
	hdiutil create -srcfolder $^ $@
