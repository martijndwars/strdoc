package org.metaborg.strdoc.transform;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.SimpleProjectService;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.style.IRegionCategory;
import org.metaborg.core.style.IRegionStyle;
import org.metaborg.core.style.IStyle;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokens;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.strategoxt.HybridInterpreter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    private static final String ARCHIVE = "/Users/martijn/eclipse-workspace/xdoc/target/xdoc-2.4.0-SNAPSHOT.spoofax-language";
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String STRATEGO_ARCHIVE = "/Users/martijn/Projects/spoofax-releng/stratego/org.metaborg.meta.lang.stratego/target/org.metaborg.meta.lang.stratego-2.4.0-SNAPSHOT.spoofax-language";
    private static final String DATA_DIR = "/Users/martijn/Projects/documentation-xsl/data/";
    private static final String SOURCE_DIR = "/Users/martijn/Projects/documentation-xsl/source/";
    private static final String TRANSFORM_TO_XDOC = "stratego-to-xdoc";
    private static final String TRANSFORM_TO_JSON = "xdoc-to-json";
    private static final String SSL_DIR = "/Users/martijn/Projects/spoofax-releng/strategoxt/strategoxt/stratego-libraries/lib/spec";

    public static void main(String[] args) {
        try (final Spoofax spoofax = new Spoofax()) {
            // Load language
            FileObject directory = spoofax.resourceService.resolve(ARCHIVE);
            ILanguageImpl languageImpl = spoofax.languageDiscoveryService.languageFromArchive(directory);
            FileObject parseTable = spoofax.resourceService.resolve("file:///Users/martijn/eclipse-workspace/xdoc/trans/Stratego-xdoc.tbl");
            FileObject location = spoofax.resourceService.resolve("zip:file:///Users/martijn/eclipse-workspace/org.metaborg.xdoc/target/org.metaborg.xdoc-0.1.0-SNAPSHOT.spoofax-language");
            SyntaxFacet syntaxFacet = new SyntaxFacet(parseTable, null, Lists.newArrayList("Module"));
            ILanguageImpl dialect = spoofax.dialectService.add("Stratego-Docx", location, languageImpl, syntaxFacet);

            FileObject strategoDirectory = spoofax.resourceService.resolve("file://" + SSL_DIR);
            FileObject[] files = strategoDirectory.findFiles(new AllFileSelector());

            SimpleProjectService simpleProjectService = spoofax.injector.getInstance(SimpleProjectService.class);
            FileObject resource = spoofax.resourceService.resolve(ARCHIVE);
            IProject project = simpleProjectService.create(resource);

            FileObject strategoArchive = spoofax.resourceService.resolve(STRATEGO_ARCHIVE);
            ILanguageImpl strategoImpl = spoofax.languageDiscoveryService.languageFromArchive(strategoArchive);

            for (FileObject file : files) {
                if (!file.isFile()) {
                    continue;
                }

                // Extract doc, save XML file
                String doc = extractDoc(spoofax, languageImpl, dialect, project, file);

                String relativeName = strategoDirectory.getName().getRelativeName(file.getName());
                System.out.println(relativeName);

                File targetFile = new File(DATA_DIR + relativeName + ".json");
                targetFile.getParentFile().mkdirs();
                targetFile.createNewFile();

                try (PrintWriter printWriter = new PrintWriter(targetFile)) {
                    printWriter.write(doc);
                }

                // Turn source into HTML
                String html = htmlSource(spoofax, strategoImpl, project, file);

                File targetSourceFile = new File(SOURCE_DIR + relativeName + ".html");
                targetSourceFile.getParentFile().mkdirs();
                targetSourceFile.createNewFile();

                try (PrintWriter printWriter = new PrintWriter(targetSourceFile)) {
                    printWriter.println(html);
                }
            }
        } catch (MetaborgException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String htmlSource(Spoofax spoofax, ILanguageImpl languageImpl, IProject project, FileObject file) throws IOException, ParseException {
        // Prepare parse
        String text = spoofax.sourceTextService.text(file);
        JSGLRParserConfiguration config = new JSGLRParserConfiguration(true, false, false, 10000, 0);
        ISpoofaxInputUnit input = spoofax.unitService.inputUnit(file, text, languageImpl, null, config);

        // Parse
        ISpoofaxParseUnit parseUnit = parse(spoofax, input);

        if (parseUnit == null) {
            return "";
        }

        IStrategoTerm ast = parseUnit.ast();

        // TODO: What does this do?
        Iterable<IRegionCategory<IStrategoTerm>> categorization = spoofax.categorizerService.categorize(languageImpl, parseUnit);
        Iterable<IRegionStyle<IStrategoTerm>> styles = spoofax.stylerService.styleParsed(languageImpl, categorization);

        // Transform tokens in source?
        ITokens tokens = ImploderAttachment.get(ast).getLeftToken().getTokenizer();

        // TODO: Zip tokens with styles
        // TODO: Collapse adjacent tokens with the same style (to get lightweight HTML)

        StringBuilder stringBuilder = new StringBuilder();

        for (IToken token : tokens) {
            ISourceRegion tokenRegion = tokenToRegion(token);
            IStyle style = getStyle(tokenRegion, styles);
            String string = StringEscapeUtils.escapeHtml4(token.toString());

            if (style != null) {
                Color color = style.color();

                if (color != null) {
                    String rgb = getRgb(color);

                    stringBuilder.append("<span style=\"color: " + rgb + "\">" + string + "</span>");
                } else {
                    stringBuilder.append(string);
                }
            } else {
                stringBuilder.append(string);
            }
        }

        return wrapLines(stringBuilder.toString());
    }

    private static String wrapLines(String code) {
        String[] lines = code.split(NEWLINE);

        return IntStream.range(0, lines.length)
                .mapToObj(i -> "<code id=\"" + i + "\">" + lines[i] + "</code>")
                .collect(Collectors.joining(NEWLINE));
    }

    private static String getRgb(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        return "rgb(" + r + ", " + g + ", " + b + ")";
    }

    // TODO: Do not loop styles every time
    private static IStyle getStyle(ISourceRegion tokenRegion, Iterable<IRegionStyle<IStrategoTerm>> styles) {
        for (IRegionStyle<IStrategoTerm> style : styles) {
            if (regionEquals(tokenRegion, style.region())) {
                return style.style();
            }
        }

        return null;
    }

    private static boolean regionEquals(ISourceRegion r1, ISourceRegion r2) {
        return  r1.contains(r2) && r2.contains(r1);
    }

    private static ISourceRegion tokenToRegion(IToken token) {
        return new SourceRegion(token.getStartOffset(), token.getEndOffset());
    }

    private static String extractDoc(Spoofax spoofax, ILanguageImpl languageImpl, ILanguageImpl dialect, IProject project, FileObject file) throws IOException, MetaborgException {
        // Prepare parse
        String nablContents = spoofax.sourceTextService.text(file);
        JSGLRParserConfiguration config = new JSGLRParserConfiguration(true, false, false, 10000, 0);
        ISpoofaxInputUnit input = spoofax.unitService.inputUnit(file, nablContents, languageImpl, dialect, config);

        // Parse
        ISpoofaxParseUnit parse = parse(spoofax, input);

        if (parse == null) {
            return "";
        }

        IStrategoTerm ast = parse.ast();

        // Transform
        FileObject languageLocation = Iterables.get(languageImpl.locations(), 0);
        IContext context = spoofax.contextService.getTemporary(languageLocation, project, languageImpl);

        ILanguageComponent component = Iterables.get(languageImpl.components(), 0);
        HybridInterpreter interpreter = spoofax.strategoRuntimeService.runtime(component, context, false);

        IStrategoTerm xdoc = spoofax.strategoCommon.invoke(interpreter, ast, TRANSFORM_TO_XDOC);
        IStrategoTerm json = spoofax.strategoCommon.invoke(interpreter, xdoc, TRANSFORM_TO_JSON);

        return ((IStrategoString) json).stringValue();
    }

    private static ISpoofaxParseUnit parse(Spoofax spoofax, ISpoofaxInputUnit input) throws ParseException {
        ISpoofaxParseUnit output = spoofax.syntaxService.parse(input);

        if (!output.valid()) {
            System.out.println("Could not parse " + input.source());
            return null;
        }

        if (!output.success()) {
            System.out.println("Parsed, but not successful: " + output.ast());
            System.out.println(Joiner.on("\n").join(output.messages()));
            return null;
        }

        return output;
    }
}
