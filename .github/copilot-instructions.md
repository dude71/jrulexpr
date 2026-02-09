# Copilot / AI agent instructions for JRuleXpr

Purpose: quick, actionable guidance to make AI coding agents productive in this repository.

- **Project root:** Java 17 + Maven project. See `pom.xml` for Java version and dependencies.
- **Build:** use Maven. Typical commands: `mvn test` (runs JUnit5 tests), `mvn -DskipTests package` (build jar/assembly).
- **Tests / reports:** surefire reports are under `target/surefire-reports/` after running tests.

**Big-picture architecture**
- **Orchestrator:** `JRuleXpr` (src/main/java/org/d71/jrulexpr/JRuleXpr.java) generates item rules via `ItemRuleGenerator`.
- **Platform integration:** `JRuleXprLoader` integrates with OpenHAB's JRule runtime and checks for generated classes under `org.openhab.automation.jrule.rules.user.generated`.
- **Expression engine:** `JrxExpression` (uses EvalEx) composes expressions from items+functions and evaluates them.
- **Registries & packages:** `item/`, `function/`, `expression/`, `rule/` contain core logic. Key registries: `JrxItemRegistry` and `JrxFunctionRegistry`.

**Concrete patterns and points-of-change**
- **Item discovery:** `JrxItemRegistry.getItems()` filters items by tags (`jrx`, `jrxp`, `jrxt`, `jrxf`) or metadata keys of the same names. See `item/JrxItemRegistry.java`.
- **Add a new function:** create a class under `function/` extending `JrxFunction`, then add a `registry.register(YourClass.class)` entry in `JrxFunctionRegistry.getInstance()` so it appears in expressions.
- **Expression internals:** `JrxExpression` prepares function instances from tokens found in the AST (EvalEx). Use `getFunctionTokens()` or `getFunctionInstances()` to inspect tokens used by an expression.
- **Null handling:** note the comment in `JrxExpression.evaluate()` — null map values break OpenJDK EvalEx usage; code avoids putting nulls into the values map.

**Generated rules & reload behavior**
- `JRuleXprLoader.load()` will skip generation when generated classes exist. It checks for `NetRules` in `org.openhab.automation.jrule.rules.user.generated` and for the `jrulexpr-reload` resource to force reload. See `JRuleXprLoader.java` for exact checks.

**Dependencies & environment notes**
- Several OpenHAB modules are declared `provided` in `pom.xml`. Running full integration requires OpenHAB classpath; unit tests mock or use provided test helpers.
- Expression parsing/evaluation uses EvalEx (`com.ezylang:EvalEx`). Function wiring uses reflection—tests rely on Mockito and JUnit 5.

**Developer workflows & useful commands**
- Run whole test suite: `mvn test`.
- Run a single test: `mvn -Dtest=org.d71.jrulexpr.function.EnabledTest test`.
- Quick compile without tests: `mvn -DskipTests package`.
- If you need surefire logs: `target/surefire-reports/`.

**Conventions & gotchas for agents**
- Prefer minimal, focused edits: add function classes and register them in `JrxFunctionRegistry` rather than editing registry creation logic.
- When changing expression behavior, update both `JrxExpression` and function classes; tests often assert tokens and evaluation paths.
- Respect provided-scoped OpenHAB dependencies: avoid adding runtime assumptions that the repository does not include OpenHAB runtime classes.

If anything is unclear or you want additional CI/IDE commands (e.g., test debug settings, reproducible environment, or example inputs), tell me which area to expand.
