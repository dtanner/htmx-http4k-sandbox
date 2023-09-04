package com.example

import com.example.news.LatestNewsViewModel
import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.routing.*
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.viewModel
import java.time.Instant

val view = Body.viewModel(HandlebarsTemplates().HotReload("src/main/resources"), TEXT_HTML).toLens()

fun webContent(hotReload: Boolean): HttpHandler {
    val (renderer, resourceLoader) = buildResourceLoaders(hotReload)

    return routes(
            static(resourceLoader),
            htmxWebjars(),
            "/" bind GET to {
                val view = Body.viewModel(renderer, TEXT_HTML).toLens()
                val viewModel = IndexViewModel
                Response(OK).with(view of viewModel)
            },
            "/news" bind GET to {
                val viewModel = LatestNewsViewModel(Instant.now().toString())
                Response(OK).with(view of viewModel)
            },
    )
}


private fun buildResourceLoaders(hotReload: Boolean) = when {
    hotReload -> HandlebarsTemplates().HotReload("./src/main/resources") to ResourceLoader.Classpath("public")
    else -> HandlebarsTemplates().CachingClasspath() to ResourceLoader.Classpath("public")
}

fun main() {
    // if setting this to true, remember to run the app with the working directory set to the root of the example
    val hotReload = false

    webContent(hotReload).asServer(Undertow(9000)).start()
}