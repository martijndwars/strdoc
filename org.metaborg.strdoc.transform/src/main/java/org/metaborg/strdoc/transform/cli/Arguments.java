package org.metaborg.strdoc.transform.cli;

import com.beust.jcommander.Parameter;

public class Arguments {
    @Parameter(names = {"-s", "--stratego"}, description = "Stratego language archive (.spoofax-language)", required = true, validateWith = FileExistsValidator.class)
    private String strategoArchivePath;

    @Parameter(names = {"-d", "--doc"}, description = "Strdoc dialect language archive (.spoofax-language)", required = true, validateWith = FileExistsValidator.class)
    private String strdocArchivePath;

    @Parameter(names = {"-p", "--project"}, description = "Path to project to extract documentation for", required = true, validateWith = FileExistsValidator.class)
    private String projectPath;

    @Parameter(names = {"-o", "--output"}, description = "Output directory", validateWith = FileExistsValidator.class)
    private String outputPath = ".";

    @Parameter(names = {"-h", "--help"}, description = "Usage information", help = true)
    private boolean help = false;

    public String getStrategoArchivePath() {
        return strategoArchivePath;
    }

    public String getStrdocArchivePath() {
        return strdocArchivePath;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public boolean isHelp() {
        return help;
    }
}
