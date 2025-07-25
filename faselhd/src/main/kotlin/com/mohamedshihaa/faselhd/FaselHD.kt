package com.mohamedshihaa.faselhd

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class FaselHD : MainAPI() {
    override var name = "FaselHD"
    override var mainUrl = "https://web185.faselhd.cafe/"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/search/${query.replace(" ", "+")}/"
        val doc = app.get(url).document

        return doc.select(".Grid--WecimaPosts .WecimaPostItem").mapNotNull {
            it.toSearchResponse()
        }
    }

    private fun Element.toSearchResponse(): SearchResponse? {
        val title = this.selectFirst(".Title")?.text() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val poster = fixUrlNull(this.selectFirst("img")?.attr("src"))
        val isMovie = href.contains("/movie/")

        return if (isMovie) {
            MovieSearchResponse(
                title, href, this@FaselHD.name, TvType.Movie, poster
            )
        } else {
            TvSeriesSearchResponse(
                title, href, this@FaselHD.name, TvType.TvSeries, poster
            )
        }
    }
}
