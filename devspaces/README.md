# LibGDX Development with DF Devspaces

## Install DF Devspaces

1. Create and install devspaces client as it is written in help guide https://support.devspaces.io/article/22-devspaces-client-installation.

2. Here is some details about DF Devspaces https://devspaces.io/devspaces/help

Here follows the main commands used in Devspaces cli. 

|action   |Description                                                                                   |
|---------|----------------------------------------------------------------------------------------------|
|`devspaces --help`                    |Check the available command names.                               |
|`devspaces create [options]`          |Creates a DevSpace using your local DevSpaces configuration file |
|`devspaces start <devSpace>`          |Starts the DevSpace named \[devSpace\]                           |
|`devspaces bind <devSpace>`           |Syncs the DevSpace with the current directory                    |
|`devspaces info <devSpace> [options]` |Displays configuration info about the DevSpace.                  |

Use `devspaces --help` to know about updated commands.


### Start Devspaces 

It is assumed that you opened a terminal in `devspaces` folder of the repository.

1.  Create DevSpaces.

```bash
devspaces create
```

2. Start your devspaces.
```bash
devspaces start libgdx
```

3. Start containers synchronization
Open terminal on folder you want to sync with devspaces and run:

```bash
cd ..
devspaces bind libgdx
```
4. Grab some container info

```bash
devspaces info libgdx
```

5. Connect to development container

```bash
devspaces exec libgdx
```

6. Wait until source code will be synced and build the project. You may find out the sync status by openning `http://localhost:49152` in your browser.
 
```bash
cd libgdx
./gradlew fetchNatives

```

7. You may run tests.

```bash
./gradlew tests:gdx-tests-android:installDebug
./gradlew tests:gdx-tests-gwt:superDev
```

### Troubleshooting

1. By the moment of preparing this devspace `Dockerfile` was not building in DF Devspaces. However it was building fine locally. Therefore there is another dockerfile in `other` folder. Please use it in case you faced with devspaces build issues. You may start using it just by substituting this line in `devspaces.yml` file

```
...
docker-file: Dockerfile
...
```

to this one

```
...
docker-file: other/Dockerfile
...
```
