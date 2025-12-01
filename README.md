# template-pure-java

Java best practices

## Tips
* Run `echo "alias ub='./universal-build'" >> ~/.zshrc` then `source ~/.zshrc` to make `ub` a quick alias for `./universal-build`


## Build Tasks

### `./universal-build build`
* Compiles the code under `src`
* Runs unit tests

### `./universal-build help`
* Invokes Gradle's help command, showing basic functionality

### `./universal-build tasks`
* Invokes Gradle's tasks command, showing all available tasks


## Workflows

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
3) Run `./universal-build build` to download and verify it resolves; use an IDE “reload Gradle project” if needed.

### Adding A Toolchain Dependency
1) Search https://search.nixos.org/packages for the package name
2) Add the appropriate package to the package list in the `flake.nix` file

### Updating Nix `nixpkgs` Version
1) Modify the version of `inputs.nixpkgs.url` in the `flake.nix` file to the desired version, according to https://status.nixos.org
2) Run `./universal-build --update-nix-flake` to update the `flake.lock` lockfile
3) Commit both files to version control

### Ensuring Gradle Files Are Up-To-Date
1) Run `./universal-build wrapper` to update the (this is just a pass-through to `./gradlew wrapper`)
  * This connects to the internet to reset the local self-contained Gradle files to the factory default of the given version. Note that this keeps the same Gradle version.
  * This can help if the Gradle files were stale or modified for some reason (for example, IntelliJ's starting Gradle template gives incorrect files in some cases)

### Upgrading Gradle Version
1) Run `./universal-build wrapper --gradle-version x.y`
  * This connects to the internet to reset the local self-contained Gradle files to the factory default of the given version.


## IDE Setup

### IntelliJ
1) Run `./universal-build --print-java-path-for-ide` to get the local nix installation of the java version specified in the flake.
2) Go to `File -> Project Structure -> Project -> SDK -> Add JDK From Disk`, then select the path from step 1
3) Go to `Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JVM` and specify the same JDK as from step 2
4) Go to `Settings -> Build, Execution, Deployment -> Build Tools` and check the box for `Sync...`, and select the radio button for `Any changes`

### Visual Studio Code
1) If it is not already installed, install the extension with unique identifier `vscjava.vscode-java-pack` (the Extension Pack for Java)
2) Run `./universal-build --print-java-path-for-ide` to get the local nix installation of the java version specified in the flake.
3) Go to workspace settings JSON (`cmd+shift+p -> Open Workspace Settings (JSON)`) and add the following. By placing this in your workspace settings rather than the user settings, it will ensure you can properly configure this per-project
  * `"java.jdt.ls.java.home": "path/from/step/2"`
  * `"java.import.gradle.java.home": "path/from/step/2"`
  * `"java.configuration.updateBuildConfiguration": "automatic"`
