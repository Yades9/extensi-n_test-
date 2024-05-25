package eu.kanade.tachiyomi.animeextension.es.ninemanga

import eu.kanade.tachiyomi.source.AnimeSource
import eu.kanade.tachiyomi.source.model.Anime
import eu.kanade.tachiyomi.source.model.AnimesPage
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.network.asJsoup
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class NineManga : AnimeSource() {

    override val name = "NineManga"

    override val baseUrl = "https://es.ninemanga.com"

    override val lang = "es"

    private val client = OkHttpClient()

    override fun popularAnimeRequest(page: Int): Request {
        val url = "$baseUrl/category/index_$page.html"
        return GET(url, headers)
    }

    override fun popularAnimeParse(response: Response): AnimesPage {
        val document = response.asJsoup()
        val animes = mutableListOf<Anime>()

        document.select("div.bookbox").forEach { element ->
            val anime = Anime(
                title = element.select("a.bookname").text(),
                url = baseUrl + element.select("a.bookname").attr("href"),
                thumbnailUrl = element.select("img.bookimg").attr("src"),
                author = element.select("a.writer").text(),
                genre = element.select("p.tag").joinToString(", ") { it.text() }
            )
            animes.add(anime)
        }

        val hasNextPage = document.select("a.next").isNotEmpty()
        return AnimesPage(animes, hasNextPage)
    }

    override fun searchAnimeRequest(page: Int, query: String): Request {
        val url = "$baseUrl/search/?wd=$query&page=$page"
        return GET(url, headers)
    }

    override fun searchAnimeParse(response: Response): AnimesPage {
        val document = response.asJsoup()
        val animes = mutableListOf<Anime>()

        document.select("div.bookbox").forEach { element ->
            val anime = Anime(
                title = element.select("a.bookname").text(),
                url = baseUrl + element.select("a.bookname").attr("href"),
                thumbnailUrl = element.select("img.bookimg").attr("src"),
                author = element.select("a.writer").text(),
                genre = element.select("p.tag").joinToString(", ") { it.text() }
            )
            animes.add(anime)
        }

        val hasNextPage = document.select("a.next").isNotEmpty()
        return AnimesPage(animes, hasNextPage)
    }

    override fun headersBuilder(): Headers.Builder = Headers.Builder().apply {
        add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
        add("Referer", baseUrl)
    }
}
