package org.metaborg.strdoc.transform;

import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;

public class Constants {
    public static final JSGLRParserConfiguration parserConfig =
            new JSGLRParserConfiguration(true, false, false, 10000, 0);
}
