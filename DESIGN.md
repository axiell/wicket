# DESIGN.md

Durable architecture notes extracted from `AGENTS.md` to save main context.

## Project Overview

- Apache Wicket 1.4.x fork, version `1.4.23c`.
- Repo carries active servlet support plus Liferay/JSR-286 portlet support.
- Main framework module: `wicket/`.

## Main Modules

- `wicket/`: core framework, request cycle, components, markup, models, testing helpers.
- `wicket-extensions/`: higher-level components such as tables, trees, autocomplete.
- `wicket-datetime/`: Joda-Time-based date/time components.
- `wicket-auth-roles/`: role-based authorization helpers.
- `wicket-ioc/`: IoC integration base.
- `wicket-spring/`: Spring integration.
- `wicket-guice/`: Guice integration.
- `wicket-jmx/`: JMX support.
- `wicket-velocity/`: Velocity rendering integration.
- `wicket-devutils/`: developer utilities.
- `wicket-examples/`: example webapp, separate WAR, excluded from default root profile.
- `testing/wicket-threadtest/`: thread/stress test module included by default root profile.

## Request Flow

Representative entrypoints in `wicket/src/main/java`:

- `org/apache/wicket/protocol/http/WicketFilter.java`: servlet filter entrypoint for web traffic.
- `org/apache/wicket/protocol/http/WebRequestCycle.java`: per-request processing.
- `org/apache/wicket/protocol/http/WebRequestCycleProcessor.java`: resolves and dispatches request targets.
- `org/apache/wicket/IRequestTarget.java`: request target abstraction.

Implication: request-routing or rendering bugs near HTTP handling usually involve these classes first, not leaf components.

## Component / Markup Model

- Java components and their markup/resources are co-located by package.
- `wicket:id` values must match component ids in Java.
- `.properties` localization files also live next to component classes.
- Root Maven config packages non-Java files from `src/main/java` and `src/test/java`, which is why co-location works in this repo.

## Testing Model

- `org/apache/wicket/util/tester/WicketTester.java` is the standard in-process harness.
- `WicketTester.DummyWebApplication` uses `HttpSessionStore` specifically to avoid heavier stores.
- `WicketTester.NonPageCachingDummyWebApplication` exists for cheaper tests when page caching/back-button behavior is irrelevant.

## Fork-Specific Notes

- `WicketFilter` contains Arena-specific request-depth/original-URL attributes under `com.axiell.wicket.*`.
- Portlet support classes under `org/apache/wicket/protocol/http/portlet/` remain part of the fork's real behavior surface.

## Build Facts Worth Remembering

- Root `pom.xml` sets Java source/target to `1.6`.
- Surefire includes only `**/*Test.java`.
- `wicket-examples` has `maven-jetty-plugin` configured and builds as a WAR when you need a runnable sample app.
