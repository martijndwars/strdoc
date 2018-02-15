package org.metaborg.strdoc.transform.extractor;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageFileSelector;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.strdoc.transform.cli.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class Extractor {
    private static final String DIALECT_NAME = "Stratego-Docx";
    private static final String DIALECT_START_SYMBOL = "Module";
    private static final String DIALECT_TABLE = "/Users/martijn/Projects/strdoc/org.metaborg.strdoc.lang/trans/Stratego-strdoc.tbl";
    private static final String DATA_DIR = "data/json";
    private static final String SOURCE_DIR = "data/source";
    private static final Logger logger = LoggerFactory.getLogger(Extractor.class);

    private final IResourceService resourceService;
    private final ISimpleProjectService projectService;
    private final ILanguageDiscoveryService languageDiscoveryService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final IDialectService dialectService;
    private final DataExtractor dataExtractor;
    private final SourceExtractor sourceExtractor;

    @Inject
    public Extractor(
            IResourceService resourceService,
            ISimpleProjectService projectService,
            ILanguageDiscoveryService languageDiscoveryService,
            ILanguageIdentifierService languageIdentifierService,
            IDialectService dialectService,
            DataExtractor dataExtractor,
            SourceExtractor sourceExtractor) {
        this.resourceService = resourceService;
        this.projectService = projectService;
        this.languageDiscoveryService = languageDiscoveryService;
        this.languageIdentifierService = languageIdentifierService;
        this.dialectService = dialectService;
        this.dataExtractor = dataExtractor;
        this.sourceExtractor = sourceExtractor;
    }

    public void extract(Arguments arguments) throws IOException, MetaborgException {
        String outputPath = arguments.getOutputPath();
        FileObject targetDir = resourceService.resolve(outputPath);

        IProject project = getOrCreateProject(resourceService.resolve("."));

        FileObject strategoArchiveFile = resourceService.resolve(arguments.getStrategoArchivePath());
        ILanguageImpl stratego = loadLanguage(strategoArchiveFile);

        FileObject strdocArchiveFile = resourceService.resolve(arguments.getStrdocArchivePath());
        ILanguageImpl strdoc = loadDialect(strdocArchiveFile);

        FileObject sourceDir = resourceService.resolve("file://" + arguments.getProjectPath());
        FileObject[] files = sourceDir.findFiles(new LanguageFileSelector(languageIdentifierService, stratego));

        for (FileObject sourceFile : files) {
            logger.info("Process {}", sourceFile);

            try {
                String data = dataExtractor.extractData(stratego, strdoc, project, sourceFile);
                save(sourceDir, sourceFile, targetDir, data, DATA_DIR, "json");

                String source = sourceExtractor.extractSource(stratego, sourceFile);
                save(sourceDir, sourceFile, targetDir, source, SOURCE_DIR, "html");
            } catch (ExtractorException e) {
                logger.error("An error occurred while extracting the data/source.", e);
            }
        }
    }

    private void save(FileObject base, FileObject file, FileObject targetDir, String content, String location, String extension) throws IOException {
        String relativeName = base.getName().getRelativeName(file.getName());
        String pathname = location + "/" + relativeName + "." + extension;

        FileObject targetFile = targetDir.resolveFile(pathname);
        targetFile.createFile();

        try (Writer writer = new PrintWriter(targetFile.getContent().getOutputStream())) {
            writer.write(content);
        }
    }

    private ILanguageImpl loadLanguage(FileObject location) throws FileSystemException, MetaborgException {
        if (location.isFolder()) {
            return languageDiscoveryService.languageFromDirectory(location);
        } else if (location.isFile()) {
            return languageDiscoveryService.languageFromArchive(location);
        } else {
            throw new MetaborgException("Cannot load language from location with type " + location.getType());
        }
    }

    private ILanguageImpl loadDialect(FileObject archiveFile) throws MetaborgException {
        ILanguageImpl languageImpl = languageDiscoveryService.languageFromArchive(archiveFile);
        FileObject parseTable = resourceService.resolve("file://" + DIALECT_TABLE);
        FileObject location = resourceService.resolve("zip:" + archiveFile);
        SyntaxFacet syntaxFacet = new SyntaxFacet(parseTable, null, Lists.newArrayList(DIALECT_START_SYMBOL));

        return dialectService.add(DIALECT_NAME, location, languageImpl, syntaxFacet);
    }

    private IProject getOrCreateProject(FileObject resource) throws MetaborgException {
        IProject project = projectService.get(resource);

        if (project == null) {
            return projectService.create(resource);
        }

        return project;
    }
}
