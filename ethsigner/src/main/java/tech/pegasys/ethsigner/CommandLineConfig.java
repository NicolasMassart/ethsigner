/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.ethsigner;

import tech.pegasys.ethsigner.signing.ChainIdProvider;
import tech.pegasys.ethsigner.signing.ConfigurationChainId;

import java.io.PrintStream;
import java.net.InetAddress;
import java.nio.file.Path;
import java.time.Duration;

import com.google.common.base.MoreObjects;
import org.apache.logging.log4j.Level;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

@SuppressWarnings("FieldCanBeLocal") // because Picocli injected fields report false positives
@Command(
    description = "This command runs the EthSigner.",
    abbreviateSynopsis = true,
    name = "ethsigner",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    header = "Usage:",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    optionListHeading = "%nOptions:%n",
    footerHeading = "%n",
    footer = "EthSigner is licensed under the Apache License 2.0")
public class CommandLineConfig implements Config {

  private CommandLine commandLine;

  @Option(
      names = {"--logging", "-l"},
      paramLabel = "<LOG VERBOSITY LEVEL>",
      description =
          "Logging verbosity levels: OFF, FATAL, WARN, INFO, DEBUG, TRACE, ALL (default: INFO)")
  private final Level logLevel = Level.INFO;

  @Option(
      names = {"-p", "--password-file"},
      description = "The path to a file containing the passwordFile used to decrypt the keyfile.",
      required = true,
      arity = "1")
  private Path passwordFilePath;

  @SuppressWarnings("FieldMayBeFinal") // Because PicoCLI requires Strings to not be final.
  @Option(
      names = {"-k", "--key-file"},
      description = "The path to a file containing the key used to sign transactions.",
      required = true,
      arity = "1")
  private Path keyFile;

  @Option(
      names = "--downstream-http-host",
      description = "The endpoint to which received requests are forwarded",
      arity = "1")
  private InetAddress downstreamHttpHost = InetAddress.getLoopbackAddress();

  @Option(
      names = "--downstream-http-port",
      description = "The endpoint to which received requests are forwarded",
      required = true,
      arity = "1")
  private Integer downstreamHttpPort;

  @Option(
      names = {"--downstream-http-request-timeout"},
      description =
          "Timeout (in milliseconds) to wait for downstream request to timeout (default: ${DEFAULT-VALUE})",
      arity = "1")
  private long downstreamHttpRequestTimeout = Duration.ofSeconds(5).toMillis();

  @SuppressWarnings("FieldMayBeFinal") // Because PicoCLI requires Strings to not be final.
  @Option(
      names = {"--http-listen-host"},
      description = "Host for JSON-RPC HTTP to listen on (default: ${DEFAULT-VALUE})",
      arity = "1")
  private InetAddress httpListenHost = InetAddress.getLoopbackAddress();

  @Option(
      names = {"--http-listen-port"},
      description = "Port for JSON-RPC HTTP to listen on (default: ${DEFAULT-VALUE})",
      arity = "1")
  private final Integer httpListenPort = 8545;

  @SuppressWarnings("FieldMayBeFinal") // Because PicoCLI requires Strings to not be final.
  @Option(
      names = {"--chain-id"},
      description = "The Chain Id that will be the intended recipient for signed transactions",
      required = true,
      arity = "1")
  private long chainId;

  private final PrintStream output;

  public CommandLineConfig(PrintStream output) {
    this.output = output;
  }

  public boolean parse(final String... args) {

    commandLine = new CommandLine(this);
    commandLine.setCaseInsensitiveEnumValuesAllowed(true);
    commandLine.registerConverter(Level.class, Level::valueOf);

    // Must manually show the usage/version info, as per the design of picocli
    // (https://picocli.info/#_printing_help_automatically)
    try {
      commandLine.parse(args);
    } catch (ParameterException ex) {
      handleParseException(ex);
      return false;
    }

    if (commandLine.isUsageHelpRequested()) {
      commandLine.usage(output);
      return false;
    } else if (commandLine.isVersionHelpRequested()) {
      commandLine.printVersionHelp(output);
      return false;
    }
    return true;
  }

  public void handleParseException(final ParameterException ex) {
    if (logLevel != null && Level.DEBUG.isMoreSpecificThan(logLevel)) {
      ex.printStackTrace(output);
    } else {
      output.println(ex.getMessage());
    }
    if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, output)) {
      ex.getCommandLine().usage(output, Ansi.AUTO);
    }
  }

  @Override
  public Level getLogLevel() {
    return logLevel;
  }

  @Override
  public Path getPasswordFilePath() {
    return passwordFilePath;
  }

  @Override
  public Path getKeyPath() {
    return keyFile;
  }

  @Override
  public InetAddress getDownstreamHttpHost() {
    return downstreamHttpHost;
  }

  @Override
  public Integer getDownstreamHttpPort() {
    return downstreamHttpPort;
  }

  @Override
  public InetAddress getHttpListenHost() {
    return httpListenHost;
  }

  @Override
  public Integer getHttpListenPort() {
    return httpListenPort;
  }

  @Override
  public ChainIdProvider getChainId() {
    return new ConfigurationChainId(chainId);
  }

  @Override
  public Duration getDownstreamHttpRequestTimeout() {
    return Duration.ofMillis(downstreamHttpRequestTimeout);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("commandLine", commandLine)
        .add("logLevel", logLevel)
        .add("passwordFilePath", passwordFilePath)
        .add("keyFile", keyFile)
        .add("downstreamHttpHost", downstreamHttpHost)
        .add("downstreamHttpPort", downstreamHttpPort)
        .add("downstreamHttpRequestTimeout", downstreamHttpRequestTimeout)
        .add("httpListenHost", httpListenHost)
        .add("httpListenPort", httpListenPort)
        .add("chainId", chainId)
        .add("output", output)
        .toString();
  }
}
