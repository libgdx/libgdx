Libgdx gradle setup
===================

Modular setup powered by gradle, allowing any combination of sub projects and official extensions to get you up and running in a few clicks.  Although this tool will handle setup for you, LEARN GRADLE!

![Setup Ui](http://i.imgur.com/7fUXXM3.png)

Example of use:

```java
DependencyBank bank = new DependencyBank();

ProjectBuilder builder = new ProjectBuilder();
List<ProjectType> modules = new ArrayList<ProjectType>();
modules.add(ProjectType.CORE);
modules.add(ProjectType.DESKTOP);
modules.add(ProjectType.ANDROID);
modules.add(ProjectType.IOS);
// Gwt has no friends
//modules.add(ProjectType.GWT);

List<Dependency> dependencies = new ArrayList<Dependency>();
dependencies.add(bank.getDependency(ProjectDependency.GDX));
dependencies.add(bank.getDependency(ProjectDependency.BULLET));
dependencies.add(bank.getDependency(ProjectDependency.FREETYPE));

List<String> incompatList = builder.buildProject(modules, dependencies);
//incompatList is a list of strings if there are incompatibilities found.
// The setup ui checks for these and pops up a dialog.
```


The builder will generate the settings.gradle, build.gradle file, as well as alter all the platform specific files that reference dependencies/assets.

Files Altered:
* settings.gradle
* build.gradle
* GdxDefinition.gwt.xml
* GdxDefinitionSuperDev.gwt.xml
* robovm.xml
* desktop/build.gradle (for eclipse task)

Modular setup classes
=====================

* BuildScriptHelper - Helper class for writing the build.gradle script to file.
* Dependency - Holds all the information for a dependency for all platforms
* ProjectBuilder - The project builder, manages the writers and temporary files
* DependencyBank - The bank for all supported sub modules, and dependencies.  Project repositories and plugin versions are defined here.

Assets
======

android/assets is the assets directory, however if there is no android project selected, it will configure the project to use core/assets

