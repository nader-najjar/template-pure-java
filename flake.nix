{
  description = "Dev shell for template-pure-java";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.11";

  outputs = { self, nixpkgs }:
    let
      systems = [ "aarch64-darwin" "x86_64-darwin" "x86_64-linux" "aarch64-linux" ];
      forEachSystem = f: builtins.listToAttrs (map (system: { name = system; value = f system; }) systems);
    in
    {
      devShells = forEachSystem (system:
        let
          pkgs = import nixpkgs { inherit system; };

          chosenJdk = pkgs.jdk25_headless;
          chosenPodman = pkgs.podman;
        in
        {
          # Shell for local + CI builds, used by universal-build
          default = pkgs.mkShell {
            packages = [
              chosenJdk
              chosenPodman
            ];

            shellHook = ''
              export JAVA_HOME=${chosenJdk}

              # On macOS, ensure Podman is usable by bringing up the Podman machine if needed.
              # This belongs here, rather than the idiomatic build system, since it is toolchain configuration.
              if [ "$(uname -s)" = "Darwin" ]; then
                if ! podman info >/dev/null 2>&1; then
                  echo "Podman is not yet usable from this Nix shell; attempting to initialize and start Podman machine..." >&2
                  podman machine inspect >/dev/null 2>&1 || podman machine init
                  if ! podman machine start; then
                    echo "Failed to start Podman machine." >&2
                    echo "On macOS, ensure Podman Desktop/VM can run, or run:" >&2
                    echo "  podman machine init" >&2
                    echo "  podman machine start" >&2
                    exit 1
                  fi
                fi
              fi
            '';
          };

          # Shell for Docker build-time (image construction).
          # Use this when you need additional build-only tools that should not be present at runtime.
          dockerBuild = pkgs.mkShell {
            packages = [
            ];
          };

          # Shell for runtime-only environment.
          # The Dockerfile snapshots this environment via `nix print-dev-env` and applies it at container startup.
          runtime = pkgs.mkShell {
            packages = [
              chosenJdk
            ];

            shellHook = ''
              export JAVA_HOME=${chosenJdk}
            '';
          };
        });
    };
}
