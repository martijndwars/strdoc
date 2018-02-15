package org.metaborg.strdoc.transform.extractor;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.style.IRegionCategory;
import org.metaborg.core.style.IRegionStyle;
import org.metaborg.core.style.IStyle;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.style.ISpoofaxCategorizerService;
import org.metaborg.spoofax.core.style.ISpoofaxStylerService;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokens;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;

import java.awt.*;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.metaborg.strdoc.transform.Constants.parserConfig;

public class SourceExtractor {
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final Logger logger = LoggerFactory.getLogger(SourceExtractor.class);

    private final ISourceTextService sourceTextService;
    private final ISpoofaxUnitService unitService;
    private final ISpoofaxSyntaxService syntaxService;
    private final ISpoofaxCategorizerService categorizerService;
    private final ISpoofaxStylerService stylerService;

    @Inject
    public SourceExtractor(
            ISourceTextService sourceTextService,
            ISpoofaxUnitService unitService,
            ISpoofaxSyntaxService syntaxService,
            ISpoofaxCategorizerService categorizerService,
            ISpoofaxStylerService stylerService) {
        this.sourceTextService = sourceTextService;
        this.unitService = unitService;
        this.syntaxService = syntaxService;
        this.categorizerService = categorizerService;
        this.stylerService = stylerService;
    }

    public String extractSource(ILanguageImpl languageImpl, FileObject file) throws IOException, ParseException, ExtractorException {
        logger.debug("Extract source from {}", file);

        // Prepare parse
        ISpoofaxParseUnit parseUnit = parse(languageImpl, file);
        IStrategoTerm ast = parseUnit.ast();

        // Categorize
        Iterable<IRegionCategory<IStrategoTerm>> categorization = categorizerService.categorize(languageImpl, parseUnit);
        Iterable<IRegionStyle<IStrategoTerm>> styles = stylerService.styleParsed(languageImpl, categorization);
        ITokens tokens = ImploderAttachment.get(ast).getLeftToken().getTokenizer();

        // Build output
        StringBuilder stringBuilder = new StringBuilder();

        for (IToken token : tokens) {
            stringBuilder.append(extractToken(styles, token));
        }

        return wrapLines(stringBuilder.toString());
    }

    private String extractToken(Iterable<IRegionStyle<IStrategoTerm>> styles, IToken token) {
        ISourceRegion tokenRegion = tokenToRegion(token);

        if (tokenRegion.length() == 0) {
            return token.toString();
        }

        if (token.toString().equals(NEWLINE)) {
            return token.toString();
        }

        String string = StringEscapeUtils.escapeHtml4(token.toString());
        IStyle style = getStyle(tokenRegion, styles);

        if (style == null) {
            return string;
        }

        Color color = style.color();

        if (color == null) {
            return string;
        }

        String rgb = getRgb(color);

        return "<span style=\"color: " + rgb + "\">" + string + "</span>";
    }

    private ISpoofaxParseUnit parse(ILanguageImpl languageImpl, FileObject file) throws ParseException, IOException, ExtractorException {
        String text = sourceTextService.text(file);
        ISpoofaxInputUnit input = unitService.inputUnit(file, text, languageImpl, null, parserConfig);
        ISpoofaxParseUnit parseUnit = syntaxService.parse(input);

        if (parseUnit.ast() == null) {
            throw new ExtractorException("Unable to parse file " + file);
        }

        return parseUnit;
    }

    private static IStyle getStyle(ISourceRegion sourceRegion, Iterable<IRegionStyle<IStrategoTerm>> styles) {
        for (IRegionStyle<IStrategoTerm> style : styles) {
            if (regionEquals(sourceRegion, style.region())) {
                return style.style();
            }
        }

        return null;
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

    private static boolean regionEquals(ISourceRegion r1, ISourceRegion r2) {
        return r1.contains(r2) && r2.contains(r1);
    }

    private static ISourceRegion tokenToRegion(IToken token) {
        return new SourceRegion(token.getStartOffset(), token.getEndOffset());
    }
}
