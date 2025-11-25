{
  description = "Dev shell for template-pure-java";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.05";

  outputs = { self, nixpkgs }:
    let
      systems = [ "aarch64-darwin" "x86_64-darwin" "x86_64-linux" "aarch64-linux" ];
      forEachSystem = f: builtins.listToAttrs (map (system: { name = system; value = f system; }) systems);
    in
    {
      devShells = forEachSystem (system:
        let
          pkgs = import nixpkgs { inherit system; };
        in
        {
          default = pkgs.mkShell {
            packages = [
              pkgs.jdk24_headless
            ];

            shellHook = ''
              export JAVA_HOME=${pkgs.jdk24_headless}
              export GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx1g -Dfile.encoding=UTF-8"
              echo "Using Nix dev shell with OpenJDK ${pkgs.jdk24_headless.version} (headless)"
            '';
          };
        });
    };
}
