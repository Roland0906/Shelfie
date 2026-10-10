package com.rolandlin.shelfie.buildlogic

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

/**
 * Module dependency rules. A violation fails the build during configuration,
 * long before code review or CI would notice it.
 */
private data class Rule(val from: Regex, val forbidden: Regex, val reason: String)

private val RULES = listOf(
    Rule(
        from = Regex("^:feature:.+"),
        forbidden = Regex("^:feature:.+"),
        reason = "features must not depend on each other; cross-feature navigation is wired in :app",
    ),
    Rule(
        from = Regex("^:feature:.+"),
        forbidden = Regex("^:core:(network|database|datastore)$"),
        reason = "features read data only through :core:data interfaces, never DAOs or API clients",
    ),
    Rule(
        from = Regex("^:core:network$"),
        forbidden = Regex("^:core:(database|datastore|data)$"),
        reason = "network and database stay independent; combining them is :core:data's job",
    ),
    Rule(
        from = Regex("^:core:database$"),
        forbidden = Regex("^:core:(network|datastore|data)$"),
        reason = "network and database stay independent; combining them is :core:data's job",
    ),
    Rule(
        from = Regex("^:core:.+"),
        forbidden = Regex("^:(feature:.+|app)$"),
        reason = "core modules must not depend on features or the app",
    ),
    Rule(
        from = Regex("^:server$"),
        forbidden = Regex("^:(app|feature:.+|core:(network|database|datastore|data|designsystem|testing))$"),
        reason = "the server shares only pure Kotlin code (:core:model, :core:common) with the app",
    ),
    Rule(
        from = Regex("^:(app|feature:.+|core:.+)$"),
        forbidden = Regex("^:server$"),
        reason = "the app talks to the server over HTTP, never through its code",
    ),
)

internal fun Project.enforceModuleRules() {
    afterEvaluate {
        val violations = configurations
            .flatMap { configuration ->
                configuration.dependencies.withType(ProjectDependency::class.java).map { configuration.name to it.path }
            }
            .distinct()
            .flatMap { (configuration, target) ->
                RULES.filter { it.from.matches(path) && it.forbidden.matches(target) && target != path }
                    .map { "  $path --($configuration)--> $target: ${it.reason}" }
            }
        if (violations.isNotEmpty()) {
            throw GradleException("Module dependency rules violated:\n" + violations.joinToString("\n"))
        }
    }
}
