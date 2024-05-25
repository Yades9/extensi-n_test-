import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asJsoup
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import android.app.Application
import android.content.SharedPreferences
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.lib.streamtape.StreamTapeExtractor
import eu.kanade.tachiyomi.lib.voeextractor.VoeExtractor
import eu.kanade.tachiyomi.lib.filemoonextractor.FilemoonExtractor
import eu.kanade.tachiyomi.lib.streamwishector.StreamWishExtractor
import eu.kanade.tachiyomi.lib.doodstreamextractor.DoodExtractor

class NineManga : ConfigurableSource, ParsedHttpSource() {

    override val name = "NineManga"

    override val baseUrl = "https://es.ninemanga.com"

    override val lang = "es"

    override val supportsLatest = true

    private val client = OkHttpClient()

    override fun headersBuilder(): Headers.Builder = Headers.Builder().apply {
        add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
        add("Referer", baseUrl)
    }

    override fun popularMangaRequest(page: Int): Request {
        val url = "$baseUrl/category/index_$page.html"
        return GET(url, headers)
    }

    override fun popularMangaSelector() = "div.bookbox"

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        manga.title = element.select("a.bookname").text()
        manga.setUrlWithoutDomain(element.select("a.bookname").attr("href"))
        manga.thumbnail_url = element.select("img.bookimg").attr("src")
        manga.author = element.select("a.writer").text()
        manga.genre = element.select("p.tag").joinToString(", ") { it.text() }
        return manga
    }

    override fun popularMangaNextPageSelector() = "a.next"

    override fun latestUpdatesRequest(page: Int): Request {
        val url = "$baseUrl/category/latest_$page.html"
        return GET(url, headers)
    }

    override fun latestUpdatesSelector() = popularMangaSelector()

    override fun latestUpdatesFromElement(element: Element): SManga = popularMangaFromElement(element)

    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/search/?wd=$query&page=$page"
        return GET(url, headers)
    }

    override fun searchMangaSelector() = popularMangaSelector()

    override fun searchMangaFromElement(element: Element): SManga = popularMangaFromElement(element)

    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    override fun mangaDetailsParse(document: Document): SManga {
        val manga = SManga.create()
        manga.title = document.select("h1.title").text()
        manga.author = document.select("a.writer").text()
        manga.artist = document.select("a.illustrator").text()
        manga.genre = document.select("p.tags").joinToString(", ") { it.text() }
        manga.description = document.select("p.desc").text()
        manga.status = parseStatus(document.select("p.status").text())
        manga.thumbnail_url = document.select("div.cover img").attr("src")
        return manga
    }

    private fun parseStatus(status: String): Int {
        return when {
            status.contains("En curso") -> SManga.ONGOING
            status.contains("Finalizado") -> SManga.COMPLETED
            else -> SManga.UNKNOWN
        }
    }

    override fun chapterListSelector() = "ul.detail-chlist li"

    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()
        chapter.setUrlWithoutDomain(element.select("a").attr("href"))
        chapter.name = element.select("a").text()
        return chapter
    }

    override fun pageListParse(document: Document): List<Page> {
        return document.select("div.page-chapter img").mapIndexed { i, element ->
            Page(i, "", element.attr("data-original"))
        }
    }

    override fun imageUrlParse(document: Document): String {
        return document.select("div.page-chapter img").attr("data-original")
    }

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        // Setup preferences if necessary
    }
}

private fun Response.asJsoup(): Document = Jsoup.parse(this.body!!.string())
