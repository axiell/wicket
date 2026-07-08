# AGENTS.md

Compact repo notes for OpenCode. Keep durable framework detail in `DESIGN.md`.

## Read First

- `CLAUDE.md` only points here. Put agent-facing repo guidance here, not there.
- `DESIGN.md` holds Wicket architecture and domain notes that are useful but too bulky for every session.

## Build And Test

- Root build: `mvn package`
- Install all modules locally: `mvn install`
- Root tests: `mvn test`
- Single module: `mvn test -pl wicket-extensions`
- Single test class in one module: `mvn test -pl wicket -Dtest=ComponentTest`
- Faster focused package without examples: `mvn package -pl wicket,wicket-extensions,wicket-datetime,wicket-auth-roles,wicket-spring,wicket-ioc`

## Repo Shape

- Multi-module Maven repo. Active default profile is `all` in root `pom.xml`; it includes core modules, `archetypes/quickstart`, and `testing/wicket-threadtest`, but not `wicket-examples`.
- Core framework code lives in `wicket/`. Most framework changes should be verified there first.
- `wicket-examples/` is a separate WAR module with Jetty plugin configured; useful for runnable examples, but not part of the default root build.
- Non-Java package resources are intentionally stored under `src/main/java` and test fixtures under `src/test/java`; root `pom.xml` treats both as resources. Do not "clean up" co-located `.html`, `.properties`, `.js`, or similar files into `resources/` unless task explicitly requires it.

## Testing Quirks

- Surefire only includes `**/*Test.java`. New tests must match that suffix or they will not run.
- Wicket component tests commonly use `org.apache.wicket.util.tester.WicketTester`; prefer that over inventing heavier harnesses.
- When changing only one module, prefer `-pl <module>` verification instead of full-repo test runs.

## Change Traps

- Markup and i18n files are coupled to Java class location and component ids. Renaming/moving component classes often requires matching `.html` and `.properties` changes in same package.
- Portlet support is live code in this fork, not dead legacy. Changes in `wicket/protocol/http/**` can affect both servlet and portlet paths.
- `wicket/src/main/java/org/apache/wicket/protocol/http/WicketFilter.java` contains Arena-specific additions (`com.axiell.*` request attributes). Do not remove as upstream noise.
- Root build targets Java `1.6` bytecode via `maven-compiler-plugin`, even though old docs mention newer JDKs. Avoid APIs/language features beyond Java 6 compatibility in production code.

## Release / Ops

- Root has legacy release scripts (`release.sh`, `release-*.sh`) that assume old local tooling like `mvn5`, GPG, and profile-driven release packaging. Do not rely on them unless task is explicitly release-related.

## Axiell Vault

- This fork (`arena-1.4.x`, Java 6 target) is ingested into the Axiell Obsidian vault as `Arena Wicket Repo Docs`. Check it for framework-fork rationale and portlet-path history before assuming upstream Wicket behavior — route lookups through `@obsidian-helper`.

## Knowledge Graph (understand-anything)

An understand-anything knowledge graph of this repo lives in `.understand-anything/knowledge-graph.json`.

- **2,703 nodes · 8 edges** (built 2026-07-08, 2,703 files analyzed). Edge count is low — the graph may be node-only for this repo; treat it as a file/symbol index rather than a full dependency graph until rebuilt.
- Query in-session: `/understand-chat`
- Rebuild: run `/understand` from repo root to regenerate
- Graph is a local artifact (untracked); regenerate after major refactors
