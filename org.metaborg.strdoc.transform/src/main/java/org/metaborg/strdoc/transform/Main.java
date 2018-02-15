package org.metaborg.strdoc.transform;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.strdoc.transform.cli.Arguments;
import org.metaborg.strdoc.transform.extractor.Extractor;

public class Main {
    public static void main(String[] args) {
        try (final Spoofax spoofax = new Spoofax(new Module())) {
            Arguments arguments = new Arguments();

            JCommander commander = JCommander.newBuilder()
                    .addObject(arguments)
                    .build();

            commander.parse(args);

            if (arguments.isHelp()) {
                commander.usage();
            } else {
                Extractor extractor = spoofax.injector.getInstance(Extractor.class);
                extractor.extract(arguments);
            }
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
