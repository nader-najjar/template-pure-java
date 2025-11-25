# template-pure-java


## Build Tasks

### `./universal-build build`
* Compiles the code under `src`
* Runs unit tests

### `./universal-build help`
* Invokes Gradle's help command, showing available tasks


## Workflows

### Adding A Java Dependency

### Adding A Toolchain Dependency
* Search https://search.nixos.org/packages for the package name
* Add the appropriate package to the package list in the `flake.nix` file

### Updating Nix `nixpkgs` Version
* Modify the version of `inputs.nixpkgs.url` in the `flake.nix` file to the desired version, according to https://status.nixos.org
* Run `./universal-build --update-flake` to update the `flake.lock` lockfile
* Commit both files to version control
