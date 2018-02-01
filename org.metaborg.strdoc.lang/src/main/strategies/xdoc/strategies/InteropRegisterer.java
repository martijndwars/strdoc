package xdoc.strategies;

import org.strategoxt.lang.JavaInteropRegisterer;
import org.strategoxt.lang.Strategy;

public class InteropRegisterer extends JavaInteropRegisterer {
    public InteropRegisterer() {
        super(new Strategy[] {
        		get_row_start_0_0.instance,
        		get_row_end_0_0.instance
        });
    }
}
