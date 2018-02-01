# Stratego API documentation generator

## Goals

1. _Re-generate Stratego Standard Library (SSL) documentation._ The current API documentation website was generated in 2009. Almost a decade later the standard library has been extended and the API documentation (comments) have been updated. However, because the tooling to generate the documentation is lost, we have been stuck with the 2009 documentation.

2. *Ability to search for strategies.* It is 2018, we want search and we want it now. If I want to know how to "concatenate" things in Stratego, I want to search for "concat" and find "concat", "mapconcat(s)", "tconcat(s)", and "concat-strings".

3. *Ability to generate your own documentation.* It could be useful for other Stratego codebases to generate browsable and searchable API documentation. This could make it easier for new people to get familiar with the codebase.

## Architecture

This project consists of three sub-projects:

- `org.metaborg.strdoc.lang` is a Spoofax language project that contains a syntax definition for Stratego. The syntax definition is identical to the original Stratego synax definition, except that it parses multi-line comments as terms instead of as whitespace.
- `org.metaborg.strdoc.transform` is a Java project that parses every module in the Stratego Standard Library (SSL) and outputs a .json file that describes the API.
- `org.metaborg.strdoc.website` is a website project that provides a nice user interface to the aforementioned .json files.

## Usage

...

If you don't have a webserver, install Node's `http-server`:

```
$ npm install -g http-server
```

Then run a HTTP server that serves the files in `org.metaborg.strdoc.website`:

```
$ http-server org.metaborg.strdoc.website
```

## Deploy

To deploy the website, run:

```
$ org.metaborg.strdoc.website/deploy.sh
```

## Todo

* Feature: Link to source on module page. Currently, only strategies link to their specific region.
* Bug: Some whitespace gets lost in the description (see util/config/parse-options)
* Bug: When pretty-printing the type of a parameter it looses its parenthesis, so the string looses associativity/precedence.
* Bug: Cannot parse `collection/hash-table/scoped-finite-map.str`, `system/io/file.str`, and `system/posix/pipe-abstractions.str`.
* Feature: Strategy docblock can have @see tag
* Feature: Update the page title so we get nice history in the browser.
* Feature: Show method summary.
* Feature: Extract constructors (with comments)
* Feature: Extract overlays (with comments)
* Idea: Search based on type.

## Inspiration

For the presentation:

- Current Stratego docs: releases.strategoxt.org/docs/api/libstratego-lib/stable/docs/
- Rust API docs: https://doc.rust-lang.org/std/
- Scala API docs: http://www.scala-lang.org/api/current/
- AngularJS API docs: https://docs.angularjs.org/api/
- PHP API generator: http://www.apigen.org/ApiGen/
