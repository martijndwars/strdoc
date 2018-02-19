# Stratego API documentation generator

## Goals

1. _Re-generate Stratego Standard Library (SSL) documentation._ The current API documentation website was generated in 2009. Almost a decade later the standard library has been extended and the API documentation (comments) have been updated. However, because the tooling to generate the documentation is lost, we have been stuck with the 2009 documentation.

2. *Ability to search for strategies.* It is 2018, so we want to easily search for strategies. If I want to know how to "concatenate" things in Stratego, I want to search for "concat" and find "concat", "mapconcat(s)", "tconcat(s)", and "concat-strings".

## Architecture

This project consists of three sub-projects:

- `org.metaborg.strdoc.lang` is a Spoofax language project that contains a syntax definition for Stratego. The syntax definition is identical to the original Stratego synax definition, except that it parses multi-line comments as terms instead of as whitespace.
- `org.metaborg.strdoc.transform` is a Java project that parses every module in the Stratego Standard Library (SSL) and outputs a .json file that describes the API.
- `org.metaborg.strdoc.website` is a website project that provides a nice user interface to the aforementioned .json files.

## Usage

First, use Maven to build the `strdoc.lang` and `strdoc.transform` projects:

```
$ mvn package
```

Second, run `strdoc.transform` to extract the documentation and save it as .json:

```
$ java \
  -jar org.metaborg.strdoc.transform/target/org.metaborg.strdoc.transform-2.5.0-SNAPSHOT-jar-with-dependencies.jar \
  --stratego /path/to/org.metaborg.meta.lang.stratego/target/org.metaborg.meta.lang.stratego-2.4.0-SNAPSHOT.spoofax-language \
  --doc /path/to/org.metaborg.strdoc.lang/target/org.metaborg.strdoc.lang-2.4.0-SNAPSHOT.spoofax-language \
  --project /path/to/spoofax-releng/strategoxt/strategoxt/stratego-libraries/lib/spec \
  --output /path/to/strdoc/org.metaborg.strdoc.website/
```

Third, build and run a Docker container with a nginx webserver:

```
docker build -t docs . && docker run -p 80:80 docs
```

Finally, open localhost in your webbrowser:

```
open http://localhost
```

## Deploy

To deploy the website, run:

```
$ org.metaborg.strdoc.website/deploy.sh
```

## Todo

* Feature: Cross-reference the source code (reference links to the declaration)
  - This is something the old Stratego docs does better.
  - This requires analysis on Stratego code. Can we get an NaBL2 spec that does name resolution?
* Bug: Some whitespace gets lost in the description (see util/config/parse-options)
* Bug: When pretty-printing the type of a parameter it looses its parenthesis, so the string looses associativity/precedence.
* Bug: Cannot parse `collection/hash-table/scoped-finite-map.str`, `system/io/file.str`, and `system/posix/pipe-abstractions.str`.
* Bug: When visiting a non-existent page, return a 404 error (so Google removes the page).
* Feature: Update the page title so we get nice history in the browser.
* Feature: Show method summary.
* Feature: Extract constructors (with comments)
* Feature: Extract overlays (with comments)
* Idea: Search based on type (ala Haskell's Hoogle)
  - Probably requires more formal type annotations on Stratego
* Idea: Allow comments/discussion on a strategy definition ala php.net?
* Idea: Show module's strategies in the right side menu, like in Scala API docs.
* Idea: Apply strdoc to [https://github.com/metaborg/spoofax/tree/5c1114453ec2b30abcaf6fc51c15bf29872f6603/meta.lib.spoofax/trans/libspoofax](libspoofax).

## Inspiration

- Current Stratego docs: releases.strategoxt.org/docs/api/libstratego-lib/stable/docs/
- Rob Vermaas' thesis on xDoc (2004): http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.102.3625&rank=4
- Searchable xdoc: http://xdoc.martkolthof.nl/
- Rust API docs: https://doc.rust-lang.org/std/
- Scala API docs: http://www.scala-lang.org/api/current/
- AngularJS API docs: https://docs.angularjs.org/api/
- PHP API generator: http://www.apigen.org/ApiGen/

