Starting from SoundTouch 1.6.0, the "configure" file is removed from the source code package due to autoconf/automake version conflicts.

Instead, generate the "configure" file using local tools by invoking "./bootstrap" script, then configure & compile as usual.
