package org.metaborg.strdoc.transform.extractor;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import java.io.IOException;

import static org.metaborg.strdoc.transform.Constants.parserConfig;

public class DataExtractor {
    private static final String TRANSFORM_TO_XDOC = "stratego-to-strdoc";
    private static final String TRANSFORM_TO_JSON = "strdoc-to-json";
    private static final Logger logger = LoggerFactory.getLogger(DataExtractor.class);

    private final ISourceTextService sourceTextService;
    private final ISpoofaxUnitService unitService;
    private final ISpoofaxSyntaxService syntaxService;
    private final IContextService contextService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final IStrategoCommon strategoCommon;

    @Inject
    public DataExtractor(
            ISourceTextService sourceTextService,
            ISpoofaxUnitService unitService,
            ISpoofaxSyntaxService syntaxService,
            IContextService contextService,
            IStrategoRuntimeService strategoRuntimeService,
            IStrategoCommon strategoCommon) {
        this.sourceTextService = sourceTextService;
        this.unitService = unitService;
        this.syntaxService = syntaxService;
        this.contextService = contextService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.strategoCommon = strategoCommon;
    }

    public String extractData(ILanguageImpl languageImpl, ILanguageImpl dialect, IProject project, FileObject file) throws IOException, MetaborgException, ExtractorException {
        logger.debug("Extract data from {}", file);

        IStrategoTerm ast = parse(languageImpl, dialect, file);

        FileObject languageLocation = Iterables.get(dialect.locations(), 0);
        IContext context = contextService.getTemporary(languageLocation, project, dialect);

        ILanguageComponent component = Iterables.get(dialect.components(), 0);
        HybridInterpreter interpreter = strategoRuntimeService.runtime(component, context, false);

        IStrategoTerm xdoc = strategoCommon.invoke(interpreter, ast, TRANSFORM_TO_XDOC);
        IStrategoTerm json = strategoCommon.invoke(interpreter, xdoc, TRANSFORM_TO_JSON);

        return ((IStrategoString) json).stringValue();
    }

    private IStrategoTerm parse(ILanguageImpl languageImpl, ILanguageImpl dialect, FileObject file) throws IOException, ParseException, ExtractorException {
        String text = sourceTextService.text(file);
        ISpoofaxInputUnit inputUnit = unitService.inputUnit(file, text, languageImpl, dialect, parserConfig);
        ISpoofaxParseUnit parseUnit = syntaxService.parse(inputUnit);

        if (parseUnit.ast() == null) {
            throw new ExtractorException("Unable to parse file " + file);
        }

        return parseUnit.ast();
    }
}
