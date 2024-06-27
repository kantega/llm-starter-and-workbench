package no.kantega.llm.expr;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import no.hal.expressions.ExpressionSupport;
import no.hal.expressions.janino.JaninoExpressionSupport;
import no.hal.expressions.mvel.MvelExpressionSupport;

@ApplicationScoped
public class ExpressionSupports {
    
    @Inject
    MvelExpressionSupport mvelExpressionSupport;
    
    @Produces
    @Named("mvel")
    public ExpressionSupport createMvelExpressionSupport() {
        return mvelExpressionSupport;
    }
    
    @Inject
    JaninoExpressionSupport janinoExpressionSupport;
    
    @Produces
    @Named("janino")
    public ExpressionSupport createJaninoExpressionSupport() {
        return janinoExpressionSupport;
    }
}
