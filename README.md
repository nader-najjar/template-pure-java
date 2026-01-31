# template-pure-java

Java best practices.

&nbsp;

## 1. Local Development Machine Prerequisites
* Install Nix as per https://nix.dev/install-nix
  * Nix handles all other requirements so that developer machine setup is as minimal as possible
    * Further, it does not matter what version of Nix you have - it guarantees reproducibility

&nbsp;

## 2. Tips
* Run `echo "alias ub='./universal-build'" >> ~/.zshrc` then `source ~/.zshrc` to make `ub` a quick alias for `./universal-build`

&nbsp;

## 3. Build Tasks

### `./universal-build build`
* Compiles the code under `src`
* Runs unit tests
* Runs Jacoco
* Runs Checkstyle
* Runs SpotBugs
* Builds a container image with Podman, set to run the code entrypoint (tagged `<project-name>:latest`)
* Saves the image as `build/container-image.tar`

### `./universal-build clean`
* Removes build output directories

### `./universal-build help`
* Invokes Gradle's help command, showing basic functionality

### `./universal-build tasks`
* Invokes Gradle's tasks command, showing all available tasks

&nbsp;

## 4. Workflows

### Adding A Java Dependency
1) Find the coordinates on Maven Central (https://central.sonatype.com) or the library’s docs.
2) Decide path:
   * **If a BOM (Bill of Materials) exists:** add the BOM once as a platform (e.g., `implementation(platform("group:artifact-bom:version"))`), then add the dependency in the right scope, using the same syntax as below but omitting the `:version`.
     * **If no BOM exists:** add the dependency with an explicit version in the right scope:
       * `implementation("group:artifact:version")`
       * `compileOnly("group:artifact:version")`
       * `annotationProcessor("group:artifact:version")`
       * `runtimeOnly("group:artifact:version")`
       * Tests mirror these scopes: `testImplementation`, `testCompileOnly`, `testAnnotationProcessor`, `testRuntimeOnly`
3) Update the Gradle lockfile using the workflow below
4) Run `./universal-build build` to download and verify it resolves; use an IDE “reload Gradle project” if needed.

### Updating Gradle Lockfile
1) Run `./universal-build dependencies --write-locks`

### Adding A Toolchain Dependency
1) Search https://search.nixos.org/packages for the package name
2) Add the appropriate package to the package list in the `flake.nix` file

### Updating Nix `nixpkgs` Version
1) Modify the version of `inputs.nixpkgs.url` in the `flake.nix` file to the desired version, according to https://status.nixos.org
2) Run `./universal-build --update-nix-flake-lockfile` to update the `flake.lock` lockfile
3) Commit both files to version control

### Ensuring Gradle Files Are Up-To-Date
1) Run `./universal-build wrapper` to update the (this is just a pass-through to `./gradlew wrapper`)
   * This connects to the internet to reset the local self-contained Gradle files to the factory default of the given version. Note that this keeps the same Gradle version.
   * This can help if the Gradle files were stale or modified for some reason (for example, IntelliJ's starting Gradle template gives incorrect files in some cases)

### Upgrading Gradle Version
1) Run `./universal-build wrapper --gradle-version x.y`
   * This connects to the internet to reset the local self-contained Gradle files to the factory default of the given version.

&nbsp;

## 5. IDE Setup

### IntelliJ
1) Open the IntelliJ startup window, click `Open`, and select this cloned repository
2) Run `./universal-build --print-java-path-for-ide` to get the local nix installation of the java version specified in the flake
3) Go to `File -> Project Structure -> Project -> SDK -> Add JDK From Disk`, then select the path from step 1
4) Go to `Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JVM` and specify the same JDK as from step 2
5) Go to `Settings -> Build, Execution, Deployment -> Build Tools` and check the box for `Sync...`, and select the radio button for `Any changes`

### Visual Studio Code
1) Open an empty Visual Studio Code startup window (important!!)
2) If it is not already installed, install the extension with unique identifier `vscjava.vscode-java-pack` (the Extension Pack for Java)
3) Click `File -> Add Folder to Workspace` and select this cloned repository
4) Run `./universal-build --print-java-path-for-ide` to get the local nix installation of the java version specified in the flake
5) Go to folder settings JSON (`cmd+shift+p -> Preferences: Open Folder Settings (JSON)`), select the folder root that was added in step 3, and add the following. By placing this in your folder settings rather than the user/workspace settings, it will ensure you can properly configure this per-repository
   * `"java.jdt.ls.java.home": "path/from/step/2"`
   * `"java.import.gradle.java.home": "path/from/step/2"`
   * `"java.configuration.updateBuildConfiguration": "automatic"`

&nbsp;

## 6. References

### Nix
* Installation: https://nix.dev/install-nix
* Nix Versions: https://status.nixos.org
* Nix Package Search: https://search.nixos.org/packages

### Maven
* Maven Central Search: https://central.sonatype.com

### Jackson
* Jackson Databind Javadoc: https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-databind/latest/index.html
* Jackson Databind GitHub README Tutorial: https://github.com/FasterXML/jackson-databind
* Jackson Databind Wiki (Including Databind-Specific Annotations): https://github.com/FasterXML/jackson-databind/wiki
* Jackson Annotations Javadoc: https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-annotations/latest/com.fasterxml.jackson.annotation/com/fasterxml/jackson/annotation/package-summary.html
* Jackson Annotations GitHub README Tutorial: https://github.com/FasterXML/jackson-annotations
* Jackson Annotations Wiki: https://github.com/FasterXML/jackson-annotations/wiki

### Hibernate
* Main Documentation Page: https://docs.hibernate.org/validator/9.1/reference/en-US/html_single/
* Anchor To Available Annotations: https://docs.hibernate.org/validator/9.1/reference/en-US/html_single/#section-builtin-constraints
