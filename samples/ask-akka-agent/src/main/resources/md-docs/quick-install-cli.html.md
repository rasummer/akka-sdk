<!-- <nav> -->
- [Akka](../index.html)
- [Install the Akka CLI](quick-install-cli.html)

<!-- </nav> -->

# Install the Akka CLI

|  | In case there is any trouble with installing the CLI when following these instructions, please check the [detailed CLI installation instructions](../operations/cli/installation.html). |
Linux Download and install the latest version of `akka`:

```bash
curl -sL https://doc.akka.io/install-cli.sh | bash
```
macOS The recommended approach to install `akka` on macOS, is using [brew](https://brew.sh/)

```bash
brew install akka/brew/akka
```
Windows
1. Download the latest version of `akka` from [https://downloads.akka.io/latest/akka_windows_amd64.zip](https://downloads.akka.io/latest/akka_windows_amd64.zip)
2. Extract the zip file and move `akka.exe` to a location on your `%PATH%`.
Verify that the Akka CLI has been installed successfully by running the following to list all available commands:

```command
akka help
```

<!-- <footer> -->

<!-- </footer> -->

<!-- <aside> -->

<!-- </aside> -->