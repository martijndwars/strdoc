package org.metaborg.strdoc.lang.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.metaborg.core.context.IContext;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import com.google.inject.Injector;

public class get_row_start_0_0 extends Strategy {
	public static get_row_start_0_0 instance = new get_row_start_0_0();
    
    @Override
    public IStrategoTerm invoke(Context strategoContext, IStrategoTerm current) {
        IContext context = (IContext) strategoContext.contextObject();
        Injector injector = context.injector();
    		
        ISpoofaxTracingService tracingService = injector.getInstance(ISpoofaxTracingService.class);
        ISourceLocation location = tracingService.location(current);
        int row = location.region().startRow();
        
        ITermFactory termFactory = strategoContext.getFactory();
    		IStrategoTerm term = termFactory.makeInt(row);
    		
    		return term;
    }
}
