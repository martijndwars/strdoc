package org.metaborg.strdoc.transform.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import java.io.File;

public class FileExistsValidator implements IParameterValidator {
    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!new File(value).exists()) {
            throw new ParameterException("The provided " + name + " points to a non-existing file.");
        }
    }
}
