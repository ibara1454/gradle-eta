package com.typelead.gradle.utils;

import com.typelead.gradle.eta.config.EtaExtension;
import com.typelead.gradle.eta.plugins.EtaPlugin;
import com.typelead.gradle.eta.tasks.EtlasTaskSpec;
import org.gradle.api.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EtlasCommand {

    private final Project project;
    private final boolean useSandbox;
    private final String sandboxConfig;
    private final String defaultUserConfig;
    private final String etlasBinary;
    private final List<String> etlasFlags;
    private final List<String> buildFlags;
    private final String buildDir;
    private final List<String> components;

    public EtlasCommand(Project project, EtaExtension extension) {
        this.project = project;
        this.useSandbox = extension.getUseSandbox();
        this.sandboxConfig = extension.getSandboxConfig();
        this.defaultUserConfig = extension.getDefaultUserConfig();
        this.etlasBinary = extension.getEtlasBinary();
        this.etlasFlags = extension.getEtlasFlags();
        this.buildFlags = extension.getBuildFlags();
        this.buildDir = extension.getBuildDir();
        this.components = Collections.emptyList();
    }

    public EtlasCommand(EtlasTaskSpec task) {
        this.project = task.getProject();
        this.useSandbox = task.getUseSandbox();
        this.sandboxConfig = task.getSandboxConfig();
        this.defaultUserConfig = task.getDefaultUserConfig();
        this.etlasBinary = task.getEtlasBinary();
        this.etlasFlags = task.getEtlasFlags();
        this.buildFlags = task.getBuildFlags();
        this.buildDir = task.getBuildDir();
        this.components = task.getComponents();
    }

    public void configure(List<String> flags) {
        commandWithComponent("configure", flags);
    }

    public void clean() {
        commandWithComponent("clean");
    }

    public void build() {
        commandWithComponent("build");
    }

    public void test(List<String> testFlags) {
        commandWithComponent("test", testFlags);
    }

    public void installDependenciesOnly() {
        defaultCommand("install", Collections.singletonList("--dependencies-only"));
    }

    public List<String> depsMaven() {
        return defaultCommandLine("deps", "--maven").executeAndGetStandardOutputLines()
                .stream()
                .filter(line ->
                    !line.startsWith(" ")
                    && !line.contains("Notice:")
                    && line.contains(":")
                ).collect(Collectors.toList());
    }

    /** This will also download dependencies via `etlas install --dependencies-only` */
    public List<String> depsClasspath() {
        return defaultCommandLine("deps", "--classpath").executeAndGetStandardOutputLines()
                .stream()
                .filter(line ->
                    !line.startsWith(" ")
                    && !line.contains("Notice:")
                    && line.contains(File.separator)
                ).collect(Collectors.toList());
    }

    /** If useSandbox == false or if it has already been init'd, skip; otherwise, sandbox init. */
    public void maybeInitSandbox() {
        if (!useSandbox) return;
        // Check if sandbox has been init'd by seeing if the config already exists.
        boolean sandboxConfigExists =
                new File(project.getRootDir(), determineSandboxConfig()).exists();
        if (sandboxConfigExists) return;
        CommandLine c = initCommandLine();
        c.getCommand().add("sandbox");
        c.getCommand().add("init");
        c.executeAndOutputToSystem();
    }

    private void commandWithComponent(String command, List<String> commandArgs) {
        List<String> args = new ArrayList<>(commandArgs);
        args.addAll(components);
        defaultCommand(command, args);
    }

    private void commandWithComponent(String command, String... commandArgs) {
        commandWithComponent(command, Arrays.asList(commandArgs));
    }

    private CommandLine initCommandLine() {
        CommandLine c = new CommandLine(etlasBinary);
        c.setWorkingDir(project.getRootDir().getPath());
        c.getCommand().addAll(etlasFlags);
        return c;
    }

    private CommandLine defaultCommandLine(String command, List<String> args) {
        CommandLine c = initCommandLine();
        if (useSandbox) {
            if (sandboxConfig != null) c.getCommand().add("--sandbox-config-file=" + sandboxConfig);
            if (defaultUserConfig != null) c.getCommand().add("--default-user-config=" + defaultUserConfig);
        }
        c.getCommand().add(command);
        List<String> commandArgs = new ArrayList<>(args);
        commandArgs.addAll(buildFlags);
        commandArgs.add("--builddir=" + buildDir);
        c.getCommand().addAll(commandArgs);
        return c;
    }

    private CommandLine defaultCommandLine(String command, String... args) {
        return defaultCommandLine(command, Arrays.asList(args));
    }

    private void defaultCommand(String command, List<String> args) {
        defaultCommandLine(command, args).executeAndOutputToSystem();
    }

    private String determineSandboxConfig() {
        return sandboxConfig != null
                ? sandboxConfig
                : EtaPlugin.DEFAULT_SANDBOX_CONFIG;
    }
}
