import eu.kanade.tachiyomi.animeextension.AnimeExtension
import eu.kanade.tachiyomi.source.model.Anime
import eu.kanade.tachiyomi.source.model.AnimesPage
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asJsoup
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class NineManga : AnimeExtension() {

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

    override fun fetchAnimeDetails(anime: Anime): Anime {
        val response = client.newCall(GET(anime.url, headers)).execute()
        val document = response.asJsoup()

        val chapters = document.select("ul.detail-chlist li").map { chapterElement ->
            val chapterUrl = baseUrl + chapterElement.select("a").attr("href")
            val chapterTitle = chapterElement.select("a").text()
            AnimeChapter(chapterTitle, chapterUrl)
        }

        return anime.copy(chapters = chapters)
    }

    override fun headersBuilder(): Headers.Builder = Headers.Builder().apply {
        add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
        add("Referer", baseUrl)
    }

    private fun Response.asJsoup(): Document = this.asJsoup()

    data class AnimeChapter(
        val title: String,
        val url: String
    )

    // ============================== Chapters ==============================
    override fun chapterListSelector() = "ul.detail-chlist li"

    override fun chapterFromElement(element: Element): SChapter {
        val chapterUrl = element.select("a").attr("abs:href")
        val chapterTitle = element.select("a").text()
        return SChapter.create().apply {
            setUrlWithoutDomain(chapterUrl)
            name = chapterTitle
        }
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val document = response.asJsoup()
        return document.select(chapterListSelector()).map { chapterFromElement(it) }
    }
}

private fun Response.asJsoup(): Document = this.asJsoup()

data class AnimeChapter(
    val title: String,
    val url: String
)
