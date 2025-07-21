# HypixelQol


A hypixel <span style="font-size:7px">~~macro~~</span> improvement mod for Hypixel Skyblock.

## Table of Contents

1. [How to use the fishing feature](#how-to-use-the-fishing-feature)
2. [Building it yourself](#building-it-yourself)

### How to use the fishing feature
This project has been split into two versions, **which are currently out-of-sync and incompatible with each other**:
- [Documentation for Forge 1.8.9](forge-1.8.9/README.md)
- [Documentation for Fabric 1.21.5](fabric-1.21.5/README.md)

### Building it yourself

Due to the influx of rats in this community (yall ratters are desperate for coins man), many people are worried if the .jar is safe, which I totally understand. <br>
Github Actions automatically updates the jar [in the release tab](https://github.com/SteinGaming/FishingQol/releases/tag/v1.0.0), so it's not possible for me to modify it afterwards, but I will still include steps on how to get it yourself.

1. Either:
   1. [Download](https://github.com/SteinGaming/FishingQol/archive/refs/tags/v1.0.0.zip) this repository and extract the folder.
   2. [Clone](https://www.git-scm.com/docs/git-clone) this repository using the [git cli](https://www.git-scm.com/)
2. Open your Command line inside the FishingQol directory
3. Run `gradlew.bat build` for Windows and `./gradlew build` for Linux (if you get a permission missing error, run `chmod +x gradlew;`)
4. After it's done running, the jar will appear in `output/`, which you can then copy to your mods directory
5. FOR NON DEVS ONLY: Delete any remenants after you're done. <br>
   Your home folder will contain a `.gradle`, most likely hidden, folder which should/could contain multiple hundreds of Megabytes. <br>
   You can safely delete it, but if you want to build this project again, it will have to download everything again. <br>
