# template-pure-java


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

### Adding A Toolchain Dependency
* Search https://search.nixos.org/packages for the package name
* Add the appropriate package to the package list in the `flake.nix` file

### Updating Nix `nixpkgs` Version
* Modify the version of `inputs.nixpkgs.url` in the `flake.nix` file to the desired version, according to https://status.nixos.org
* Run `./universal-build --update-nix-flake` to update the `flake.lock` lockfile
* Commit both files to version control
