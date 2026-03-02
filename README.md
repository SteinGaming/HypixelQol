# HypixelQol


A hypixel <span style="font-size:7px">~~macro~~</span> improvement mod for Hypixel Skyblock.

### Feature list
- **Fishing**: Automatically reel and throw bobber
- **Rift**: Auto-Ice and Auto-Melon
- **Fastleap**: Leap to class when in specified section


### Building it yourself

Due to the influx of rats in this community (yall ratters are desperate for coins man), many people are worried if the .jar is safe, which I totally understand. <br>
Github Actions automatically updates the jar [in the release tab](https://github.com/SteinGaming/FishingQol/releases/tag/v1.0.0), so it's not possible for me to modify it afterwards, but I will still include steps on how to get it yourself.

1. Either:
   1. [Download](https://github.com/SteinGaming/FishingQol/archive/refs/tags/v1.0.0.zip) this repository and extract the folder.
   2. [Clone](https://www.git-scm.com/docs/git-clone) this repository using the [git cli](https://www.git-scm.com/)
2. Open your Command line inside the HypixelQol directory
3. Run `gradlew.bat :<version>:build` for Windows or `./gradlew :<version>:build` for Linux (if you get a permission missing error, run `chmod +x gradlew;`)
   - Replace `<version>` with the legacy version style. It currently accepts either `1.21.10` or `1.21.11`
4. After it's done running, the JARs will appear in `build/libs/`, which you can then copy to your mods directory
5. FOR NON DEVS ONLY: Delete any remenants after you're done. <br>
   Your home folder will contain a `.gradle`, most likely hidden, folder which should/could contain multiple hundreds of Megabytes. <br>
   You can safely delete it, but if you want to build this project again, it will have to download everything again. <br>
